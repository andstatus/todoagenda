package org.andstatus.todoagenda.widget

import androidx.annotation.LayoutRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class AlarmIndicatorScaled(@field:LayoutRes val indicatorResId: Int) {
    VERY_SMALL(R.id.event_entry_indicator_alarm_very_small),
    SMALL(R.id.event_entry_indicator_alarm_small),
    MEDIUM(R.id.event_entry_indicator_alarm)
}
