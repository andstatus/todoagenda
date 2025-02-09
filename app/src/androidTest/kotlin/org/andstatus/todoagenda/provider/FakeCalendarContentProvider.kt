package org.andstatus.todoagenda.provider

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import androidx.test.platform.app.InstrumentationRegistry
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.prefs.SettingsStorage
import org.andstatus.todoagenda.prefs.SnapshotMode
import org.andstatus.todoagenda.util.RawResourceUtils
import org.joda.time.DateTime
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * @author yvolk@yurivolkov.com
 */
class FakeCalendarContentProvider private constructor(
    val context: Context,
) {
    private val results = QueryResultsStorage()
    val widgetId: Int
    val usesActualWidget: Boolean

    val settingsRef = AtomicReference<InstanceSettings>()
    var settings: InstanceSettings
        get() = settingsRef.get() ?: throw java.lang.IllegalStateException("No settings stored")
        set(value) {
            settingsRef.set(value)
            AllSettings.addOrReplace(TAG, context, value)
        }

    init {
        val instanceToReuse =
            AllSettings
                .getInstances(context)
                .values
                .stream()
                .filter { obj: InstanceSettings -> obj.isForTestsReplaying }
                .findFirst()
                .orElse(null)
        usesActualWidget = instanceToReuse != null
        widgetId = if (usesActualWidget) instanceToReuse.widgetId else lastWidgetId.incrementAndGet()
        val timeZoneId = ZONE_IDS[(System.currentTimeMillis() % ZONE_IDS.size).toInt()]
        settings =
            InstanceSettings(
                contextIn = context,
                widgetId = widgetId,
                proposedInstanceName = "ToDo Agenda " + widgetId + " " + InstanceSettings.TEST_REPLAY_SUFFIX,
                lockedTimeZoneIdIn = timeZoneId,
            )
    }

    fun updateAppSettings(tag: String) {
        settings =
            settings.copy(
                logEvents = true,
                resultsStorage = results,
                snapshotModeIn = SnapshotMode.SNAPSHOT_TIME,
            )
        AllSettings.addOrReplace(tag, context, settings)
        if (results.results.isNotEmpty()) {
            Log.d(tag, "Results executed at " + settings.clock.now())
        }
    }

    fun addResults(newResults: QueryResultsStorage) {
        results.addResults(newResults)
    }

    val executedAt: DateTime get() = results.executedAt.get()

    fun setExecutedAt(executedAt: DateTime) {
        results.executedAt.set(executedAt)
    }

    fun addRow(event: CalendarEvent) {
        addRow(
            QueryRow()
                .setCalendarId(event.eventSource.source.id)
                .setEventId(event.eventId)
                .setTitle(event.title)
                .setBegin(event.startMillis)
                .setEnd(event.endMillis)
                .setDisplayColor(event.color)
                .setAllDay(if (event.isAllDay) 1 else 0)
                .setEventLocation(event.location)
                .setDescription(event.description)
                .setHasAlarm(if (event.isAlarmActive) 1 else 0)
                .setRRule(if (event.isRecurring) "FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE,FR" else null),
        )
    }

    fun addRow(queryRow: QueryRow) {
        val providerType = EventProviderType.CALENDAR
        val result = results.findLast(providerType).orElseGet { addFirstQueryResult(providerType) }
        result.addRow(queryRow)
    }

    private fun addFirstQueryResult(providerType: EventProviderType): QueryResult {
        ensureOneActiveEventSource(providerType)
        val r2 = QueryResult(providerType, settings.widgetId, settings.clock.now())
        results.addResult(r2)
        return r2
    }

    private fun ensureOneActiveEventSource(type: EventProviderType) {
        if (settings.activeEventSources
                .none { source: OrderedEventSource -> source.source.providerType === type }
        ) {
            val sourceId = settings.activeEventSources.size + 1
            val source = EventSource(type, sourceId, "(Mocked $type #$sourceId)", "", 0, true)
            val newSource = OrderedEventSource(source, 1)
            settings =
                settings.copy(
                    activeEventSourcesIn = settings.activeEventSources + newSource,
                )
        }
    }

    fun clear() {
        results.clear()
    }

    fun startEditingPreferences() {
        ApplicationPreferences.fromInstanceSettings(context, widgetId)
    }

    fun savePreferences() {
        ApplicationPreferences.save(context, widgetId)
        settings = AllSettings.instanceFromId(context, widgetId)
    }

    fun loadResultsAndSettings(
        @RawRes jsonResId: Int,
    ): QueryResultsStorage =
        try {
            val json =
                JSONObject(RawResourceUtils.getString(InstrumentationRegistry.getInstrumentation().context, jsonResId))
            json.getJSONObject(QueryResultsStorage.KEY_SETTINGS).put(InstanceSettings.PREF_WIDGET_ID, widgetId)
            val widgetData = WidgetData.fromJson(json)
            settings =
                widgetData.getSettingsForWidget(
                    context,
                    settings,
                    widgetId,
                )
            settings.resultsStorage?.also { addResults(it) }
                ?: throw IllegalStateException("No results storage")
        } catch (e: Exception) {
            throw IllegalStateException("loadResultsAndSettings" + e.message)
        }

    val firstActiveEventSource: OrderedEventSource
        get() {
            for (orderedSource in settings.activeEventSources) {
                return orderedSource
            }
            return OrderedEventSource.EMPTY
        }

    companion object {
        val TAG = FakeCalendarContentProvider::class.java.simpleName
        private const val TEST_WIDGET_ID_MIN = 434892
        private val ZONE_IDS = arrayOf("America/Los_Angeles", "Europe/Moscow", "Asia/Kuala_Lumpur", "UTC")
        private val lastWidgetId = AtomicInteger(TEST_WIDGET_ID_MIN)
        val contentProvider: FakeCalendarContentProvider
            get() {
                val targetContext =
                    InstrumentationRegistry.getInstrumentation().targetContext
                return FakeCalendarContentProvider(targetContext)
            }

        fun tearDown() {
            val toDelete: MutableList<Int> = ArrayList()
            val instances = AllSettings.loadedInstances
            for (settings in instances.values) {
                if (settings.widgetId >= TEST_WIDGET_ID_MIN) {
                    toDelete.add(settings.widgetId)
                }
            }
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            for (widgetId in toDelete) {
                instances.remove(widgetId)
                SettingsStorage.delete(context, AllSettings.getStorageKey(widgetId))
            }
            ApplicationPreferences.setWidgetId(context, TEST_WIDGET_ID_MIN)
        }
    }
}
