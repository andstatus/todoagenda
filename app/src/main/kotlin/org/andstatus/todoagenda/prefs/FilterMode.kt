package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class FilterMode(val value: String, @field:StringRes val valueResId: Int) {
    NORMAL_FILTER("normal", R.string.filter_mode_normal),

    /** Include filtering that is usually done at the content provider query level  */
    DEBUG_FILTER("debug", R.string.filter_mode_debug),
    NO_FILTERING("no_filtering", R.string.filter_mode_no_filtering);

    companion object {
        val defaultValue = NORMAL_FILTER
        fun fromValue(value: String?): FilterMode {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
