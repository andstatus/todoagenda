package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.widget.WidgetEntryPosition

/**
 * See https://github.com/andstatus/todoagenda/issues/48
 * @author yvolk@yurivolkov.com
 */
enum class AllDayEventsPlacement(
    val value: String,
    @field:StringRes val valueResId: Int,
    val widgetEntryPosition: WidgetEntryPosition
) {
    TOP_DAY("top_day", R.string.all_day_events_placement_top_of_the_days_events, WidgetEntryPosition.START_OF_DAY),
    BOTTOM_DAY(
        "bottom_day",
        R.string.all_day_events_placement_bottom_of_the_days_events,
        WidgetEntryPosition.END_OF_DAY
    );

    companion object {
        val defaultValue = TOP_DAY
        fun fromValue(value: String?): AllDayEventsPlacement {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
