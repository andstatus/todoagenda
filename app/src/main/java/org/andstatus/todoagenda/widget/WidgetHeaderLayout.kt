package org.andstatus.todoagenda.widget

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class WidgetHeaderLayout(
    @field:LayoutRes @param:LayoutRes val layoutId: Int,
    val value: String,
    @field:StringRes @param:StringRes val summaryResId: Int
) {
    ONE_ROW(R.layout.widget_header_one_row, "ONE_ROW", R.string.single_line_layout),
    TWO_ROWS(R.layout.widget_header_two_rows, "TWO_ROWS", R.string.two_rows_layout),
    HIDDEN(0, "HIDDEN", R.string.hidden);

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
