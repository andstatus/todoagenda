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
import org.andstatus.todoagenda.util.InstanceId
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.widget.Alignment
import org.andstatus.todoagenda.widget.EventEntryLayout
import org.andstatus.todoagenda.widget.TextShadow
import org.andstatus.todoagenda.widget.WidgetHeaderLayout
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors

/**
 * Loaded settings of one Widget
 *
 * @author yvolk@yurivolkov.com
 */
data class InstanceSettings(
    private val contextIn: Context?,
    val widgetId: Int,
    val instanceId: Long = InstanceId.next(),

    var isCompactLayout: Boolean = false,
    var widgetHeaderLayout: WidgetHeaderLayout = WidgetHeaderLayout.defaultValue,
    var widgetHeaderDateFormat: DateFormatValue = PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT,
    var showDayHeaders: Boolean = true,
    var dayHeaderDateFormat: DateFormatValue = PREF_DAY_HEADER_DATE_FORMAT_DEFAULT,
    var showPastEventsUnderOneHeader: Boolean = false,
    var dayHeaderAlignment: String = PREF_DAY_HEADER_ALIGNMENT_DEFAULT,
    var horizontalLineBelowDayHeader: Boolean = false,
    var showDaysWithoutEvents: Boolean = false,
    var eventEntryLayout: EventEntryLayout = EventEntryLayout.DEFAULT,
    var showEventIcon: Boolean = true,
    var entryDateFormat: DateFormatValue = PREF_ENTRY_DATE_FORMAT_DEFAULT,
    var isMultilineTitle: Boolean = PREF_MULTILINE_TITLE_DEFAULT,
    var maxLinesTitle: Int = PREF_MAXLINES_TITLE_DEFAULT,
    var isMultilineDetails: Boolean = PREF_MULTILINE_DETAILS_DEFAULT,
    var maxLinesDetails: Int = PREF_MAXLINES_DETAILS_DEFAULT,

    // ----------------------------------------------------------------------------------
    // Colors
    private var defaultColors: ThemeColors = if (contextIn == null) {
        ThemeColors.EMPTY
    } else {
        ThemeColors(contextIn, ColorThemeType.SINGLE)
    },
    private var darkColors: ThemeColors = ThemeColors.EMPTY,
    var textShadow: TextShadow = TextShadow.NO_SHADOW,

    // ----------------------------------------------------------------------------------
    // ,,,
    var showEndTime: Boolean = PREF_SHOW_END_TIME_DEFAULT,
    var showLocation: Boolean = PREF_SHOW_LOCATION_DEFAULT,
    var showDescription: Boolean = PREF_SHOW_DESCRIPTION_DEFAULT,
    var fillAllDayEvents: Boolean = PREF_FILL_ALL_DAY_DEFAULT,
    var indicateAlerts: Boolean = true,
    var indicateRecurring: Boolean = false,
    var eventsEnded: EndedSomeTimeAgo? = EndedSomeTimeAgo.NONE,
    var showPastEventsWithDefaultColor: Boolean = false,
    var eventRange: Int = PREF_EVENT_RANGE_DEFAULT.toInt(),
    var hideBasedOnKeywords: String? = "",
    var showBasedOnKeywords: String? = "",
    var showOnlyClosestInstanceOfRecurringEvent: Boolean = false,
    var hideDuplicates: Boolean = false,
    var allDayEventsPlacement: AllDayEventsPlacement = AllDayEventsPlacement.defaultValue,
    var taskScheduling: TaskScheduling = TaskScheduling.defaultValue,
    var taskWithoutDates: TasksWithoutDates = TasksWithoutDates.defaultValue,
    private var filterModeInner: FilterMode = FilterMode.defaultValue,
    val clock: MyClock = MyClock(),
    private val activeEventSourcesInner: MutableList<OrderedEventSource> = CopyOnWriteArrayList(),
    private val proposedInstanceName: String? = null, // TODO: delete
    val widgetInstanceName: String = if (contextIn == null) "(empty)" else AllSettings.uniqueInstanceName(
        contextIn,
        widgetId,
        proposedInstanceName
    ),
    var textSizeScale: TextSizeScale = TextSizeScale.MEDIUM,
    var timeFormat: String = PREF_TIME_FORMAT_DEFAULT,
    private var refreshPeriodMinutesIn: Int = PREF_REFRESH_PERIOD_MINUTES_DEFAULT,
    var resultsStorage: QueryResultsStorage? = null
) {
    val context: Context get() = contextIn ?: throw IllegalStateException("Context is null")

    val filterMode: FilterMode
        get() = if (filterModeInner == FilterMode.NORMAL_FILTER &&
            clock.snapshotMode.isSnapshotMode
        ) FilterMode.DEBUG_FILTER else filterModeInner

    val activeEventSources: MutableList<OrderedEventSource>
        get() = activeEventSourcesInner.also {
            if (it.isEmpty()) it.addAll(EventProviderType.availableSources)
        }

    var refreshPeriodMinutes: Int
        get() = refreshPeriodMinutesIn
        set(value) {
            if (value > 0) {
                refreshPeriodMinutesIn = value
            }
        }

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
        put(PREF_WIDGET_HEADER_DATE_FORMAT, widgetHeaderDateFormat.save())
        put(PREF_WIDGET_INSTANCE_NAME, widgetInstanceName)
        put(PREF_ACTIVE_SOURCES, OrderedEventSource.toJsonArray(activeEventSources))
        put(PREF_EVENT_RANGE, eventRange)
        put(PREF_EVENTS_ENDED, eventsEnded!!.save())
        put(PREF_FILL_ALL_DAY, fillAllDayEvents)
        put(PREF_HIDE_BASED_ON_KEYWORDS, hideBasedOnKeywords)
        put(PREF_SHOW_BASED_ON_KEYWORDS, showBasedOnKeywords)
        defaultColors.toJson(this)
        if (!darkColors.isEmpty) {
            put(PREF_DARK_THEME, darkColors.toJson(JSONObject()))
        }
        put(PREF_TEXT_SHADOW, textShadow.value)
        put(PREF_SHOW_DAYS_WITHOUT_EVENTS, showDaysWithoutEvents)
        put(PREF_SHOW_DAY_HEADERS, showDayHeaders)
        put(PREF_DAY_HEADER_DATE_FORMAT, dayHeaderDateFormat.save())
        put(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER, horizontalLineBelowDayHeader)
        put(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER, showPastEventsUnderOneHeader)
        put(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, showPastEventsWithDefaultColor)
        put(PREF_SHOW_EVENT_ICON, showEventIcon)
        put(PREF_ENTRY_DATE_FORMAT, entryDateFormat.save())
        put(PREF_SHOW_END_TIME, showEndTime)
        put(PREF_SHOW_LOCATION, showLocation)
        put(PREF_SHOW_DESCRIPTION, showDescription)
        put(PREF_TIME_FORMAT, timeFormat)
        put(PREF_LOCKED_TIME_ZONE_ID, clock.lockedTimeZoneId)
        put(PREF_SNAPSHOT_MODE, clock.snapshotMode.value)
        put(PREF_REFRESH_PERIOD_MINUTES, refreshPeriodMinutes)
        put(PREF_EVENT_ENTRY_LAYOUT, eventEntryLayout.value)
        put(PREF_MULTILINE_TITLE, isMultilineTitle)
        put(PREF_MAXLINES_TITLE, maxLinesTitle)
        put(PREF_MULTILINE_DETAILS, isMultilineDetails)
        put(PREF_MAXLINES_DETAILS, maxLinesDetails)
        put(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, showOnlyClosestInstanceOfRecurringEvent)
        put(PREF_HIDE_DUPLICATES, hideDuplicates)
        put(PREF_ALL_DAY_EVENTS_PLACEMENT, allDayEventsPlacement.value)
        put(PREF_TASK_SCHEDULING, taskScheduling.value)
        put(PREF_TASK_WITHOUT_DATES, taskWithoutDates.value)
        put(PREF_FILTER_MODE, filterMode.value)
        put(PREF_INDICATE_ALERTS, indicateAlerts)
        put(PREF_INDICATE_RECURRING, indicateRecurring)
        put(PREF_COMPACT_LAYOUT, isCompactLayout)
        put(PREF_WIDGET_HEADER_LAYOUT, widgetHeaderLayout.value)
        put(PREF_TEXT_SIZE_SCALE, textSizeScale.preferenceValue)
        put(PREF_DAY_HEADER_ALIGNMENT, dayHeaderAlignment)
        if (resultsStorage != null) {
            put(PREF_RESULTS_STORAGE, resultsStorage!!.toJson(context, widgetId, false))
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
        get() = (if (eventRange > 0) clock.now().plusDays(eventRange) else clock.now().withTimeAtStartOfDay()
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
        get() = clock.snapshotMode.isSnapshotMode
    val isLiveMode: Boolean
        get() = clock.snapshotMode.isLiveMode

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
            activeEventSources.add(orderedSource)
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

    fun hasResults(): Boolean {
        return resultsStorage != null && !resultsStorage!!.results.isEmpty()
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

        @Deprecated("")
        private val PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT = "showNumberOfDaysToEvent" // till v 4.0
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
        const val PREF_SNAPSHOT_MODE = "snapshotMode"
        const val PREF_REFRESH_PERIOD_MINUTES = "refreshPeriodMinutes"
        const val PREF_REFRESH_PERIOD_MINUTES_DEFAULT = 10
        private const val PREF_RESULTS_STORAGE = "resultsStorage"

        fun fromJson(context: Context?, storedSettings: InstanceSettings?, json: JSONObject): InstanceSettings {
            val widgetId = json.optInt(PREF_WIDGET_ID)
            if (widgetId == 0) {
                return EMPTY
            }
            val proposedInstanceName = json.optString(PREF_WIDGET_INSTANCE_NAME).let { prevName ->
                if (storedSettings != null && storedSettings.isForTestsReplaying &&
                    !prevName.endsWith(TEST_REPLAY_SUFFIX)
                ) {
                    (if (prevName.isEmpty()) "" else "$prevName - ") + TEST_REPLAY_SUFFIX
                } else {
                    prevName
                }
            }
            return InstanceSettings(
                contextIn = context,
                widgetId = widgetId,
                proposedInstanceName = proposedInstanceName
            ).apply {
                try {
                    if (json.has(PREF_WIDGET_HEADER_DATE_FORMAT)) {
                        widgetHeaderDateFormat = DateFormatValue.load(
                            json.getString(PREF_WIDGET_HEADER_DATE_FORMAT), PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT
                        )
                    } else if (json.has(PREF_SHOW_DATE_ON_WIDGET_HEADER)) {
                        widgetHeaderDateFormat = if (json.getBoolean(PREF_SHOW_DATE_ON_WIDGET_HEADER)) {
                            PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT
                        } else {
                            DateFormatType.HIDDEN.defaultValue
                        }
                    }
                    if (json.has(PREF_ACTIVE_SOURCES)) {
                        val jsonArray = json.getJSONArray(PREF_ACTIVE_SOURCES)
                        activeEventSources.addAll(OrderedEventSource.fromJsonArray(jsonArray))
                    }
                    if (json.has(PREF_EVENT_RANGE)) {
                        eventRange = json.getInt(PREF_EVENT_RANGE)
                    }
                    if (json.has(PREF_EVENTS_ENDED)) {
                        eventsEnded = EndedSomeTimeAgo.fromValue(json.getString(PREF_EVENTS_ENDED))
                    }
                    if (json.has(PREF_FILL_ALL_DAY)) {
                        fillAllDayEvents = json.getBoolean(PREF_FILL_ALL_DAY)
                    }
                    if (json.has(PREF_HIDE_BASED_ON_KEYWORDS)) {
                        hideBasedOnKeywords = json.getString(PREF_HIDE_BASED_ON_KEYWORDS)
                    }
                    if (json.has(PREF_SHOW_BASED_ON_KEYWORDS)) {
                        showBasedOnKeywords = json.getString(PREF_SHOW_BASED_ON_KEYWORDS)
                    }
                    val differentColorsForDark = ColorThemeType.canHaveDifferentColorsForDark() && json.has(
                        PREF_DARK_THEME
                    )
                    defaultColors = ThemeColors.fromJson(
                        context,
                        if (differentColorsForDark) ColorThemeType.LIGHT else ColorThemeType.SINGLE, json
                    )
                    darkColors = if (differentColorsForDark) {
                        ThemeColors.fromJson(
                            context, ColorThemeType.DARK, json.getJSONObject(
                                PREF_DARK_THEME
                            )
                        )
                    } else {
                        ThemeColors.EMPTY
                    }
                    if (json.has(PREF_TEXT_SHADOW)) {
                        textShadow = TextShadow.fromValue(json.getString(PREF_TEXT_SHADOW))
                    }
                    if (json.has(PREF_SHOW_DAYS_WITHOUT_EVENTS)) {
                        showDaysWithoutEvents = json.getBoolean(PREF_SHOW_DAYS_WITHOUT_EVENTS)
                    }
                    if (json.has(PREF_SHOW_DAY_HEADERS)) {
                        showDayHeaders = json.getBoolean(PREF_SHOW_DAY_HEADERS)
                    }
                    if (json.has(PREF_DAY_HEADER_DATE_FORMAT)) {
                        dayHeaderDateFormat = DateFormatValue.load(
                            json.getString(PREF_DAY_HEADER_DATE_FORMAT), PREF_DAY_HEADER_DATE_FORMAT_DEFAULT
                        )
                    }
                    if (json.has(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER)) {
                        horizontalLineBelowDayHeader = json.getBoolean(PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER)
                    }
                    if (json.has(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER)) {
                        showPastEventsUnderOneHeader = json.getBoolean(PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER)
                    }
                    if (json.has(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)) {
                        showPastEventsWithDefaultColor = json.getBoolean(PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR)
                    }
                    if (json.has(PREF_SHOW_EVENT_ICON)) {
                        showEventIcon = json.getBoolean(PREF_SHOW_EVENT_ICON)
                    }
                    if (json.has(PREF_EVENT_ENTRY_LAYOUT)) {
                        eventEntryLayout = EventEntryLayout.fromValue(json.getString(PREF_EVENT_ENTRY_LAYOUT))
                    }
                    if (json.has(PREF_ENTRY_DATE_FORMAT)) {
                        entryDateFormat = DateFormatValue.load(
                            json.getString(PREF_ENTRY_DATE_FORMAT), PREF_ENTRY_DATE_FORMAT_DEFAULT
                        )
                    } else if (json.has(PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT)) {
                        entryDateFormat = (if (json.getBoolean(PREF_SHOW_NUMBER_OF_DAYS_TO_EVENT) &&
                            eventEntryLayout == EventEntryLayout.ONE_LINE
                        ) DateFormatType.NUMBER_OF_DAYS else DateFormatType.HIDDEN)
                            .defaultValue
                    }
                    if (json.has(PREF_SHOW_END_TIME)) {
                        showEndTime = json.getBoolean(PREF_SHOW_END_TIME)
                    }
                    if (json.has(PREF_SHOW_LOCATION)) {
                        showLocation = json.getBoolean(PREF_SHOW_LOCATION)
                    }
                    if (json.has(PREF_SHOW_DESCRIPTION)) {
                        showDescription = json.getBoolean(PREF_SHOW_DESCRIPTION)
                    }
                    if (json.has(PREF_TIME_FORMAT)) {
                        timeFormat = json.getString(PREF_TIME_FORMAT)
                    }
                    if (json.has(PREF_LOCKED_TIME_ZONE_ID)) {
                        clock.lockedTimeZoneId = json.getString(PREF_LOCKED_TIME_ZONE_ID)
                    }
                    if (json.has(PREF_REFRESH_PERIOD_MINUTES)) {
                        refreshPeriodMinutes = json.getInt(PREF_REFRESH_PERIOD_MINUTES)
                    }
                    if (json.has(PREF_MULTILINE_TITLE)) {
                        isMultilineTitle = json.getBoolean(PREF_MULTILINE_TITLE)
                    }
                    if (json.has(PREF_MAXLINES_TITLE)) {
                        maxLinesTitle = json.getInt(PREF_MAXLINES_TITLE)
                    }
                    if (json.has(PREF_MULTILINE_DETAILS)) {
                        isMultilineDetails = json.getBoolean(PREF_MULTILINE_DETAILS)
                    }
                    if (json.has(PREF_MAXLINES_DETAILS)) {
                        maxLinesDetails = json.getInt(PREF_MAXLINES_DETAILS)
                    }
                    if (json.has(PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT)) {
                        showOnlyClosestInstanceOfRecurringEvent = json.getBoolean(
                            PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT
                        )
                    }
                    if (json.has(PREF_HIDE_DUPLICATES)) {
                        hideDuplicates = json.getBoolean(PREF_HIDE_DUPLICATES)
                    }
                    if (json.has(PREF_ALL_DAY_EVENTS_PLACEMENT)) {
                        allDayEventsPlacement = AllDayEventsPlacement.fromValue(
                            json.getString(
                                PREF_ALL_DAY_EVENTS_PLACEMENT
                            )
                        )
                    }
                    if (json.has(PREF_TASK_SCHEDULING)) {
                        taskScheduling = TaskScheduling.fromValue(json.getString(PREF_TASK_SCHEDULING))
                    }
                    if (json.has(PREF_TASK_WITHOUT_DATES)) {
                        taskWithoutDates = TasksWithoutDates.fromValue(json.getString(PREF_TASK_WITHOUT_DATES))
                    }
                    if (json.has(PREF_FILTER_MODE)) {
                        filterModeInner = FilterMode.fromValue(json.getString(PREF_FILTER_MODE))
                    }
                    if (json.has(PREF_INDICATE_ALERTS)) {
                        indicateAlerts = json.getBoolean(PREF_INDICATE_ALERTS)
                    }
                    if (json.has(PREF_INDICATE_RECURRING)) {
                        indicateRecurring = json.getBoolean(PREF_INDICATE_RECURRING)
                    }
                    if (json.has(PREF_COMPACT_LAYOUT)) {
                        isCompactLayout = json.getBoolean(PREF_COMPACT_LAYOUT)
                    }
                    if (json.has(PREF_WIDGET_HEADER_LAYOUT)) {
                        widgetHeaderLayout = WidgetHeaderLayout.fromValue(json.getString(PREF_WIDGET_HEADER_LAYOUT))
                    }
                    if (json.has(PREF_TEXT_SIZE_SCALE)) {
                        textSizeScale = TextSizeScale.fromPreferenceValue(json.getString(PREF_TEXT_SIZE_SCALE))
                    }
                    if (json.has(PREF_DAY_HEADER_ALIGNMENT)) {
                        dayHeaderAlignment = json.getString(PREF_DAY_HEADER_ALIGNMENT)
                    }
                    if (json.has(PREF_RESULTS_STORAGE)) {
                        resultsStorage =
                            QueryResultsStorage.fromJson(widgetId, json.getJSONObject(PREF_RESULTS_STORAGE))
                    }
                    clock.setSnapshotMode(SnapshotMode.fromValue(json.optString(PREF_SNAPSHOT_MODE)), this)
                } catch (e: JSONException) {
                    Log.w(TAG, "setFromJson failed, widgetId:$widgetId\n$json, $e")
                }
            }
        }

        fun fromApplicationPreferences(
            context: Context,
            widgetId: Int,
            settingsStored: InstanceSettings?
        ): InstanceSettings = synchronized(ApplicationPreferences::class) {
            InstanceSettings(
                contextIn = context, widgetId = widgetId,
                proposedInstanceName = ApplicationPreferences.getString(
                    context, PREF_WIDGET_INSTANCE_NAME,
                    ApplicationPreferences.getString(context, PREF_WIDGET_INSTANCE_NAME, "")
                ),
            ).apply {
                widgetHeaderDateFormat = ApplicationPreferences.getWidgetHeaderDateFormat(context)
                activeEventSources.addAll(ApplicationPreferences.getActiveEventSources(context))
                eventRange = ApplicationPreferences.getEventRange(context)
                eventsEnded = ApplicationPreferences.getEventsEnded(context)
                fillAllDayEvents = ApplicationPreferences.getFillAllDayEvents(context)
                hideBasedOnKeywords = ApplicationPreferences.getHideBasedOnKeywords(context)
                showBasedOnKeywords = ApplicationPreferences.getShowBasedOnKeywords(context)
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
                textShadow = ApplicationPreferences.getTextShadow(context)

                showDaysWithoutEvents = ApplicationPreferences.getShowDaysWithoutEvents(context)
                showDayHeaders = ApplicationPreferences.getShowDayHeaders(context)
                dayHeaderDateFormat = ApplicationPreferences.getDayHeaderDateFormat(context)
                horizontalLineBelowDayHeader = ApplicationPreferences.getHorizontalLineBelowDayHeader(context)
                showPastEventsUnderOneHeader = ApplicationPreferences.getShowPastEventsUnderOneHeader(context)
                showPastEventsWithDefaultColor = ApplicationPreferences.getShowPastEventsWithDefaultColor(context)
                showEventIcon = ApplicationPreferences.getShowEventIcon(context)
                entryDateFormat = ApplicationPreferences.getEntryDateFormat(context)
                showEndTime = ApplicationPreferences.getShowEndTime(context)
                showLocation = ApplicationPreferences.getShowLocation(context)
                showDescription = ApplicationPreferences.getShowDescription(context)
                timeFormat = ApplicationPreferences.getTimeFormat(context)
                refreshPeriodMinutes = ApplicationPreferences.getRefreshPeriodMinutes(context)
                eventEntryLayout = ApplicationPreferences.getEventEntryLayout(context)
                isMultilineTitle = ApplicationPreferences.isMultilineTitle(context)
                maxLinesTitle = ApplicationPreferences.getMaxLinesTitle(context)
                isMultilineDetails = ApplicationPreferences.isMultilineDetails(context)
                maxLinesDetails = ApplicationPreferences.getMaxLinesDetails(context)
                showOnlyClosestInstanceOfRecurringEvent =
                    ApplicationPreferences.getShowOnlyClosestInstanceOfRecurringEvent(
                        context
                    )
                hideDuplicates = ApplicationPreferences.getHideDuplicates(context)
                allDayEventsPlacement = ApplicationPreferences.getAllDayEventsPlacement(context)
                taskScheduling = ApplicationPreferences.getTaskScheduling(context)
                taskWithoutDates = ApplicationPreferences.getTasksWithoutDates(context)
                filterModeInner = ApplicationPreferences.getFilterMode(context)
                indicateAlerts = ApplicationPreferences.getBoolean(context, PREF_INDICATE_ALERTS, true)
                indicateRecurring = ApplicationPreferences.getBoolean(context, PREF_INDICATE_RECURRING, false)
                isCompactLayout = ApplicationPreferences.isCompactLayout(context)
                widgetHeaderLayout = ApplicationPreferences.getWidgetHeaderLayout(context)
                textSizeScale = TextSizeScale.fromPreferenceValue(
                    ApplicationPreferences.getString(context, PREF_TEXT_SIZE_SCALE, "")
                )
                dayHeaderAlignment = ApplicationPreferences.getString(
                    context, PREF_DAY_HEADER_ALIGNMENT,
                    PREF_DAY_HEADER_ALIGNMENT_DEFAULT
                )
                clock.lockedTimeZoneId = ApplicationPreferences.getLockedTimeZoneId(context)
                if (settingsStored != null && settingsStored.hasResults()) {
                    resultsStorage = settingsStored.resultsStorage
                }
                clock.setSnapshotMode(ApplicationPreferences.getSnapshotMode(context), this)
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
