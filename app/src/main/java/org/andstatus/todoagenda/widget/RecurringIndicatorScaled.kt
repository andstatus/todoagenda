package org.andstatus.todoagenda.widget;

import androidx.annotation.LayoutRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
 */
public enum RecurringIndicatorScaled {
    VERY_SMALL(R.id.event_entry_indicator_recurring_very_small),
    SMALL(R.id.event_entry_indicator_recurring_small),
    MEDIUM(R.id.event_entry_indicator_recurring);

    @LayoutRes
    public final int indicatorResId;

    RecurringIndicatorScaled(int indicatorResId) {
        this.indicatorResId = indicatorResId;
    }
}
