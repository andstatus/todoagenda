package org.andstatus.todoagenda.layout

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class EventEntryLayout(
    val widgetLayout: WidgetLayout,
    val value: String,
    @field:StringRes val summaryResId: Int,
) {
    TIME_BELOW_TITLE(WidgetLayout.EVENT_ENTRY_TIME_BELOW_TITLE, "DEFAULT", R.string.default_multiline_layout),
    ONE_LINE(WidgetLayout.EVENT_ENTRY_ONE_LINE, "ONE_LINE", R.string.single_line_layout),
    ;

    companion object {
        const val SPACE_PIPE_SPACE = "  |  "

        fun fromValue(value: String?): EventEntryLayout {
            var layout = TIME_BELOW_TITLE
            for (item in entries) {
                if (item.value == value) {
                    layout = item
                    break
                }
            }
            return layout
        }
    }
}
