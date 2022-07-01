package org.andstatus.todoagenda.prefs;

import androidx.annoation.StringRes;

import org.andstatus.todoagenda.R;

/**
 * See https://github.com/andstatus/todoagenda/issues/104
 * @author felix.stupp+github@banananet.work
 */
public enum HideSubtasks {
    SHOW_ALL("show_all", R.string.pref_hide_subtasks_show_all),
    HIDE_ALL("hide_all", R.string.pref_hide_subtasks_hide_all);

    public final static HideSubtasks defaultValue = SHOW_ALL;
    
    public final String value;
    @StringRes
    public final int valueResId;

    HideSubtasks(String value, int valueResId) {
        this.value = value;
        this.valueResId = valueResId;
    }

    public static HideSubtasks fromValue(String value) {
        for (HideSubtasks item : HideSubtasks.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}
