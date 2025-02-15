package org.andstatus.todoagenda.layout

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.widget.EventEntryVisualizer

/**
 * @author yvolk@yurivolkov.com
 */
enum class EventEntryLayout(
    val widgetLayout: WidgetLayout,
    val value: String,
    @field:StringRes val summaryResId: Int,
    val applier: (EventEntryVisualizer) -> EventEntryLayoutApplier,
) {
    TIME_BELOW_TITLE(
        widgetLayout = WidgetLayout.EVENT_ENTRY_TIME_BELOW_TITLE,
        value = "DEFAULT",
        summaryResId = R.string.default_multiline_layout,
        applier = ::TimeBelowTitleLayoutApplier,
    ),
    ONE_LINE(
        widgetLayout = WidgetLayout.EVENT_ENTRY_ONE_LINE,
        value = "ONE_LINE",
        summaryResId = R.string.single_line_layout,
        applier = ::OneLineLayoutApplier,
    ),
    ;

    companion object {
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
