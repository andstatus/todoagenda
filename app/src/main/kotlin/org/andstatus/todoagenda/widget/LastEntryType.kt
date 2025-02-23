package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.layout.WidgetLayout

enum class LastEntryType(
    val widgetLayout: WidgetLayout,
) {
    NOT_LOADED(WidgetLayout.ITEM_NOT_LOADED),
    NO_PERMISSIONS(WidgetLayout.ITEM_NO_PERMISSIONS),
    EMPTY(WidgetLayout.ITEM_EMPTY_LIST),
    LAST(WidgetLayout.ITEM_LAST),
}
