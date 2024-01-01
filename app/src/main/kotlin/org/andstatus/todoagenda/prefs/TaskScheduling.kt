package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * What date gets main attention for a task
 * See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
enum class TaskScheduling(val value: String, @field:StringRes val valueResId: Int) {
    DATE_DUE("date_due", R.string.task_scheduling_date_due),
    DATE_STARTED("date_started", R.string.task_scheduling_date_started);

    companion object {
        val defaultValue = DATE_DUE
        fun fromValue(value: String?): TaskScheduling {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
