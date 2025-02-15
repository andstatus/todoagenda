package org.andstatus.todoagenda.layout

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class WidgetHeaderLayout(
    val widgetLayout: WidgetLayout?,
    val value: String,
    @field:StringRes @param:StringRes val summaryResId: Int,
) {
    ONE_ROW(WidgetLayout.WIDGET_HEADER_ONE_ROW, "ONE_ROW", R.string.single_line_layout),
    TWO_ROWS(WidgetLayout.WIDGET_HEADER_TWO_ROWS, "TWO_ROWS", R.string.two_rows_layout),
    HIDDEN(null, "HIDDEN", R.string.hidden),
    ;

    companion object {
        var defaultValue = ONE_ROW

        fun fromValue(value: String?): WidgetHeaderLayout {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
