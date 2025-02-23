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
        fun getLastEntry(
            settings: InstanceSettings,
            widgetEntries: List<WidgetEntry>,
        ): LastEntry? {
            if (widgetEntries.isEmpty()) {
                return when {
                    PermissionsUtil.mustRequestPermissions(settings.context) -> LastEntryType.NO_PERMISSIONS
                    else -> LastEntryType.EMPTY
                }.let { entryType ->
                    if (settings.maxNumberOfEvents < 1) {
                        return LastEntry(settings, entryType, settings.clock.now())
                    } else {
                        return null
                    }
                }
            } else if (settings.maxNumberOfEvents < 1) {
                return LastEntry(
                    settings,
                    LastEntryType.LAST,
                    widgetEntries[widgetEntries.size - 1].entryDate,
                )
            } else {
                return null
            }
        }
    }
}
