package org.andstatus.todoagenda.prefs.colors;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.widget.TimeSection;

import static org.andstatus.todoagenda.prefs.colors.ThemeColors.TRANSPARENT_BLACK;

public enum BackgroundColorPref {
    WIDGET_HEADER("widgetHeaderBackgroundColor", TRANSPARENT_BLACK,
            R.string.widget_header_background_color_title, TimeSection.ALL),
    PAST_EVENTS("pastEventsBackgroundColor", 0xBF78782C,
            R.string.appearance_past_events_background_color_title, TimeSection.PAST),
    TODAYS_EVENTS("todaysEventsBackgroundColor", 0xDAFFFFFF,
            R.string.todays_events_background_color_title, TimeSection.TODAY),
    FUTURE_EVENTS("backgroundColor", 0x80000000,
            R.string.appearance_background_color_title, TimeSection.FUTURE);

    public final String colorPreferenceName;
    @ColorInt public final int defaultColor;
    @StringRes public final int colorTitleResId;

    public final TimeSection timeSection;

    BackgroundColorPref(String colorPreferenceName, @ColorInt int defaultColor, int colorTitleResId,
                  TimeSection timeSection) {
        this.colorPreferenceName = colorPreferenceName;
        this.defaultColor = defaultColor;
        this.colorTitleResId = colorTitleResId;
        this.timeSection = timeSection;
    }

    public static BackgroundColorPref forTimeSection(TimeSection timeSection) {
        for (BackgroundColorPref pref: values()) {
            if (pref.timeSection == timeSection) return pref;
        }
        return WIDGET_HEADER;
    }
}
