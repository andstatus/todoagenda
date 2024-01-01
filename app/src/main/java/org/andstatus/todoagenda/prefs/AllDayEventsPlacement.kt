package org.andstatus.todoagenda.prefs;

import androidx.annotation.StringRes;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.widget.WidgetEntryPosition;

/**
 * See https://github.com/andstatus/todoagenda/issues/48
 * @author yvolk@yurivolkov.com
 */
public enum AllDayEventsPlacement {
    TOP_DAY("top_day", R.string.all_day_events_placement_top_of_the_days_events, WidgetEntryPosition.START_OF_DAY),
    BOTTOM_DAY("bottom_day", R.string.all_day_events_placement_bottom_of_the_days_events, WidgetEntryPosition.END_OF_DAY);

    public final static AllDayEventsPlacement defaultValue = TOP_DAY;

    public final String value;
    @StringRes
    public final int valueResId;
    public final WidgetEntryPosition widgetEntryPosition;

    AllDayEventsPlacement(String value, int valueResId, WidgetEntryPosition widgetEntryPosition) {
        this.value = value;
        this.valueResId = valueResId;
        this.widgetEntryPosition = widgetEntryPosition;
    }

    public static AllDayEventsPlacement fromValue(String value) {
        for (AllDayEventsPlacement item : AllDayEventsPlacement.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return defaultValue;
    }
}