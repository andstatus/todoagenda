package org.andstatus.todoagenda.prefs

import android.content.Context
import android.text.TextUtils
import androidx.preference.PreferenceManager
import org.andstatus.todoagenda.layout.EventEntryLayout
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.layout.WidgetHeaderLayout
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_MAX_NUMBER_OF_EVENTS
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_TEXT_SHADOW
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_TEXT_SHADOW_DEFAULT
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_TIME_UNTIL_BACKGROUND_SOURCE
import org.andstatus.todoagenda.prefs.colors.BackgroundColorPref
import org.andstatus.todoagenda.prefs.colors.ColorThemeType
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.prefs.colors.TextColorSource
import org.andstatus.todoagenda.prefs.colors.ThemeColors
import org.andstatus.todoagenda.prefs.colors.TimeUntilBackgroundSource
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue
import org.andstatus.todoagenda.util.StringUtil

object ApplicationPreferences {
    const val PREF_DIFFERENT_COLORS_FOR_DARK = "differentColorsForDark"
    private const val PREF_COLOR_THEME_TYPE = "colorThemeType"
    private const val PREF_ASK_FOR_PERMISSIONS = "askForPermissions"

    fun fromInstanceSettings(
        context: Context,
        widgetId: Int,
    ) {
        synchronized(ApplicationPreferences::class) {
            val settings = AllSettings.instanceFromId(context, widgetId)
            setWidgetId(context, if (widgetId == 0) settings.widgetId else widgetId)
            //
            // ----------------------------------------------------------------------------------
            // Layout
            setBoolean(context, InstanceSettings.PREF_COMPACT_LAYOUT, settings.isCompactLayout)
            setString(context, InstanceSettings.PREF_WIDGET_HEADER_LAYOUT, settings.widgetHeaderLayout.value)
            setDateFormat(
                context,
                InstanceSettings.PREF_WIDGET_HEADER_DATE_FORMAT,
                settings.widgetHeaderDateFormat,
            )
            setShowDayHeaders(context, settings.showDayHeaders)
            setDateFormat(context, InstanceSettings.PREF_DAY_HEADER_DATE_FORMAT, settings.dayHeaderDateFormat)
            setShowPastEventsUnderOneHeader(context, settings.showPastEventsUnderOneHeader)
            setString(context, InstanceSettings.PREF_DAY_HEADER_ALIGNMENT, settings.dayHeaderAlignment)
            setHorizontalLineBelowDayHeader(context, settings.horizontalLineBelowDayHeader)
            setShowDaysWithoutEvents(context, settings.showDaysWithoutEvents)
            setString(context, InstanceSettings.PREF_EVENT_ENTRY_LAYOUT, settings.eventEntryLayout.value)
            setShowEventIcon(context, settings.showEventIcon)
            setDateFormat(context, InstanceSettings.PREF_ENTRY_DATE_FORMAT, settings.entryDateFormat)
            setBoolean(context, InstanceSettings.PREF_MULTILINE_TITLE, settings.isMultilineTitle)
            setString(context, InstanceSettings.PREF_MAXLINES_TITLE, settings.maxLinesTitle.toString())
            setBoolean(context, InstanceSettings.PREF_MULTILINE_DETAILS, settings.isMultilineDetails)
            setString(context, InstanceSettings.PREF_MAXLINES_DETAILS, settings.maxLinesDetails.toString())
            setBoolean(context, InstanceSettings.PREF_SHOW_CURRENT_TIME_LINE, settings.showCurrentTimeLine)
            setBoolean(context, InstanceSettings.PREF_SHOW_TIME_UNTIL_TAG, settings.showTimeUntilTag)
            setString(context, InstanceSettings.PREF_LAST_ENTRY_APPEARANCE, settings.lastEntryAppearance.value)
            //
            // ----------------------------------------------------------------------------------
            // Colors
            val colors = settings.colors()
            setString(context, PREF_COLOR_THEME_TYPE, colors.colorThemeType.value)
            setBoolean(context, PREF_DIFFERENT_COLORS_FOR_DARK, colors.colorThemeType != ColorThemeType.SINGLE)
            for (pref in BackgroundColorPref.entries) {
                setInt(context, pref.colorPreferenceName, colors.getBackground(pref).color)
            }
            setString(context, ThemeColors.PREF_TEXT_COLOR_SOURCE, colors.textColorSource!!.value)
            for (pref in TextColorPref.entries) {
                setString(context, pref.shadingPreferenceName, colors.getTextShadingStored(pref).shading.themeName)
                setInt(context, pref.colorPreferenceName, colors.getTextColorStored(pref).color)
            }
            setString(context, PREF_TEXT_SHADOW, settings.textShadow.value)
            setString(context, PREF_TIME_UNTIL_BACKGROUND_SOURCE, settings.timeUntilBackgroundSource.value)
            //
            // ----------------------------------------------------------------------------------
            // Event details
            setBoolean(context, InstanceSettings.PREF_SHOW_DAY_XY, settings.showDayXY)
            setBoolean(context, InstanceSettings.PREF_SHOW_END_TIME, settings.showEndTime)
            setBoolean(context, InstanceSettings.PREF_SHOW_LOCATION, settings.showLocation)
            setBoolean(context, InstanceSettings.PREF_SHOW_DESCRIPTION, settings.showDescription)
            setFillAllDayEvents(context, settings.fillAllDayEvents)
            setBoolean(context, InstanceSettings.PREF_INDICATE_ALERTS, settings.indicateAlerts)
            setBoolean(context, InstanceSettings.PREF_INDICATE_RECURRING, settings.indicateRecurring)
            //
            // ----------------------------------------------------------------------------------
            // Event filters
            setEventsEnded(context, settings.eventsEnded)
            setShowPastEventsWithDefaultColor(context, settings.showPastEventsWithDefaultColor)
            setEventRange(context, settings.eventRange)
            setHideBasedOnKeywords(context, settings.hideBasedOnKeywords)
            setShowBasedOnKeywords(context, settings.showBasedOnKeywords)
            setBoolean(
                context,
                InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT,
                settings
                    .showOnlyClosestInstanceOfRecurringEvent,
            )
            setHideDuplicates(context, settings.hideDuplicates)
            setString(context, PREF_MAX_NUMBER_OF_EVENTS, settings.maxNumberOfEvents.limitToString())
            setAllDayEventsPlacement(context, settings.allDayEventsPlacement)
            setString(context, InstanceSettings.PREF_TASK_SCHEDULING, settings.taskScheduling.value)
            setString(context, InstanceSettings.PREF_TASK_WITHOUT_DATES, settings.taskWithoutDates.value)
            setString(context, InstanceSettings.PREF_FILTER_MODE, settings.filterMode.value)
            //
            // ----------------------------------------------------------------------------------
            // Calendars and task lists
            setActiveEventSources(context, settings.activeEventSources)
            //
            // ----------------------------------------------------------------------------------
            // Other
            setString(context, InstanceSettings.PREF_WIDGET_INSTANCE_NAME, settings.widgetInstanceName)
            setString(
                context,
                InstanceSettings.PREF_WIDGET_HEADER_BUTTONS_SCALE,
                settings.widgetHeaderButtonsScale.preferenceValue,
            )
            setString(context, InstanceSettings.PREF_TEXT_SIZE_SCALE, settings.textSizeScale.preferenceValue)
            setString(context, InstanceSettings.PREF_TIME_FORMAT, settings.timeFormat)
            setLockedTimeZoneId(context, settings.lockedTimeZoneId)
            setString(context, InstanceSettings.PREF_START_HOUR_OF_DAY, settings.startHourOfDay.toString())
            setString(context, InstanceSettings.PREF_SNAPSHOT_MODE, settings.snapshotMode.value)
            setRefreshPeriodMinutes(context, settings.refreshPeriodMinutes)
        }
    }

