package org.andstatus.todoagenda.prefs.colors;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

import static org.andstatus.todoagenda.prefs.InstanceSettings.isDarkThemeOn;

/**
 * See https://github.com/andstatus/todoagenda/issues/48
 * @author yvolk@yurivolkov.com
 */
public enum ColorThemeType {
    SINGLE("single", R.string.appearance_group_color_title),
    DARK("dark", R.string.colors_dark),
    LIGHT("light", R.string.colors_light),
    NONE("none", R.string.no_title);

    private final static ColorThemeType defaultValue = SINGLE;

    public final String value;
    @StringRes
    public final int titleResId;

    ColorThemeType(String value, int titleResId) {
        this.value = value;
        this.titleResId = titleResId;
    }

    public boolean isValid() {
        return this != NONE;
    }

    public static ColorThemeType fromValue(String value) {
        for (ColorThemeType item : ColorThemeType.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }

    /** See https://developer.android.com/guide/topics/ui/look-and-feel/darktheme */
    public static boolean canHaveDifferentColorsForDark() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public ColorThemeType fromEditor(Context context, boolean differentColorsForDark) {
        if (differentColorsForDark && canHaveDifferentColorsForDark()) {
            if (this == ColorThemeType.DARK || this == ColorThemeType.SINGLE && isDarkThemeOn(context)) {
                return ColorThemeType.DARK;
            } else {
                return ColorThemeType.LIGHT;
            }
        } else {
            if (this == ColorThemeType.LIGHT || this == ColorThemeType.SINGLE) {
                return ColorThemeType.SINGLE;
            } else {
                return ColorThemeType.NONE;
            }
        }
    }

    public ColorThemeType setTitle(Activity activity) {
        if (activity != null) {
            activity.setTitle(titleResId);
        }
        return this;
    }

}