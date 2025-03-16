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
    val appearance: LastEntryAppearance,
    isOngoing: Boolean,
    date: DateTime,
) : WidgetEntry(
        settings = settings,
        entryPosition = WidgetEntryPosition.ENTRY_DATE,
        entryDateIn = date,
        allDay = true,
        isOngoing = isOngoing,
        endDate = null,
    ) {
    override val source: OrderedEventSource
        get() = OrderedEventSource.LAST_ENTRY

    override fun toString(): String = (super.toString() + ", LastEntry [$type]")

    companion object {
        fun getLastEntry(
            settings: InstanceSettings,
            widgetEntries: List<WidgetEntry>,
        ): LastEntry? =
            if (widgetEntries.isEmpty()) {
                when {
                    PermissionsUtil.mustRequestPermissions(settings.context) -> LastEntryType.NO_PERMISSIONS
                    settings.noPastEvents() -> LastEntryType.NO_UPCOMING
                    else -> LastEntryType.NO_EVENTS
                }.let { entryType ->
                    if (entryType == LastEntryType.NO_PERMISSIONS) {
                        LastEntryAppearance.WITH_MESSAGE
                    } else {
                        settings.lastEntryAppearance
                    }.let { appearance ->
                        if (appearance == LastEntryAppearance.HIDDEN) {
                            null
                        } else {
                            LastEntry(
                                settings = settings,
                                type = entryType,
                                appearance = appearance,
                                isOngoing = true,
                                date = settings.clock.now(),
                            )
                        }
                    }
                }
            } else if (settings.lastEntryAppearance == LastEntryAppearance.HIDDEN) {
                null
            } else {
                LastEntry(
                    settings = settings,
                    type = LastEntryType.END_OF_LIST,
                    appearance = settings.lastEntryAppearance,
                    isOngoing = false,
                    date = widgetEntries[widgetEntries.size - 1].entryDate,
                )
            }
    }
}
