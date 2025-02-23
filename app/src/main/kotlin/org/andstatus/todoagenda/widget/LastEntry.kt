package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.LastEntryAppearance
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
        ): LastEntry? =
            if (widgetEntries.isEmpty()) {
                when {
                    PermissionsUtil.mustRequestPermissions(settings.context) -> LastEntryType.NO_PERMISSIONS
                    else -> LastEntryType.EMPTY
                }.let { entryType ->
                    if (entryType == LastEntryType.NO_PERMISSIONS) {
                        LastEntryAppearance.WITH_MESSAGE
                    } else {
                        settings.lastEntryAppearance
                    }.let { appearance ->
                        if (appearance == LastEntryAppearance.HIDDEN) {
                            null
                        } else {
                            LastEntry(settings, entryType, settings.clock.now())
                        }
                    }
                }
            } else if (settings.lastEntryAppearance == LastEntryAppearance.HIDDEN) {
                null
            } else {
                LastEntry(
                    settings,
                    LastEntryType.LAST,
                    widgetEntries[widgetEntries.size - 1].entryDate,
                )
            }
    }
}
