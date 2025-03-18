package org.andstatus.todoagenda.prefs.colors

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.graphics.toColorInt
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.TimeSection
import org.andstatus.todoagenda.prefs.colors.BackgroundColorPref.entries

enum class BackgroundColorPref(
    val colorPreferenceName: String,
    @field:ColorInt @param:ColorInt val defaultColor: Int,
    @field:StringRes val colorTitleResId: Int,
    val timeSection: TimeSection,
) {
    WIDGET_HEADER(
        "widgetHeaderBackgroundColor",
        ThemeColors.Companion.TRANSPARENT_BLACK,
        R.string.appearance_background_color_title,
        TimeSection.ALL,
    ),
    PAST_EVENTS(
        "pastEventsBackgroundColor",
        "#BF78782C".toColorInt(),
        R.string.appearance_background_color_title,
        TimeSection.PAST,
    ),
    ONGOING_EVENTS(
        "ongoingEventsBackgroundColor",
        "#F0FFFFFF".toColorInt(),
        R.string.appearance_background_color_title,
        TimeSection.ONGOING,
    ),
    CURRENT_TIME(
        "currentTimeLineColor",
        "#FFF44336".toColorInt(),
        R.string.current_time_line_color_title,
        TimeSection.ALL,
    ),
    TODAYS_EVENTS(
        "todaysEventsBackgroundColor",
        "#DAFFFFFF".toColorInt(),
        R.string.appearance_background_color_title,
        TimeSection.TODAY,
    ),
    FUTURE_EVENTS(
        "backgroundColor",
        "#80000000".toColorInt(),
        R.string.appearance_background_color_title,
        TimeSection.FUTURE,
    ),
    ;

    companion object {
        fun forTimeSection(timeSection: TimeSection?): BackgroundColorPref {
            for (pref in entries) {
                if (pref.timeSection == timeSection) return pref
            }
            return WIDGET_HEADER
        }
    }
}
