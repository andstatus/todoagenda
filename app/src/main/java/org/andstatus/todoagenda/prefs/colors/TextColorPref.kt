package org.andstatus.todoagenda.prefs.colors;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.widget.TimeSection;
import org.andstatus.todoagenda.widget.WidgetEntry;

public enum TextColorPref {

    WIDGET_HEADER("headerTheme", Shading.LIGHT, R.string.appearance_header_theme_title,
            "widgetHeaderTextColor", 0x9AFFFFFF, R.string.widget_header_text_color,
            R.attr.header, BackgroundColorPref.WIDGET_HEADER, false, TimeSection.ALL),
    DAY_HEADER_PAST("dayHeaderThemePast", Shading.LIGHT, R.string.day_header_theme_title,
            "dayHeaderTextColorPast", 0xFFCCCCCC, R.string.day_header_text_color,
            R.attr.dayHeaderTitle, BackgroundColorPref.PAST_EVENTS, true, TimeSection.PAST),
    EVENT_PAST("entryThemePast", Shading.WHITE, R.string.appearance_entries_theme_title,
            "eventTextColorPast", 0xFFFFFFFF, R.string.event_text_color,
            R.attr.eventEntryTitle, BackgroundColorPref.PAST_EVENTS, false, TimeSection.PAST),
    DAY_HEADER_TODAY("dayHeaderTheme", Shading.DARK, R.string.day_header_theme_title,
            "dayHeaderTextColorToday", 0xFF777777, R.string.day_header_text_color,
            R.attr.dayHeaderTitle, BackgroundColorPref.TODAYS_EVENTS, true, TimeSection.TODAY),
    EVENT_TODAY("entryTheme", Shading.BLACK, R.string.appearance_entries_theme_title,
            "eventTextColorToday", 0xFF000000, R.string.event_text_color,
            R.attr.eventEntryTitle, BackgroundColorPref.TODAYS_EVENTS, false, TimeSection.TODAY),
    DAY_HEADER_FUTURE("dayHeaderThemeFuture", Shading.LIGHT, R.string.day_header_theme_title,
            "dayHeaderTextColorFuture", 0xFFCCCCCC, R.string.day_header_text_color,
            R.attr.dayHeaderTitle, BackgroundColorPref.FUTURE_EVENTS, true, TimeSection.FUTURE),
    EVENT_FUTURE("entryThemeFuture", Shading.WHITE, R.string.appearance_entries_theme_title,
            "eventTextColorFuture", 0xFFFFFFFF, R.string.event_text_color,
            R.attr.eventEntryTitle, BackgroundColorPref.FUTURE_EVENTS, false, TimeSection.FUTURE);

    public final String shadingPreferenceName;
    public final Shading defaultShading;
    @StringRes public final int shadingTitleResId;

    public final String colorPreferenceName;
    @ColorInt public final int defaultColor;
    @StringRes public final int colorTitleResId;
    @AttrRes public final int colorAttrId;

    public final BackgroundColorPref backgroundColorPref;
    public final boolean dependsOnDayHeader;
    public final TimeSection timeSection;

    TextColorPref(String shadingPreferenceName, Shading defaultShading, int shadingTitleResId,
                  String colorPreferenceName, @ColorInt int defaultColor, int colorTitleResId,
                  int colorAttrId, BackgroundColorPref backgroundColorPref, boolean dependsOnDayHeader, TimeSection timeSection) {
        this.shadingPreferenceName = shadingPreferenceName;
        this.defaultShading = defaultShading;
        this.colorPreferenceName = colorPreferenceName;
        this.defaultColor = defaultColor;
        this.shadingTitleResId = shadingTitleResId;
        this.colorTitleResId = colorTitleResId;
        this.colorAttrId = colorAttrId;
        this.backgroundColorPref = backgroundColorPref;
        this.dependsOnDayHeader = dependsOnDayHeader;
        this.timeSection = timeSection;
    }

    public Shading getShadingForBackground(Shading backgroundShading) {
        switch (this) {
            case DAY_HEADER_PAST:
            case DAY_HEADER_TODAY:
            case DAY_HEADER_FUTURE:
                switch (backgroundShading) {
                    case BLACK:
                    case DARK:
                        return Shading.LIGHT;
                    case LIGHT:
                    case WHITE:
                        return Shading.DARK;
                }
            default:
                switch (backgroundShading) {
                    case BLACK:
                        return Shading.LIGHT;
                    case DARK:
                        return Shading.WHITE;
                    case LIGHT:
                        return Shading.BLACK;
                    case WHITE:
                        return Shading.DARK;
                }
        }
        throw new IllegalStateException("getShadingForBackground for " + this + " and background " + backgroundShading);
    }

    public static TextColorPref forDayHeader(WidgetEntry<?> entry) {
        return entry.timeSection.select(DAY_HEADER_PAST, DAY_HEADER_TODAY, DAY_HEADER_FUTURE);
    }

    public static TextColorPref forDetails(WidgetEntry<?> entry) {
        return entry.timeSection.select(DAY_HEADER_PAST, DAY_HEADER_TODAY, DAY_HEADER_FUTURE);
    }

    public static TextColorPref forTitle(WidgetEntry<?> entry) {
        return entry.timeSection.select(EVENT_PAST, EVENT_TODAY, EVENT_FUTURE);
    }

}
