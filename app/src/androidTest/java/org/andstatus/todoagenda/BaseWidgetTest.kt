package org.andstatus.todoagenda

import android.util.Log
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.andstatus.todoagenda.util.LazyVal
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert
import org.junit.Before

/**
 * @author yvolk@yurivolkov.com
 */
open class BaseWidgetTest {
    protected lateinit var provider: FakeCalendarContentProvider
    protected var factory = LazyVal.of { RemoteViewsFactory(provider.context, provider.widgetId, false) }
    @Before
    @Throws(Exception::class)
    open fun setUp() {
        provider = FakeCalendarContentProvider.contentProvider
    }

    @After
    @Throws(Exception::class)
    open fun tearDown() {
        FakeCalendarContentProvider.tearDown()
        factory.reset()
    }

    @JvmOverloads
    fun dateTime(
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        hourOfDay: Int = 0,
        minuteOfHour: Int = 0
    ): DateTime {
        return DateTime(
            year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0,
            provider.settings.clock().zone
        )
    }

    protected fun playResults(tag: String?) {
        Log.d(tag, provider.widgetId.toString() + " playResults started")
        provider.updateAppSettings(tag)
        if (provider.usesActualWidget) {
            InstanceState.clear(provider.widgetId)
            EnvironmentChangedReceiver.updateWidget(provider.context, provider.widgetId)
            if (!RemoteViewsFactory.factories.containsKey(provider.widgetId)) {
                waitForRemoteViewsFactoryCreation()
            }
            waitTillWidgetIsUpdated(tag)
            waitTillWidgetIsReloaded(tag)
            waitTillWidgetIsRedrawn(tag)
            EnvironmentChangedReceiver.sleep(1000)
            if (InstanceState.get(provider.widgetId).listReloaded == 0L) {
                Log.d(tag, provider.widgetId.toString() + " was not reloaded by a Launcher")
                getFactory().onDataSetChanged()
            }
        } else {
            getFactory().onDataSetChanged()
        }
        getFactory().logWidgetEntries(tag)
        Log.d(tag, provider.widgetId.toString() + " playResults ended")
    }

    private fun waitForRemoteViewsFactoryCreation() {
        val start = System.currentTimeMillis()
        while (RemoteViewsFactory.factories[settings.widgetId] == null &&
            Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION
        ) {
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsUpdated(tag: String?) {
        val start = System.currentTimeMillis()
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState.get(provider.widgetId).updated > 0) {
                Log.d(tag, provider.widgetId.toString() + " updated")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsReloaded(tag: String?) {
        val start = System.currentTimeMillis()
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState.get(provider.widgetId).listReloaded > 0) {
                Log.d(tag, provider.widgetId.toString() + " reloaded")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsRedrawn(tag: String?) {
        val start = System.currentTimeMillis()
        while (Math.abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState.get(provider.widgetId).listRedrawn > 0) {
                Log.d(tag, provider.widgetId.toString() + " redrawn")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    protected val settings: InstanceSettings
        get() = provider.settings

    fun getFactory(): RemoteViewsFactory {
        val existingFactory = RemoteViewsFactory.factories[provider.widgetId]
        return existingFactory ?: factory.get()
    }

    protected fun ensureNonEmptyResults() {
        val inputs = provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.birthday)
        val settings = settings
        settings.setFilterMode(FilterMode.NO_FILTERING)
        provider.addResults(inputs)
    }

    protected fun assertPosition(ind: Int, position: WidgetEntryPosition) {
        val widgetEntries = getFactory().widgetEntries
        Assert.assertTrue(
            "Expecting " + position + " at " + (ind + 1) + "th entry, but found only " +
                widgetEntries.size + " entries", widgetEntries.size > ind
        )
        Assert.assertEquals(widgetEntries[ind].toString(), position, widgetEntries[ind].entryPosition)
    }

    companion object {
        val TAG: String = BaseWidgetTest::class.java.simpleName
        private const val MAX_MILLIS_TO_WAIT_FOR_LAUNCHER = 2000
        private const val MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION = 40000
    }
}
