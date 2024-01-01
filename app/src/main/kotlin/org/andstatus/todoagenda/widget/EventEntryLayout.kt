package org.andstatus.todoagenda.widget

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class EventEntryLayout(
    @field:LayoutRes @param:LayoutRes val layoutId: Int,
    val value: String,
    @field:StringRes val summaryResId: Int
) {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout),
    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout);

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
