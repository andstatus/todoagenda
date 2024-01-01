package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.R
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

    enum class LastEntryType(val layoutId: Int) {
        NOT_LOADED(R.layout.item_not_loaded),
        NO_PERMISSIONS(R.layout.item_no_permissions),
        EMPTY(R.layout.item_empty_list),
        LAST(R.layout.item_last)
    }

    companion object {
        fun forEmptyList(settings: InstanceSettings): LastEntry {
            val entryType =
                if (PermissionsUtil.arePermissionsGranted(settings.context)) LastEntryType.EMPTY else LastEntryType.NO_PERMISSIONS
            return LastEntry(settings, entryType, settings.clock().now())
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