    fun save(
        context: Context?,
        wigdetId: Int,
    ) {
        if (context != null && wigdetId != 0 && wigdetId == getWidgetId(context)) {
            AllSettings.saveFromApplicationPreferences(context, wigdetId)
        }
    }

    fun isAskForPermissions(context: Context): Boolean = getBoolean(context, PREF_ASK_FOR_PERMISSIONS, true)

    fun setAskForPermissions(
        context: Context,
        value: Boolean,
    ) {
        setBoolean(context, PREF_ASK_FOR_PERMISSIONS, value)
    }

    fun getWidgetId(context: Context?): Int = if (context == null) 0 else getInt(context, InstanceSettings.PREF_WIDGET_ID, 0)

    fun setWidgetId(
        context: Context?,
        value: Int,
    ) {
        setInt(context, InstanceSettings.PREF_WIDGET_ID, value)
    }

    fun setWidgetHeaderDateFormat(
        context: Context?,
        dateFormatValue: DateFormatValue?,
    ) {
        setDateFormat(context, InstanceSettings.PREF_WIDGET_HEADER_DATE_FORMAT, dateFormatValue)
    }

    fun noTaskSources(context: Context): Boolean {
        val sources = getActiveEventSources(context)
        for (orderedSource in sources) {
            if (!orderedSource.source.providerType.isCalendar) return false
        }
        return true
    }

