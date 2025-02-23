package org.andstatus.todoagenda

import android.util.Log
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntryType
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import kotlin.math.abs

/**
 * @author yvolk@yurivolkov.com
 */
open class BaseWidgetTest {
    protected lateinit var provider: FakeCalendarContentProvider

    protected val factory1: RemoteViewsFactory by lazy {
        RemoteViewsFactory(provider.context, provider.widgetId, false)
    }
    val factory: RemoteViewsFactory
        get() {
            val existingFactory = RemoteViewsFactory.factories[provider.widgetId]
            return existingFactory ?: factory1
        }

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        provider = FakeCalendarContentProvider.contentProvider
    }

    @After
    @Throws(Exception::class)
    open fun tearDown() {
        FakeCalendarContentProvider.tearDown()
    }

    @JvmOverloads
    fun dateTime(
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        hourOfDay: Int = 0,
        minuteOfHour: Int = 0,
    ): DateTime = DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0, provider.settings.timeZone)

    protected fun playResults(tag: String) {
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
            if (InstanceState[provider.widgetId].listReloaded == 0L) {
                Log.d(tag, provider.widgetId.toString() + " was not reloaded by a Launcher")
                factory.onDataSetChanged()
            }
        } else {
            factory.onDataSetChanged()
        }
        factory.logWidgetEntries(tag)
        Log.d(tag, provider.widgetId.toString() + " playResults ended")
    }

    private fun waitForRemoteViewsFactoryCreation() {
        val start = System.currentTimeMillis()
        while (RemoteViewsFactory.factories[settings.widgetId] == null &&
            abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION
        ) {
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsUpdated(tag: String?) {
        val start = System.currentTimeMillis()
        while (abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState[provider.widgetId].updated > 0) {
                Log.d(tag, provider.widgetId.toString() + " updated")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsReloaded(tag: String?) {
        val start = System.currentTimeMillis()
        while (abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState[provider.widgetId].listReloaded > 0) {
                Log.d(tag, provider.widgetId.toString() + " reloaded")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    private fun waitTillWidgetIsRedrawn(tag: String?) {
        val start = System.currentTimeMillis()
        while (abs(System.currentTimeMillis() - start) < MAX_MILLIS_TO_WAIT_FOR_LAUNCHER) {
            if (InstanceState[provider.widgetId].listRedrawn > 0) {
                Log.d(tag, provider.widgetId.toString() + " redrawn")
                break
            }
            EnvironmentChangedReceiver.sleep(20)
        }
    }

    protected val settings: InstanceSettings
        get() = provider.settings

    protected fun ensureNonEmptyResults() {
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.birthday)
        provider.settings =
            settings.copy(
                filterModeIn = FilterMode.NO_FILTERING,
            )
    }

    protected fun assertLastEntry(
        ind: Int,
        lastEntryType: LastEntryType,
    ) {
        val widgetEntries = factory.widgetEntries
        Assert.assertTrue(
            "Expecting " + lastEntryType + " at " + (ind + 1) + "th entry, but found only " +
                widgetEntries.size + " entries",
            widgetEntries.size > ind,
        )
        val lastEntry = widgetEntries[ind] as? LastEntry
        assertTrue("Expected LastEntry but was: ${widgetEntries[ind]}", lastEntry != null)
        Assert.assertEquals(widgetEntries[ind].toString(), lastEntryType, lastEntry?.type)
    }

    protected fun assertPosition(
        ind: Int,
        position: WidgetEntryPosition,
    ) {
        val widgetEntries = factory.widgetEntries
        Assert.assertTrue(
            "Expecting " + position + " at " + (ind + 1) + "th entry, but found only " +
                widgetEntries.size + " entries",
            widgetEntries.size > ind,
        )
        Assert.assertEquals(widgetEntries[ind].toString(), position, widgetEntries[ind].entryPosition)
    }

    companion object {
        val TAG: String = BaseWidgetTest::class.java.simpleName
        private const val MAX_MILLIS_TO_WAIT_FOR_LAUNCHER = 2000
        private const val MAX_MILLIS_TO_WAIT_FOR_FACTORY_CREATION = 40000
    }
}
