package org.andstatus.todoagenda.prefs

import org.andstatus.todoagenda.widget.AlarmIndicatorScaled
import org.andstatus.todoagenda.widget.RecurringIndicatorScaled

/**
 * @author yvolk@yurivolkov.com
 */
enum class TextSizeScale(
    val preferenceValue: String, val scaleValue: Float,
    val alarmIndicator: AlarmIndicatorScaled, val recurringIndicator: RecurringIndicatorScaled
) {
    VERY_SMALL(
        "0.6", 0.6f,
        AlarmIndicatorScaled.VERY_SMALL, RecurringIndicatorScaled.VERY_SMALL
    ),
    SMALL(
        "0.8", 0.8f,
        AlarmIndicatorScaled.SMALL, RecurringIndicatorScaled.SMALL
    ),
    MEDIUM(
        "1.0", 1.0f,
        AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM
    ),
    LARGE(
        "1.25", 1.25f,
        AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM
    ),
    VERY_LARGE(
        "1.75", 1.75f,
        AlarmIndicatorScaled.MEDIUM, RecurringIndicatorScaled.MEDIUM
    );

    companion object {
        fun fromPreferenceValue(preferenceValue: String?): TextSizeScale {
            for (item in entries) {
                if (item.preferenceValue == preferenceValue) {
                    return item
                }
            }
            return MEDIUM
        }
    }
}