    fun getActiveEventSources(context: Context): MutableList<OrderedEventSource> =
        OrderedEventSource.fromJsonString(
            getString(context, InstanceSettings.PREF_ACTIVE_SOURCES),
        )

    fun setActiveEventSources(
        context: Context?,
        sources: List<OrderedEventSource>?,
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        val editor = prefs.edit()
        editor.putString(
            InstanceSettings.PREF_ACTIVE_SOURCES,
            OrderedEventSource.toJsonString(sources),
        )
        editor.apply()
    }

    fun getEventRange(context: Context): Int =
        parseIntSafe(
            getString(
                context,
                InstanceSettings.PREF_EVENT_RANGE,
                InstanceSettings.PREF_EVENT_RANGE_DEFAULT,
            ),
        )

    fun setEventRange(
        context: Context?,
        value: Int,
    ) {
        setString(context, InstanceSettings.PREF_EVENT_RANGE, value.toString())
    }

    fun getEventsEnded(context: Context): EndedSomeTimeAgo =
        EndedSomeTimeAgo.fromValue(
            getString(context, InstanceSettings.PREF_EVENTS_ENDED),
        )

    fun setEventsEnded(
        context: Context?,
        value: EndedSomeTimeAgo?,
    ) {
        setString(context, InstanceSettings.PREF_EVENTS_ENDED, value!!.save())
    }

