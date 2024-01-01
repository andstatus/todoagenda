package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.widget.WidgetEntryPosition

/**
 * Where to show tasks without start and due dates
 * See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
enum class TasksWithoutDates(
    val value: String,
    val widgetEntryPosition: WidgetEntryPosition,
    @field:StringRes val valueResId: Int
) {
    END_OF_LIST("end_of_list", WidgetEntryPosition.END_OF_LIST, R.string.tasks_wo_dates_end_of_list),
    END_OF_TODAY("end_of_today", WidgetEntryPosition.END_OF_TODAY, R.string.tasks_wo_dates_end_of_today),
    HIDE("hide", WidgetEntryPosition.HIDDEN, R.string.tasks_wo_dates_hide);

    companion object {
        val defaultValue = END_OF_LIST
        fun fromValue(value: String?): TasksWithoutDates {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
