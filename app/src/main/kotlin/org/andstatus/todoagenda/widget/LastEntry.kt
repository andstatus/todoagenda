package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.util.PermissionsUtil
import org.joda.time.DateTime

/** @author yvolk@yurivolkov.com
 */
class LastEntry(
    settings: InstanceSettings,
    val type: LastEntryType,
    date: DateTime,
) : WidgetEntry(settings, WidgetEntryPosition.LIST_FOOTER, date, true, null) {
    override val source: OrderedEventSource
        get() = OrderedEventSource.LAST_ENTRY

    companion object {
        fun forEmptyList(settings: InstanceSettings): LastEntry {
            val entryType =
                if (PermissionsUtil.mustRequestPermissions(settings.context)) LastEntryType.NO_PERMISSIONS else LastEntryType.EMPTY
            return LastEntry(settings, entryType, settings.clock.now())
        }

        fun addLast(
            settings: InstanceSettings,
            widgetEntries: List<WidgetEntry>,
        ): List<WidgetEntry> {
            val entry =
                if (widgetEntries.isEmpty()) {
                    forEmptyList(settings)
                } else {
                    LastEntry(
                        settings,
                        LastEntryType.LAST,
                        widgetEntries[widgetEntries.size - 1].entryDate,
                    )
                }
            return widgetEntries + entry
        }
    }
}