    fun getFillAllDayEvents(context: Context?): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_FILL_ALL_DAY,
            InstanceSettings.PREF_FILL_ALL_DAY_DEFAULT,
        )

    private fun setFillAllDayEvents(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_FILL_ALL_DAY, value)
    }

    fun getHideBasedOnKeywords(context: Context): String = getString(context, InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS)

    private fun setHideBasedOnKeywords(
        context: Context?,
        value: String?,
    ) {
        setString(context, InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS, value)
    }

    fun getShowBasedOnKeywords(context: Context): String = getString(context, InstanceSettings.PREF_SHOW_BASED_ON_KEYWORDS, "")

    private fun setShowBasedOnKeywords(
        context: Context?,
        value: String?,
    ) {
        setString(context, InstanceSettings.PREF_SHOW_BASED_ON_KEYWORDS, value)
    }

    fun areDifferentColorsForDark(context: Context?): Boolean = getBoolean(context, PREF_DIFFERENT_COLORS_FOR_DARK, false)

    fun getEditingColorThemeType(context: Context): ColorThemeType =
        getColorThemeType(context).fromEditor(context, areDifferentColorsForDark(context))

    fun getColorThemeType(context: Context): ColorThemeType = ColorThemeType.fromValue(getString(context, PREF_COLOR_THEME_TYPE))

    fun getBackgroundColor(
        pref: BackgroundColorPref,
        context: Context?,
    ): Int = getInt(context, pref.colorPreferenceName, pref.defaultColor)

    fun getTextColorSource(context: Context): TextColorSource =
        TextColorSource.fromValue(
            getString(
                context,
                ThemeColors.PREF_TEXT_COLOR_SOURCE,
                TextColorSource.defaultEntry.value,
            ),
        )

    fun getTextShadow(context: Context): TextShadow =
        TextShadow.fromValue(
            getString(
                context,
                PREF_TEXT_SHADOW,
                PREF_TEXT_SHADOW_DEFAULT,
            ),
        )

    fun getTimeUntilBackgroundSource(context: Context): TimeUntilBackgroundSource =
        TimeUntilBackgroundSource.fromValue(
            getString(
                context,
                PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                TimeUntilBackgroundSource.defaultEntry.value,
            ),
        )

    fun getHorizontalLineBelowDayHeader(context: Context?): Boolean =
        getBoolean(context, InstanceSettings.PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER, false)

    private fun setHorizontalLineBelowDayHeader(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_HORIZONTAL_LINE_BELOW_DAY_HEADER, value)
    }

    fun getShowDaysWithoutEvents(context: Context?): Boolean = getBoolean(context, InstanceSettings.PREF_SHOW_DAYS_WITHOUT_EVENTS, false)

    private fun setShowDaysWithoutEvents(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_DAYS_WITHOUT_EVENTS, value)
    }

    fun getShowDayHeaders(context: Context?): Boolean = getBoolean(context, InstanceSettings.PREF_SHOW_DAY_HEADERS, true)

    private fun setShowDayHeaders(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_DAY_HEADERS, value)
    }

    fun getShowPastEventsUnderOneHeader(context: Context?): Boolean =
        getBoolean(context, InstanceSettings.PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER, false)

    private fun setShowPastEventsUnderOneHeader(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_PAST_EVENTS_UNDER_ONE_HEADER, value)
    }

    fun getShowEventIcon(context: Context?): Boolean = getBoolean(context, InstanceSettings.PREF_SHOW_EVENT_ICON, false)

    fun setShowEventIcon(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_EVENT_ICON, value)
    }

    fun getShowPastEventsWithDefaultColor(context: Context?): Boolean =
        getBoolean(context, InstanceSettings.PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, false)

    fun setShowPastEventsWithDefaultColor(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_PAST_EVENTS_WITH_DEFAULT_COLOR, value)
    }

    fun getShowEndTime(context: Context?): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_SHOW_END_TIME,
            InstanceSettings.PREF_SHOW_END_TIME_DEFAULT,
        )

    fun getShowLocation(context: Context?): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_SHOW_LOCATION,
            InstanceSettings.PREF_SHOW_LOCATION_DEFAULT,
        )

    fun getShowDescription(context: Context?): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_SHOW_DESCRIPTION,
            InstanceSettings.PREF_SHOW_DESCRIPTION_DEFAULT,
        )

    fun getDayHeaderDateFormat(context: Context): DateFormatValue =
        getDateFormat(
            context,
            InstanceSettings.PREF_DAY_HEADER_DATE_FORMAT,
            InstanceSettings.PREF_DAY_HEADER_DATE_FORMAT_DEFAULT,
        )

    fun getWidgetHeaderDateFormat(context: Context): DateFormatValue =
        getDateFormat(
            context,
            InstanceSettings.PREF_WIDGET_HEADER_DATE_FORMAT,
            InstanceSettings.PREF_WIDGET_HEADER_DATE_FORMAT_DEFAULT,
        )

    fun getEntryDateFormat(context: Context): DateFormatValue =
        getDateFormat(
            context,
            InstanceSettings.PREF_ENTRY_DATE_FORMAT,
            InstanceSettings.PREF_ENTRY_DATE_FORMAT_DEFAULT,
        )

    fun setDateFormat(
        context: Context?,
        key: String?,
        value: DateFormatValue?,
    ) {
        setString(context, key, value!!.save())
    }

    fun getDateFormat(
        context: Context,
        key: String?,
        defaultValue: DateFormatValue?,
    ): DateFormatValue = DateFormatValue.load(getString(context, key), defaultValue!!)

    fun getTimeFormat(context: Context): String =
        getString(
            context,
            InstanceSettings.PREF_TIME_FORMAT,
            InstanceSettings.PREF_TIME_FORMAT_DEFAULT,
        )

    fun getLockedTimeZoneId(context: Context): String = getString(context, InstanceSettings.PREF_LOCKED_TIME_ZONE_ID, "")

    fun setLockedTimeZoneId(
        context: Context?,
        value: String?,
    ) {
        setString(context, InstanceSettings.PREF_LOCKED_TIME_ZONE_ID, value)
    }

    fun getSnapshotMode(context: Context): SnapshotMode =
        SnapshotMode.fromValue(getString(context, InstanceSettings.PREF_SNAPSHOT_MODE, ""))

    fun setRefreshPeriodMinutes(
        context: Context?,
        value: Int,
    ) {
        setString(
            context,
            InstanceSettings.PREF_REFRESH_PERIOD_MINUTES,
            Integer.toString(if (value > 0) value else InstanceSettings.PREF_REFRESH_PERIOD_MINUTES_DEFAULT),
        )
    }

    fun getRefreshPeriodMinutes(context: Context): Int {
        val stored =
            getIntStoredAsString(
                context,
                InstanceSettings.PREF_REFRESH_PERIOD_MINUTES,
                InstanceSettings.PREF_REFRESH_PERIOD_MINUTES_DEFAULT,
            )
        return if (stored > 0) stored else InstanceSettings.PREF_REFRESH_PERIOD_MINUTES_DEFAULT
    }

    fun isTimeZoneLocked(context: Context): Boolean = !TextUtils.isEmpty(getLockedTimeZoneId(context))

    fun getEventEntryLayout(context: Context): EventEntryLayout =
        EventEntryLayout.fromValue(
            getString(
                context,
                InstanceSettings.PREF_EVENT_ENTRY_LAYOUT,
                "",
            ),
        )

    fun isMultilineTitle(context: Context): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_MULTILINE_TITLE,
            InstanceSettings.PREF_MULTILINE_TITLE_DEFAULT,
        )

    fun getMaxLinesTitle(context: Context): Int =
        getIntStoredAsString(
            context,
            InstanceSettings.PREF_MAXLINES_TITLE,
            InstanceSettings.PREF_MAXLINES_TITLE_DEFAULT,
        )

    fun isMultilineDetails(context: Context): Boolean =
        getBoolean(
            context,
            InstanceSettings.PREF_MULTILINE_DETAILS,
            InstanceSettings.PREF_MULTILINE_DETAILS_DEFAULT,
        )

    fun getMaxLinesDetails(context: Context): Int =
        getIntStoredAsString(
            context,
            InstanceSettings.PREF_MAXLINES_DETAILS,
            InstanceSettings.PREF_MAXLINES_DETAILS_DEFAULT,
        )

    fun getLastEntryAppearance(context: Context): LastEntryAppearance =
        LastEntryAppearance.fromValue(getString(context, InstanceSettings.PREF_LAST_ENTRY_APPEARANCE))

    fun getShowOnlyClosestInstanceOfRecurringEvent(context: Context?): Boolean =
        getBoolean(context, InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, false)

    fun setShowOnlyClosestInstanceOfRecurringEvent(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_SHOW_ONLY_CLOSEST_INSTANCE_OF_RECURRING_EVENT, value)
    }

    fun getHideDuplicates(context: Context?): Boolean = getBoolean(context, InstanceSettings.PREF_HIDE_DUPLICATES, false)

    fun getMaxNumberOfEvents(context: Context): Int =
        getIntStoredAsString(context, PREF_MAX_NUMBER_OF_EVENTS, InstanceSettings.EMPTY.maxNumberOfEvents)

    fun setHideDuplicates(
        context: Context?,
        value: Boolean,
    ) {
        setBoolean(context, InstanceSettings.PREF_HIDE_DUPLICATES, value)
    }

    fun setAllDayEventsPlacement(
        context: Context?,
        value: AllDayEventsPlacement,
    ) {
        setString(context, InstanceSettings.PREF_ALL_DAY_EVENTS_PLACEMENT, value.value)
    }

    fun getAllDayEventsPlacement(context: Context): AllDayEventsPlacement =
        AllDayEventsPlacement.fromValue(
            getString(context, InstanceSettings.PREF_ALL_DAY_EVENTS_PLACEMENT),
        )

    fun getTaskScheduling(context: Context): TaskScheduling =
        TaskScheduling.fromValue(
            getString(context, InstanceSettings.PREF_TASK_SCHEDULING),
        )

    fun getTasksWithoutDates(context: Context): TasksWithoutDates =
        TasksWithoutDates.fromValue(
            getString(
                context,
                InstanceSettings.PREF_TASK_WITHOUT_DATES,
            ),
        )

    fun getFilterMode(context: Context): FilterMode = FilterMode.fromValue(getString(context, InstanceSettings.PREF_FILTER_MODE))

    private fun setString(
        context: Context?,
        key: String?,
        value: String?,
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getIntStoredAsString(
        context: Context,
        key: String?,
        defaultValue: Int,
    ): Int =
        try {
            val stringValue = getString(context, key)
            if (TextUtils.isEmpty(stringValue)) defaultValue else stringValue.toInt()
        } catch (_: Exception) {
            defaultValue
        }

    fun getString(
        context: Context,
        key: String?,
        defaultValue: String = "",
    ): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return (
            if (prefs == null) {
                defaultValue
            } else {
                prefs.getString(key, defaultValue)
            }
        ) ?: ""
    }

    private fun setBoolean(
        context: Context?,
        key: String,
        value: Boolean,
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(
        context: Context?,
        key: String?,
        defaultValue: Boolean,
    ): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        return prefs?.getBoolean(key, defaultValue) ?: defaultValue
    }

    private fun setInt(
        context: Context?,
        key: String?,
        value: Int,
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(
        context: Context?,
        key: String?,
        defaultValue: Int,
    ): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        return prefs?.getInt(key, defaultValue) ?: defaultValue
    }

    fun getWidgetInstanceName(context: Context): String = getString(context, InstanceSettings.PREF_WIDGET_INSTANCE_NAME, "")

    fun isCompactLayout(context: Context?): Boolean = getBoolean(context, InstanceSettings.PREF_COMPACT_LAYOUT, false)

    fun getWidgetHeaderLayout(context: Context): WidgetHeaderLayout =
        WidgetHeaderLayout.fromValue(
            getString(context, InstanceSettings.PREF_WIDGET_HEADER_LAYOUT, ""),
        )

    fun noPastEvents(context: Context?): Boolean =
        context != null &&
            !getShowPastEventsWithDefaultColor(context) &&
            getEventsEnded(context) === EndedSomeTimeAgo.NONE &&
            noTaskSources(context)

    fun parseIntSafe(value: String?): Int =
        if (StringUtil.isEmpty(value)) {
            0
        } else {
            try {
                value!!.toInt()
            } catch (_: Exception) {
                0
            }
        }

    fun Int.limitToString(): String = if (this < 1) "" else toString()
}
