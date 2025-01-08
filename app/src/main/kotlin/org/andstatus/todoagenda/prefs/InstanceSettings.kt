package org.andstatus.todoagenda.prefs

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import org.andstatus.todoagenda.prefs.colors.ColorThemeType
import org.andstatus.todoagenda.prefs.colors.ThemeColors
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue
import org.andstatus.todoagenda.prefs.dateformat.DateFormatter
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.provider.hasResults
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.InstanceId
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.MyClock.Companion.defaultTimeZone
import org.andstatus.todoagenda.util.StringUtil
import org.andstatus.todoagenda.widget.Alignment
import org.andstatus.todoagenda.widget.EventEntryLayout
import org.andstatus.todoagenda.widget.TextShadow
import org.andstatus.todoagenda.widget.WidgetHeaderLayout
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.Int
import kotlin.math.abs

/**
 * Loaded settings of one Widget
 *
 * @author yvolk@yurivolkov.com
 */
data class InstanceSettings(
    private val contextIn: Context?,
    val widgetId: Int,
    val logEvents: Boolean = false,

    // ----------------------------------------------------------------------------------
    // Layout
    val isCompactLayout: Boolean = false,
    val widgetHeaderLayout: WidgetHeaderLayout = WidgetHeaderLayout.defaultValue,
    val widgetHeaderDateFormat: DateFormatValue = PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT,
    val showDayHeaders: Boolean = true,
    val dayHeaderDateFormat: DateFormatValue = PREF_DAY_HEADER_DATE_FORMAT_DEFAULT,
    val showPastEventsUnderOneHeader: Boolean = false,
    val dayHeaderAlignment: String = PREF_DAY_HEADER_ALIGNMENT_DEFAULT,
    val horizontalLineBelowDayHeader: Boolean = false,
    val showDaysWithoutEvents: Boolean = false,
    val eventEntryLayout: EventEntryLayout = EventEntryLayout.DEFAULT,
    val showEventIcon: Boolean = true,
    val entryDateFormat: DateFormatValue = PREF_ENTRY_DATE_FORMAT_DEFAULT,
    val isMultilineTitle: Boolean = PREF_MULTILINE_TITLE_DEFAULT,
    val maxLinesTitle: Int = PREF_MAXLINES_TITLE_DEFAULT,
    val isMultilineDetails: Boolean = PREF_MULTILINE_DETAILS_DEFAULT,
    val maxLinesDetails: Int = PREF_MAXLINES_DETAILS_DEFAULT,

    // ----------------------------------------------------------------------------------
    // Colors
    private val defaultColors: ThemeColors = if (contextIn == null) {
        ThemeColors.EMPTY
    } else {
        ThemeColors(contextIn, ColorThemeType.SINGLE)
    },
    private val darkColors: ThemeColors = ThemeColors.EMPTY,
    val textShadow: TextShadow = TextShadow.NO_SHADOW,

    // ----------------------------------------------------------------------------------
    // Event details
    val showEndTime: Boolean = PREF_SHOW_END_TIME_DEFAULT,
    val showLocation: Boolean = PREF_SHOW_LOCATION_DEFAULT,
    val showDescription: Boolean = PREF_SHOW_DESCRIPTION_DEFAULT,
    val fillAllDayEvents: Boolean = PREF_FILL_ALL_DAY_DEFAULT,
    val indicateAlerts: Boolean = true,
    val indicateRecurring: Boolean = false,

    // ----------------------------------------------------------------------------------
    // Event filters
    val eventsEnded: EndedSomeTimeAgo? = EndedSomeTimeAgo.NONE,
    val showPastEventsWithDefaultColor: Boolean = false,
    val eventRange: Int = PREF_EVENT_RANGE_DEFAULT.toInt(),
    val hideBasedOnKeywords: String? = "",
    val showBasedOnKeywords: String? = "",
    val showOnlyClosestInstanceOfRecurringEvent: Boolean = false,
    val hideDuplicates: Boolean = false,
    val allDayEventsPlacement: AllDayEventsPlacement = AllDayEventsPlacement.defaultValue,
    val taskScheduling: TaskScheduling = TaskScheduling.defaultValue,
    val taskWithoutDates: TasksWithoutDates = TasksWithoutDates.defaultValue,
    private val filterModeIn: FilterMode = FilterMode.defaultValue,

    // ----------------------------------------------------------------------------------
    // Calendars and task lists
    private val activeEventSourcesIn: List<OrderedEventSource> = emptyList(),

    // ----------------------------------------------------------------------------------
    // Other
    private val proposedInstanceName: String? = null,
    val textSizeScale: TextSizeScale = TextSizeScale.MEDIUM,
    val timeFormat: String = PREF_TIME_FORMAT_DEFAULT,
    private val lockedTimeZoneIdIn: String = "",
    private val startHourOfDayIn: Int = 0,
    private val snapshotModeIn: SnapshotMode = SnapshotMode.Companion.defaultValue,
    val resultsStorage: QueryResultsStorage? = null,
    private val refreshPeriodMinutesIn: Int = PREF_REFRESH_PERIOD_MINUTES_DEFAULT,
) {
    private val instanceId: Long = InstanceId.next()
    val context: Context get() = contextIn ?: throw IllegalStateException("Context is null")
    val widgetInstanceName: String = if (contextIn == null) "(empty)" else AllSettings.uniqueInstanceName(
        contextIn,
        widgetId,
        proposedInstanceName
    )
    val lockedTimeZoneId: String = DateUtil.validatedTimeZoneId(lockedTimeZoneIdIn)
    val startHourOfDay: Int = startHourOfDayIn.takeIf { abs(it) < 13 } ?: 0
    val hasResults: Boolean get() = resultsStorage.hasResults()
    val snapshotMode: SnapshotMode = if (snapshotModeIn.isSnapshotMode && !hasResults) {
        SnapshotMode.LIVE_DATA
    } else {
        snapshotModeIn
    }
    val snapshotDate: DateTime? = if (snapshotMode.isSnapshotMode) resultsStorage?.executedAt?.get() else null
    val timeZone: DateTimeZone = snapshotDate?.let<DateTime, DateTimeZone?> {
        if (snapshotMode == SnapshotMode.SNAPSHOT_TIME) it.zone else null
    } ?: if (StringUtil.nonEmpty(lockedTimeZoneId)) {
        DateTimeZone.forID(lockedTimeZoneId)
    } else {
        defaultTimeZone
    }
    val filterMode: FilterMode = if (filterModeIn == FilterMode.NORMAL_FILTER &&
        snapshotMode.isSnapshotMode
    ) FilterMode.DEBUG_FILTER else filterModeIn

    val snapshotActiveEventSources: MutableList<OrderedEventSource> = CopyOnWriteArrayList()
    val activeEventSources: List<OrderedEventSource>
        get() = (activeEventSourcesIn
            .takeIf { it.isNotEmpty() }
            ?: EventProviderType.availableSources) + snapshotActiveEventSources

    val refreshPeriodMinutes: Int = refreshPeriodMinutesIn.takeIf { it > 0 } ?: 10

    val clock = MyClock(
        snapshotMode = snapshotMode,
        snapshotDate = snapshotDate,
        timeZone = timeZone,
        startHourOfDay = startHourOfDay,
    )

    val isEmpty: Boolean
        get() = widgetId == 0

    /**
     * @return true if success
     */
    fun save(tag: String?, method: String): Boolean {
        val msgLog = "save from $method"
        if (widgetId == 0) {
            logMe(tag, "Skipped $msgLog", widgetId)
            return false
        }
        logMe(tag, msgLog, widgetId)
        try {
            SettingsStorage.saveJson(context, getStorageKey(widgetId), toJson())
            return true
        } catch (e: IOException) {
            Log.e(tag, "$msgLog $this", e)
        }
        return false
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put(PREF_WIDGET_ID, widgetId)

        // ----------------------------------------------------------------------------------
        // Layout
        put(PREF_COMPACT_LAYOUT, isCompactLayout)
        put(PREF_WIDGET_HEADER_LAYOUT, widgetHeaderLayout.value)
        put(PREF_WIDGET_HEADER_DATE_FORMAT, widgetHeaderDateFormat.save())
        put(PREF_SHOW_DAY_HEADERS, showDayHeaders)
        put(PREF_DAY_HEADER_DATE_FORMAT, dayHeaderDateFormat.save())
        put(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER, showPastEventsUnderOneHeader)
        put(PREF_DAY_HEADER_ALIGNMENT, dayHeaderAlignment)
        put(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER, horizontalLineBelowDayHeader)
        put(PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents)
        put(PREF_EVENT_ENTRY_LAYOUT, eventEntryLayout.value)
        put(PREF_SHOW_EVENT_ICON, showEventIcon)
        put(PREF_ENTRY_DATE_FORMAT, entryDateFormat.save())
        put(PREF_MULTILINE_TITLE, isMultilineTitle)
        put(PREF_MAXLINES_TITLE, maxLinesTitle)
        put(PREF_MULTILINE_DETAILS, isMultilineDetails)
        put(PREF_MAXLINES_DETAILS, maxLinesDetails)

        // ----------------------------------------------------------------------------------
        // Colors
        defaultColors.toJson(this)
        if (!darkColors.isEmpty) {
            put(PREF_DARK_THEME, darkColors.toJson(JSONObject()))
        }
        put(PREF_TEXT_SHADOW, textShadow.value)

        // ----------------------------------------------------------------------------------
        // Event details
        put(PREF_SHOW_END_TIME, showEndTime)
        put(PREF_SHOW_LOCATION, showLocation)
        put(PREF_SHOW_DESCRIPTION, showDescription)
        put(PREF_FILL_ALL_DAY, fillAllDayEvents)
        put(PREF_INDICATE_ALERTS, indicateAlerts)
        put(PREF_INDICATE_RECURRING, indicateRecurring)

        // ----------------------------------------------------------------------------------
        // Event filters
        put(PREF_EVENTS_ENDED, eventsEnded!!.save())
        put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, showPastEventsWithDefaultColor)
        put(PREF_EVENT_RANGE, eventRange)
        put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords)
        put(PREF_SHOW_BASED_ON_KEYWORDS, showBasedOnKeywords)
        put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, showOnlyClosestInstanceOfRecurringEvent)
        put(PREF_HIDE_DUPLICATES, hideDuplicates)
        put(PREF_ALL_DAY_EVENTS_PLACEMENT, allDayEventsPlacement.value)
        put(PREF_TASK_SCHEDULING, taskScheduling.value)
        put(PREF_TASK_WITHOUT_DATES, taskWithoutDates.value)
        put(PREF_FILTER_MODE, filterMode.value)

        // ----------------------------------------------------------------------------------
        // Calendars and task lists
        put(PREF_ACTIVE_SOURCES, OrderedEventSource.toJsonArray(activeEventSources))

        // ----------------------------------------------------------------------------------
        // Other
        put(PREF_WIDGET_INSTANCE_NAME, widgetInstanceName)
        put(PREF_TEXT_SIZE_SCALE, textSizeScale.preferenceValue)
        put(PREF_TIME_FORMAT, timeFormat)
        put(PREF_LOCKED_TIME_ZONE_ID, lockedTimeZoneId)
        put(PREF_START_HOUR_OF_DAY, startHourOfDay)
        put(PREF_SNAPSHOT_MODE, snapshotMode.value)
        put(PREF_REFRESH_PERIOD_MINUTES, refreshPeriodMinutes)
        if (resultsStorage.hasResults()) {
            put(PREF_RESULTS_STORAGE, resultsStorage?.toJson(context, widgetId, false))
        }
    }

    val isForTestsReplaying: Boolean
        get() = widgetInstanceName.endsWith(TEST_REPLAY_SUFFIX)

    fun getActiveEventSources(type: EventProviderType): List<OrderedEventSource> {
        val sources: MutableList<OrderedEventSource> = ArrayList()
        for (orderedSource in activeEventSources) {
            if (orderedSource.source.providerType === type) sources.add(orderedSource)
        }
        return sources
    }

    val endOfTimeRange: DateTime
        get() = (if (eventRange > 0) clock.now().plusDays(eventRange)
        else clock.startOfToday()
            .plusDays(1 - eventRange))
            .minusMillis(1)
    val startOfTimeRange: DateTime?
        get() = eventsEnded!!.endedAt(clock.now())

    fun colors(): ThemeColors {
        return if (!darkColors.isEmpty && isDarkThemeOn(context)) darkColors else defaultColors
    }

    fun widgetHeaderDateFormatter(): DateFormatter {
        return DateFormatter(context, widgetHeaderDateFormat, clock.now())
    }

    fun dayHeaderDateFormatter(): DateFormatter {
        return DateFormatter(context, dayHeaderDateFormat, clock.now())
    }

    fun entryDateFormatter(): DateFormatter {
        return DateFormatter(context, entryDateFormat, clock.now())
    }

    val isSnapshotMode: Boolean
        get() = snapshotMode.isSnapshotMode
    val isLiveMode: Boolean
        get() = snapshotMode.isLiveMode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val settings = other as InstanceSettings
        return toJson().toString() == settings.toJson().toString()
    }

    override fun hashCode(): Int {
        return toJson().toString().hashCode()
    }

    fun logMe(tag: String?, message: String, widgetId: Int) {
        Log.v(tag, "$message, widgetId:$widgetId instance:$instanceId\n${toJson()}")
    }

    fun noPastEvents(): Boolean {
        return filterMode != FilterMode.NO_FILTERING &&
            !showPastEventsWithDefaultColor && eventsEnded === EndedSomeTimeAgo.NONE &&
            noTaskSources()
    }

    fun noTaskSources(): Boolean {
        for (orderedSource in activeEventSources) {
            if (!orderedSource.source.providerType.isCalendar) return false
        }
        return true
    }

    val typesOfActiveEventProviders: List<EventProviderType>
        get() = activeEventSources.stream().map { s: OrderedEventSource? -> s!!.source.providerType }.distinct()
            .collect(
                Collectors.toList()
            )

    fun getActiveEventSource(type: EventProviderType, sourceId: Int): OrderedEventSource {
        for (orderedSource in activeEventSources) {
            if (orderedSource.source.providerType === type && orderedSource.source.id == sourceId) {
                return orderedSource
            }
        }
        if (isSnapshotMode) {
            // TODO: Map Calendars when moving between devices
            val eventSource = EventSource(
                type, sourceId, "($type #$sourceId)",
                "", 0, false
            )
            val orderedSource = OrderedEventSource(eventSource, activeEventSources.size + 1)
            snapshotActiveEventSources.add(orderedSource)
            return orderedSource
        }
        return OrderedEventSource.EMPTY
    }

    fun getFirstSource(isCalendar: Boolean): OrderedEventSource {
        for (orderedSource in activeEventSources) {
            if (orderedSource.source.providerType.isCalendar == isCalendar) {
                return orderedSource
            }
        }
        return OrderedEventSource.EMPTY
    }

    companion object {
        private val TAG = InstanceSettings::class.java.simpleName
        val EMPTY: InstanceSettings by lazy {
            InstanceSettings(contextIn = null, widgetId = 0)
        }
        const val PREF_WIDGET_ID = "widgetId"

        // ----------------------------------------------------------------------------------
        // Layout
        const val PREF_COMPACT_LAYOUT = "compactLayout"
        const val PREF_WIDGET_HEADER_LAYOUT = "widgetHeaderLayout"
        private const val PREF_SHOW_DATE_ON_WIDGET_HEADER = "showDateOnWidgetHeader" // till v 4.0
        const val PREF_WIDGET_HEADER_DATE_FORMAT = "widgetHeaderDateFormat"
        val PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT = DateFormatType.DEFAULT_WEEKDAY.defaultValue
        const val PREF_SHOW_DAY_HEADERS = "showDayHeaders"
        const val PREF_DAY_HEADER_DATE_FORMAT = "dayHeaderDateFormat"
        val PREF_DAY_HEADER_DATE_FORMAT_DEFAULT = DateFormatType.DEFAULT_WEEKDAY.defaultValue
        const val PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER = "showPastEventsUnderOneHeader"
        const val PREF_DAY_HEADER_ALIGNMENT = "dayHeaderAlignment"
        private val PREF_DAY_HEADER_ALIGNMENT_DEFAULT = Alignment.RIGHT.name
        const val PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER = "horizontalLineBelowDayHeader"
        const val PREF_SHOW_DAYS_WITHOUT_EVENTS = "showDaysWithoutEvents"
        const val PREF_EVENT_ENTRY_LAYOUT = "eventEntryLayout"
        const val PREF_SHOW_EVENT_ICON = "showEventIcon"
        const val PREF_ENTRY_DATE_FORMAT = "entryDateFormat"
        val PREF_ENTRY_DATE_FORMAT_DEFAULT: DateFormatValue = DateFormatType.HIDDEN.defaultValue

        const val PREF_MULTILINE_TITLE = "multiline_title"
        const val PREF_MULTILINE_TITLE_DEFAULT = false
        const val PREF_MAXLINES_TITLE = "maxLinesTitle"
        const val PREF_MAXLINES_TITLE_DEFAULT = 5
        const val PREF_MULTILINE_DETAILS = "multiline_details"
        const val PREF_MULTILINE_DETAILS_DEFAULT = false
        const val PREF_MAXLINES_DETAILS = "maxLinesDetails"
        const val PREF_MAXLINES_DETAILS_DEFAULT = 5
        const val PREF_DARK_THEME = "darkTheme"

        // ----------------------------------------------------------------------------------
        // Color
        const val PREF_TEXT_SHADOW = "textShadow"
        val PREF_TEXT_SHADOW_DEFAULT = TextShadow.NO_SHADOW.name

        // ----------------------------------------------------------------------------------
        // Event details
        const val PREF_SHOW_END_TIME = "showEndTime"
        const val PREF_SHOW_END_TIME_DEFAULT = true
        const val PREF_SHOW_LOCATION = "showLocation"
        const val PREF_SHOW_LOCATION_DEFAULT = true
        const val PREF_SHOW_DESCRIPTION = "showDescription"
        const val PREF_SHOW_DESCRIPTION_DEFAULT = false
        const val PREF_FILL_ALL_DAY = "fillAllDay"
        const val PREF_FILL_ALL_DAY_DEFAULT = true
        const val PREF_INDICATE_ALERTS = "indicateAlerts"
        const val PREF_INDICATE_RECURRING = "indicateRecurring"

        // ----------------------------------------------------------------------------------
        // Event filters
        const val PREF_EVENTS_ENDED = "eventsEnded"
        const val PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR = "showPastEventsWithDefaultColor"
        const val PREF_EVENT_RANGE = "eventRange"
        const val PREF_EVENT_RANGE_DEFAULT = "30"
        const val EVENT_RANGE_TODAY = 0
        const val EVENT_RANGE_TODAY_AND_TOMORROW = -1
        const val PREF_HIDE_BASED_ON_KEYWORDS = "hideBasedOnKeywords"
        const val PREF_SHOW_BASED_ON_KEYWORDS = "showBasedOnKeywords"
        const val PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT = "showOnlyClosestInstanceOfRecurringEvent"
        const val PREF_HIDE_DUPLICATES = "hideDuplicates"
        const val PREF_ALL_DAY_EVENTS_PLACEMENT = "allDayEventsPlacement"
        const val PREF_TASK_SCHEDULING = "taskScheduling"
        const val PREF_TASK_WITHOUT_DATES = "taskWithoutDates"
        const val PREF_FILTER_MODE = "filterMode"

        // ----------------------------------------------------------------------------------
        // Calendars and task lists
        const val PREF_ACTIVE_SOURCES = "activeSources"

        // ----------------------------------------------------------------------------------
        // Other
        const val PREF_WIDGET_INSTANCE_NAME = "widgetInstanceName"
        const val TEST_REPLAY_SUFFIX = "Test replay"
        const val PREF_TEXT_SIZE_SCALE = "textSizeScale"
        const val PREF_TIME_FORMAT = "dateFormat" // Legacy value...
        const val PREF_TIME_FORMAT_DEFAULT = "auto"
        const val PREF_LOCK_TIME_ZONE = "lockTimeZone"
        const val PREF_LOCKED_TIME_ZONE_ID = "lockedTimeZoneId"
        const val PREF_START_HOUR_OF_DAY = "startHourOfDay"
        const val PREF_SNAPSHOT_MODE = "snapshotMode"
        const val PREF_RESULTS_STORAGE = "resultsStorage"
        const val PREF_REFRESH_PERIOD_MINUTES = "refreshPeriodMinutes"
        const val PREF_REFRESH_PERIOD_MINUTES_DEFAULT = 10

        fun fromJson(context: Context?, storedSettings: InstanceSettings?, json: JSONObject): InstanceSettings {
            val widgetId = json.optInt(PREF_WIDGET_ID)
            return if (widgetId == 0 || context == null) {
                EMPTY
            } else {
                fromJsonInner(context, storedSettings, json, widgetId)
            }
        }

        private fun fromJsonInner(
            context: Context,
            storedSettings: InstanceSettings?,
            json: JSONObject,
            widgetId: Int
        ): InstanceSettings = try {
            val proposedInstanceName = json.optString(PREF_WIDGET_INSTANCE_NAME).let { prevName ->
                if (storedSettings != null && storedSettings.isForTestsReplaying &&
                    !prevName.endsWith(TEST_REPLAY_SUFFIX)
                ) {
                    (if (prevName.isEmpty()) "" else "$prevName - ") + TEST_REPLAY_SUFFIX
                } else {
                    prevName
                }
            }
            val differentColorsForDark: Boolean = ColorThemeType.canHaveDifferentColorsForDark() && json.has(
                PREF_DARK_THEME
            )
            val eventEntryLayout = if (json.has(PREF_EVENT_ENTRY_LAYOUT)) {
                EventEntryLayout.fromValue(json.getString(PREF_EVENT_ENTRY_LAYOUT))
            } else EventEntryLayout.DEFAULT

            val resultsStorage = if (json.has(PREF_RESULTS_STORAGE)) {
                QueryResultsStorage.fromJson(widgetId, json.getJSONObject(PREF_RESULTS_STORAGE))
            } else EMPTY.resultsStorage

            InstanceSettings(
                contextIn = context,
                widgetId = widgetId,
                proposedInstanceName = proposedInstanceName,
                widgetHeaderDateFormat = if (json.has(PREF_WIDGET_HEADER_DATE_FORMAT)) {
                    DateFormatValue.load(
                        json.getString(PREF_WIDGET_HEADER_DATE_FORMAT), PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT
                    )
                } else if (json.has(PREF_SHOW_DATE_ON_WIDGET_HEADER)) {
                    if (json.getBoolean(PREF_SHOW_DATE_ON_WIDGET_HEADER)) {
                        PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT
                    } else {
                        DateFormatType.HIDDEN.defaultValue
                    }
                } else {
                    PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT
                },
                activeEventSourcesIn = if (json.has(PREF_ACTIVE_SOURCES)) {
                    val jsonArray = json.getJSONArray(PREF_ACTIVE_SOURCES)
                    OrderedEventSource.fromJsonArray(jsonArray)
                } else EMPTY.activeEventSourcesIn,
                eventRange = if (json.has(PREF_EVENT_RANGE)) {
                    json.getInt(PREF_EVENT_RANGE)
                } else EMPTY.eventRange,
                eventsEnded = if (json.has(PREF_EVENTS_ENDED)) {
                    EndedSomeTimeAgo.fromValue(json.getString(PREF_EVENTS_ENDED))
                } else EMPTY.eventsEnded,
                fillAllDayEvents = if (json.has(PREF_FILL_ALL_DAY)) {
                    json.getBoolean(PREF_FILL_ALL_DAY)
                } else EMPTY.fillAllDayEvents,
                hideBasedOnKeywords = if (json.has(PREF_HIDE_BASED_ON_KEYWORDS)) {
                    json.getString(PREF_HIDE_BASED_ON_KEYWORDS)
                } else EMPTY.hideBasedOnKeywords,
                showBasedOnKeywords = if (json.has(PREF_SHOW_BASED_ON_KEYWORDS)) {
                    json.getString(PREF_SHOW_BASED_ON_KEYWORDS)
                } else EMPTY.showBasedOnKeywords,
                defaultColors = ThemeColors.fromJson(
                    context,
                    if (differentColorsForDark) ColorThemeType.LIGHT else ColorThemeType.SINGLE, json
                ),
                darkColors = if (differentColorsForDark) {
                    ThemeColors.fromJson(
                        context, ColorThemeType.DARK, json.getJSONObject(
                            PREF_DARK_THEME
                        )
                    )
                } else {
                    ThemeColors.EMPTY
                },
                textShadow = if (json.has(PREF_TEXT_SHADOW)) {
                    TextShadow.fromValue(json.getString(PREF_TEXT_SHADOW))
                } else EMPTY.textShadow,
                showDaysWithoutEvents = if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
                    json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS)
                } else EMPTY.showDaysWithoutEvents,
                showDayHeaders = if (json.has(PREF_SHOW_DAY_HEADERS)) {
                    json.getBoolean(PREF_SHOW_DAY_HEADERS)
                } else EMPTY.showDayHeaders,
                dayHeaderDateFormat = if (json.has(PREF_DAY_HEADER_DATE_FORMAT)) {
                    DateFormatValue.load(
                        json.getString(PREF_DAY_HEADER_DATE_FORMAT), PREF_DAY_HEADER_DATE_FORMAT_DEFAULT
                    )
                } else EMPTY.dayHeaderDateFormat,
                horizontalLineBelowDayHeader = if (json.has(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER)) {
                    json.getBoolean(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER)
                } else EMPTY.horizontalLineBelowDayHeader,
                showPastEventsUnderOneHeader = if (json.has(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER)) {
                    json.getBoolean(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER)
                } else EMPTY.showPastEventsUnderOneHeader,
                showPastEventsWithDefaultColor = if (json.has(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)) {
                    json.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)
                } else EMPTY.showPastEventsWithDefaultColor,
                showEventIcon = if (json.has(PREF_SHOW_EVENT_ICON)) {
                    json.getBoolean(PREF_SHOW_EVENT_ICON)
                } else EMPTY.showEventIcon,
                eventEntryLayout = eventEntryLayout,
                entryDateFormat = if (json.has(PREF_ENTRY_DATE_FORMAT)) {
                    DateFormatValue.load(
                        json.getString(PREF_ENTRY_DATE_FORMAT), PREF_ENTRY_DATE_FORMAT_DEFAULT
                    )
                } else EMPTY.entryDateFormat,
                showEndTime = if (json.has(PREF_SHOW_END_TIME)) {
                    json.getBoolean(PREF_SHOW_END_TIME)
                } else EMPTY.showEndTime,
                showLocation = if (json.has(PREF_SHOW_LOCATION)) {
                    json.getBoolean(PREF_SHOW_LOCATION)
                } else EMPTY.showLocation,
                showDescription = if (json.has(PREF_SHOW_DESCRIPTION)) {
                    json.getBoolean(PREF_SHOW_DESCRIPTION)
                } else EMPTY.showDescription,
                timeFormat = if (json.has(PREF_TIME_FORMAT)) {
                    json.getString(PREF_TIME_FORMAT)
                } else EMPTY.timeFormat,
                lockedTimeZoneIdIn = if (json.has(PREF_LOCKED_TIME_ZONE_ID)) {
                    json.getString(PREF_LOCKED_TIME_ZONE_ID)
                } else EMPTY.lockedTimeZoneId,
                startHourOfDayIn = if (json.has(PREF_START_HOUR_OF_DAY)) {
                    json.getInt(PREF_START_HOUR_OF_DAY)
                } else EMPTY.startHourOfDay,
                refreshPeriodMinutesIn = if (json.has(PREF_REFRESH_PERIOD_MINUTES)) {
                    json.getInt(PREF_REFRESH_PERIOD_MINUTES)
                } else EMPTY.refreshPeriodMinutes,
                isMultilineTitle = if (json.has(PREF_MULTILINE_TITLE)) {
                    json.getBoolean(PREF_MULTILINE_TITLE)
                } else EMPTY.isMultilineTitle,
                maxLinesTitle = if (json.has(PREF_MAXLINES_TITLE)) {
                    json.getInt(PREF_MAXLINES_TITLE)
                } else EMPTY.maxLinesTitle,
                isMultilineDetails = if (json.has(PREF_MULTILINE_DETAILS)) {
                    json.getBoolean(PREF_MULTILINE_DETAILS)
                } else EMPTY.isMultilineDetails,
                maxLinesDetails = if (json.has(PREF_MAXLINES_DETAILS)) {
                    json.getInt(PREF_MAXLINES_DETAILS)
                } else EMPTY.maxLinesDetails,
                showOnlyClosestInstanceOfRecurringEvent = if (json.has(
                        PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT
                    )
                ) {
                    json.getBoolean(
                        PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT
                    )
                } else EMPTY.showOnlyClosestInstanceOfRecurringEvent,
                hideDuplicates = if (json.has(PREF_HIDE_DUPLICATES)) {
                    json.getBoolean(PREF_HIDE_DUPLICATES)
                } else EMPTY.hideDuplicates,
                allDayEventsPlacement = if (json.has(PREF_ALL_DAY_EVENTS_PLACEMENT)) {
                    AllDayEventsPlacement.fromValue(
                        json.getString(
                            PREF_ALL_DAY_EVENTS_PLACEMENT
                        )
                    )
                } else EMPTY.allDayEventsPlacement,
                taskScheduling = if (json.has(PREF_TASK_SCHEDULING)) {
                    TaskScheduling.fromValue(json.getString(PREF_TASK_SCHEDULING))
                } else EMPTY.taskScheduling,
                taskWithoutDates = if (json.has(PREF_TASK_WITHOUT_DATES)) {
                    TasksWithoutDates.fromValue(json.getString(PREF_TASK_WITHOUT_DATES))
                } else EMPTY.taskWithoutDates,
                filterModeIn = if (json.has(PREF_FILTER_MODE)) {
                    FilterMode.fromValue(json.getString(PREF_FILTER_MODE))
                } else EMPTY.filterMode,
                indicateAlerts = if (json.has(PREF_INDICATE_ALERTS)) {
                    json.getBoolean(PREF_INDICATE_ALERTS)
                } else EMPTY.indicateAlerts,
                indicateRecurring = if (json.has(PREF_INDICATE_RECURRING)) {
                    json.getBoolean(PREF_INDICATE_RECURRING)
                } else EMPTY.indicateRecurring,
                isCompactLayout = if (json.has(PREF_COMPACT_LAYOUT)) {
                    json.getBoolean(PREF_COMPACT_LAYOUT)
                } else EMPTY.isCompactLayout,
                widgetHeaderLayout = if (json.has(PREF_WIDGET_HEADER_LAYOUT)) {
                    WidgetHeaderLayout.fromValue(json.getString(PREF_WIDGET_HEADER_LAYOUT))
                } else EMPTY.widgetHeaderLayout,
                textSizeScale = if (json.has(PREF_TEXT_SIZE_SCALE)) {
                    TextSizeScale.fromPreferenceValue(json.getString(PREF_TEXT_SIZE_SCALE))
                } else EMPTY.textSizeScale,
                dayHeaderAlignment = if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
                    json.getString(PREF_DAY_HEADER_ALIGNMENT)
                } else EMPTY.dayHeaderAlignment,
                resultsStorage = resultsStorage,
                snapshotModeIn = SnapshotMode.fromValue(json.optString(PREF_SNAPSHOT_MODE)),
            )
        } catch (e: JSONException) {
            Log.w(TAG, "setFromJson failed, widgetId:$widgetId\n$json, $e")
            EMPTY
        }

        fun fromApplicationPreferences(
            context: Context,
            widgetId: Int,
            settingsStored: InstanceSettings?
        ): InstanceSettings {
            val resultsStorage = if (settingsStored != null && settingsStored.hasResults) {
                settingsStored.resultsStorage
            } else null

            return synchronized(ApplicationPreferences::class) {
                var darkColors: ThemeColors
                var defaultColors: ThemeColors
                when (ApplicationPreferences.getEditingColorThemeType(context)) {
                    ColorThemeType.DARK -> {
                        darkColors = ThemeColors(context, ColorThemeType.DARK).setFromApplicationPreferences()
                        defaultColors = if (settingsStored == null) darkColors else settingsStored.defaultColors.copy(
                            context,
                            ColorThemeType.LIGHT
                        )
                    }

                    ColorThemeType.LIGHT -> {
                        defaultColors = ThemeColors(context, ColorThemeType.LIGHT).setFromApplicationPreferences()
                        darkColors =
                            if (settingsStored == null) defaultColors else if (settingsStored.darkColors.isEmpty) settingsStored.defaultColors.copy(
                                context, ColorThemeType.DARK
                            ) else settingsStored.darkColors.copy(context, ColorThemeType.DARK)
                    }

                    ColorThemeType.SINGLE -> {
                        darkColors = ThemeColors.EMPTY
                        defaultColors = ThemeColors(context, ColorThemeType.SINGLE).setFromApplicationPreferences()
                    }

                    else -> {
                        darkColors = ThemeColors.EMPTY
                        defaultColors = if (settingsStored == null) darkColors else settingsStored.defaultColors.copy(
                            context,
                            ColorThemeType.SINGLE
                        )
                    }
                }

                InstanceSettings(
                    contextIn = context, widgetId = widgetId,

                    // ----------------------------------------------------------------------------------
                    // Layout
                    isCompactLayout = ApplicationPreferences.isCompactLayout(context),
                    widgetHeaderLayout = ApplicationPreferences.getWidgetHeaderLayout(context),
                    widgetHeaderDateFormat = ApplicationPreferences.getWidgetHeaderDateFormat(context),
                    showDayHeaders = ApplicationPreferences.getShowDayHeaders(context),
                    dayHeaderDateFormat = ApplicationPreferences.getDayHeaderDateFormat(context),
                    showPastEventsUnderOneHeader = ApplicationPreferences.getShowPastEventsUnderOneHeader(context),
                    dayHeaderAlignment = ApplicationPreferences.getString(
                        context, PREF_DAY_HEADER_ALIGNMENT,
                        PREF_DAY_HEADER_ALIGNMENT_DEFAULT
                    ),
                    horizontalLineBelowDayHeader = ApplicationPreferences.getHorizontalLineBelowDayHeader(context),
                    showDaysWithoutEvents = ApplicationPreferences.getShowDaysWithoutEvents(context),
                    eventEntryLayout = ApplicationPreferences.getEventEntryLayout(context),
                    showEventIcon = ApplicationPreferences.getShowEventIcon(context),
                    entryDateFormat = ApplicationPreferences.getEntryDateFormat(context),
                    isMultilineTitle = ApplicationPreferences.isMultilineTitle(context),
                    maxLinesTitle = ApplicationPreferences.getMaxLinesTitle(context),
                    isMultilineDetails = ApplicationPreferences.isMultilineDetails(context),
                    maxLinesDetails = ApplicationPreferences.getMaxLinesDetails(context),

                    // ----------------------------------------------------------------------------------
                    // Colors
                    darkColors = darkColors,
                    defaultColors = defaultColors,
                    textShadow = ApplicationPreferences.getTextShadow(context),

                    // ----------------------------------------------------------------------------------
                    // Event details
                    showEndTime = ApplicationPreferences.getShowEndTime(context),
                    showLocation = ApplicationPreferences.getShowLocation(context),
                    showDescription = ApplicationPreferences.getShowDescription(context),
                    fillAllDayEvents = ApplicationPreferences.getFillAllDayEvents(context),
                    indicateAlerts = ApplicationPreferences.getBoolean(context, PREF_INDICATE_ALERTS, true),
                    indicateRecurring = ApplicationPreferences.getBoolean(context, PREF_INDICATE_RECURRING, false),

                    // ----------------------------------------------------------------------------------
                    // Event filters
                    eventsEnded = ApplicationPreferences.getEventsEnded(context),
                    showPastEventsWithDefaultColor = ApplicationPreferences.getShowPastEventsWithDefaultColor(context),
                    eventRange = ApplicationPreferences.getEventRange(context),
                    hideBasedOnKeywords = ApplicationPreferences.getHideBasedOnKeywords(context),
                    showBasedOnKeywords = ApplicationPreferences.getShowBasedOnKeywords(context),
                    showOnlyClosestInstanceOfRecurringEvent = ApplicationPreferences.getShowOnlyClosestInstanceOfRecurringEvent(
                        context
                    ),
                    hideDuplicates = ApplicationPreferences.getHideDuplicates(context),
                    allDayEventsPlacement = ApplicationPreferences.getAllDayEventsPlacement(context),
                    taskScheduling = ApplicationPreferences.getTaskScheduling(context),
                    taskWithoutDates = ApplicationPreferences.getTasksWithoutDates(context),
                    filterModeIn = ApplicationPreferences.getFilterMode(context),

                    // ----------------------------------------------------------------------------------
                    // Calendars and task lists
                    activeEventSourcesIn = ApplicationPreferences.getActiveEventSources(context),

                    // ----------------------------------------------------------------------------------
                    // Other
                    proposedInstanceName = ApplicationPreferences.getString(
                        context, PREF_WIDGET_INSTANCE_NAME,
                        ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")
                    ),
                    textSizeScale = TextSizeScale.fromPreferenceValue(
                        ApplicationPreferences.getString(context, PREF_TEXT_SIZE_SCALE, "")
                    ),
                    timeFormat = ApplicationPreferences.getTimeFormat(context),
                    lockedTimeZoneIdIn = ApplicationPreferences.getLockedTimeZoneId(context),
                    startHourOfDayIn = ApplicationPreferences.getIntStoredAsString(context, PREF_START_HOUR_OF_DAY, 0),
                    snapshotModeIn = ApplicationPreferences.getSnapshotMode(context),
                    resultsStorage = resultsStorage,
                    refreshPeriodMinutesIn = ApplicationPreferences.getRefreshPeriodMinutes(context),
                )
            }
        }

        private fun getStorageKey(widgetId: Int): String {
            return "instanceSettings$widgetId"
        }

        fun isDarkThemeOn(context: Context?): Boolean {
            val configuration = context!!.applicationContext.resources.configuration
            val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
