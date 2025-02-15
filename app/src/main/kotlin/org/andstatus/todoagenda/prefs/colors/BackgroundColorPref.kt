package org.andstatus.todoagenda.prefs.colors

import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.TimeSection

enum class BackgroundColorPref(
    val colorPreferenceName: String,
    @field:ColorInt @param:ColorInt val defaultColor: Int,
    @field:StringRes val colorTitleResId: Int,
    val timeSection: TimeSection
) {
    WIDGET_HEADER(
        "widgetHeaderBackgroundColor", ThemeColors.Companion.TRANSPARENT_BLACK,
        R.string.widget_header_background_color_title, TimeSection.ALL
    ),
    PAST_EVENTS(
        "pastEventsBackgroundColor", -0x408787d4,
        R.string.appearance_past_events_background_color_title, TimeSection.PAST
    ),
    TODAYS_EVENTS(
        "todaysEventsBackgroundColor", -0x25000001,
        R.string.todays_events_background_color_title, TimeSection.TODAY
    ),
    FUTURE_EVENTS(
        "backgroundColor", -0x80000000,
        R.string.appearance_background_color_title, TimeSection.FUTURE
    );

    companion object {
        fun forTimeSection(timeSection: TimeSection?): BackgroundColorPref {
            for (pref in entries) {
                if (pref.timeSection == timeSection) return pref
            }
            return WIDGET_HEADER
        }
    }
}
