package org.andstatus.todoagenda.widget

import androidx.annotation.LayoutRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class RecurringIndicatorScaled(@field:LayoutRes val indicatorResId: Int) {
    VERY_SMALL(R.id.event_entry_indicator_recurring_very_small),
    SMALL(R.id.event_entry_indicator_recurring_small),
    MEDIUM(R.id.event_entry_indicator_recurring)
}
