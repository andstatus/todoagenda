package org.andstatus.todoagenda.prefs.colors;

import android.app.Activity;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * See https://github.com/andstatus/todoagenda/issues/47
 * @author yvolk@yurivolkov.com
 */
public enum TextColorSource {
    AUTO("auto", R.string.text_color_source_auto, R.string.text_color_source_auto_desc),
    SHADING("shading", R.string.text_color_source_shading, R.string.text_color_source_shading_desc),
    COLORS("colors", R.string.text_color_source_color, R.string.text_color_source_color_desc);

    public final static TextColorSource defaultValue = AUTO;

    public final String value;
    @StringRes
    public final int titleResId;
    @StringRes
    public final int summaryResId;

    TextColorSource(String value, int titleResId, int summaryResId) {
        this.value = value;
        this.titleResId = titleResId;
        this.summaryResId = summaryResId;
    }

    public static TextColorSource fromValue(String value) {
        for (TextColorSource item : TextColorSource.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }

    public TextColorSource setTitle(Activity activity) {
        if (activity != null) {
            activity.setTitle(titleResId);
        }
        return this;
    }

}