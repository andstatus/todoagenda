package org.andstatus.todoagenda.prefs.colors

import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.TimeSection
import org.andstatus.todoagenda.widget.WidgetEntry

enum class TextColorPref(
    val shadingPreferenceName: String,
    val defaultShading: Shading,
    @field:StringRes val shadingTitleResId: Int,
    val colorPreferenceName: String,
    @field:ColorInt @param:ColorInt val defaultColor: Int,
    @field:StringRes val colorTitleResId: Int,
    @field:AttrRes val colorAttrId: Int,
    val backgroundColorPref: BackgroundColorPref,
    val dependsOnDayHeader: Boolean,
    val timeSection: TimeSection,
) {
    WIDGET_HEADER(
        shadingPreferenceName = "headerTheme",
        defaultShading = Shading.LIGHT,
        shadingTitleResId = R.string.appearance_header_theme_title,
        colorPreferenceName = "widgetHeaderTextColor",
        defaultColor = -0x65000001,
        colorTitleResId = R.string.widget_header_text_color,
        colorAttrId = R.attr.header,
        backgroundColorPref = BackgroundColorPref.WIDGET_HEADER,
        dependsOnDayHeader = false,
        timeSection = TimeSection.ALL,
    ),
    DAY_HEADER_PAST(
        "dayHeaderThemePast",
        Shading.LIGHT,
        R.string.day_header_theme_title,
        "dayHeaderTextColorPast",
        -0x333334,
        R.string.day_header_text_color,
        R.attr.dayHeaderTitle,
        BackgroundColorPref.PAST_EVENTS,
        true,
        TimeSection.PAST,
    ),
    EVENT_PAST(
        "entryThemePast",
        Shading.WHITE,
        R.string.appearance_entries_theme_title,
        "eventTextColorPast",
        -0x1,
        R.string.event_text_color,
        R.attr.eventEntryTitle,
        BackgroundColorPref.PAST_EVENTS,
        false,
        TimeSection.PAST,
    ),
    DAY_HEADER_TODAY(
        "dayHeaderTheme",
        Shading.DARK,
        R.string.day_header_theme_title,
        "dayHeaderTextColorToday",
        -0x888889,
        R.string.day_header_text_color,
        R.attr.dayHeaderTitle,
        BackgroundColorPref.TODAYS_EVENTS,
        true,
        TimeSection.TODAY,
    ),
    EVENT_ONGOING(
        "entryThemeOngoing",
        Shading.BLACK,
        R.string.appearance_entries_theme_title,
        "eventTextColorOngoing",
        -0x1000000,
        R.string.event_text_color,
        R.attr.eventEntryTitle,
        BackgroundColorPref.ONGOING_EVENTS,
        false,
        TimeSection.ONGOING,
    ),
    EVENT_TODAY(
        "entryTheme",
        Shading.BLACK,
        R.string.appearance_entries_theme_title,
        "eventTextColorToday",
        -0x1000000,
        R.string.event_text_color,
        R.attr.eventEntryTitle,
        BackgroundColorPref.TODAYS_EVENTS,
        false,
        TimeSection.TODAY,
    ),
    DAY_HEADER_FUTURE(
        "dayHeaderThemeFuture",
        Shading.LIGHT,
        R.string.day_header_theme_title,
        "dayHeaderTextColorFuture",
        -0x333334,
        R.string.day_header_text_color,
        R.attr.dayHeaderTitle,
        BackgroundColorPref.FUTURE_EVENTS,
        true,
        TimeSection.FUTURE,
    ),
    EVENT_FUTURE(
        "entryThemeFuture",
        Shading.WHITE,
        R.string.appearance_entries_theme_title,
        "eventTextColorFuture",
        -0x1,
        R.string.event_text_color,
        R.attr.eventEntryTitle,
        BackgroundColorPref.FUTURE_EVENTS,
        false,
        TimeSection.FUTURE,
    ),
    ;

    fun getShadingForBackground(backgroundShading: Shading): Shading {
        when (this) {
            DAY_HEADER_PAST, DAY_HEADER_TODAY, DAY_HEADER_FUTURE -> {
                when (backgroundShading) {
                    Shading.BLACK, Shading.DARK -> return Shading.LIGHT
                    Shading.LIGHT, Shading.WHITE -> return Shading.DARK
                }
                when (backgroundShading) {
                    Shading.BLACK -> return Shading.LIGHT
                    Shading.DARK -> return Shading.WHITE
                    Shading.LIGHT -> return Shading.BLACK
                    Shading.WHITE -> return Shading.DARK
                }
            }

            else ->
                when (backgroundShading) {
                    Shading.BLACK -> return Shading.LIGHT
                    Shading.DARK -> return Shading.WHITE
                    Shading.LIGHT -> return Shading.BLACK
                    Shading.WHITE -> return Shading.DARK
                }
        }
        throw IllegalStateException("getShadingForBackground for $this and background $backgroundShading")
    }

    companion object {
        fun forDayHeader(entry: WidgetEntry): TextColorPref = entry.timeSection.select(DAY_HEADER_PAST, DAY_HEADER_TODAY, DAY_HEADER_TODAY, DAY_HEADER_FUTURE)

        fun forDetails(entry: WidgetEntry): TextColorPref = entry.timeSection.select(DAY_HEADER_PAST, EVENT_ONGOING, DAY_HEADER_TODAY, DAY_HEADER_FUTURE)

        fun forTitle(entry: WidgetEntry): TextColorPref = entry.timeSection.select(EVENT_PAST, EVENT_ONGOING, EVENT_TODAY, EVENT_FUTURE)
    }
}
