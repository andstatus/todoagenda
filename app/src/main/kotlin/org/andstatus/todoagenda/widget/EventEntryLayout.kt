package org.andstatus.todoagenda.widget

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class EventEntryLayout(
    val widgetLayout: WidgetLayout,
    val value: String,
    @field:StringRes val summaryResId: Int
) {
    DEFAULT(WidgetLayout.EVENT_ENTRY_DEFAULT, "DEFAULT", R.string.default_multiline_layout),
    ONE_LINE(WidgetLayout.EVENT_ENTRY_ONE_LINE, "ONE_LINE", R.string.single_line_layout);

    companion object {
        const val SPACE_PIPE_SPACE = "  |  "
        fun fromValue(value: String?): EventEntryLayout {
            var layout = DEFAULT
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
