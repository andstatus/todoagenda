package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.util.PermissionsUtil
import org.joda.time.DateTime

/** @author yvolk@yurivolkov.com
 */
class LastEntry(settings: InstanceSettings, val type: LastEntryType, date: DateTime) :
    WidgetEntry<LastEntry>(settings, WidgetEntryPosition.LIST_FOOTER, date, true, null) {
    override val source: OrderedEventSource
        get() = OrderedEventSource.LAST_ENTRY

    enum class LastEntryType(val widgetLayout: WidgetLayout) {
        NOT_LOADED(WidgetLayout.ITEM_NOT_LOADED),
        NO_PERMISSIONS(WidgetLayout.ITEM_NO_PERMISSIONS),
        EMPTY(WidgetLayout.ITEM_EMPTY_LIST),
        LAST(WidgetLayout.ITEM_LAST)
    }

    companion object {
        fun forEmptyList(settings: InstanceSettings): LastEntry {
            val entryType =
                if (PermissionsUtil.mustRequestPermissions(settings.context)) LastEntryType.NO_PERMISSIONS else LastEntryType.EMPTY
            return LastEntry(settings, entryType, settings.clock.now())
        }

        fun addLast(settings: InstanceSettings, widgetEntries: MutableList<WidgetEntry<*>>) {
            val entry = if (widgetEntries.isEmpty()) forEmptyList(settings) else LastEntry(
                settings,
                LastEntryType.LAST,
                widgetEntries[widgetEntries.size - 1].entryDate
            )
            widgetEntries.add(entry)
        }
    }
}
