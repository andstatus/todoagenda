package org.andstatus.todoagenda.widget

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.WidgetLayout

enum class LastEntryType(
    val widgetLayout: WidgetLayout,
    @field:StringRes val valueResId: Int,
) {
    NO_PERMISSIONS(WidgetLayout.ENTRY_LAST, R.string.permissions_justification),
    NO_EVENTS(WidgetLayout.ENTRY_LAST, R.string.no_events_to_show),
    NO_UPCOMING(WidgetLayout.ENTRY_LAST, R.string.no_upcoming_events),
    END_OF_LIST(WidgetLayout.ENTRY_LAST, R.string.end_of_list),
}
