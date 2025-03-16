package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.joda.time.DateTime

class CurrentTimeEntry(
    settings: InstanceSettings,
    date: DateTime = settings.clock.now(),
) : WidgetEntry(
        settings = settings,
        entryPosition = WidgetEntryPosition.ENTRY_DATE,
        entryDateIn = date,
        allDay = false,
        isOngoing = true,
        endDate = date.plusSeconds(1),
    ) {
    override val source: OrderedEventSource
        get() = OrderedEventSource.CURRENT_TIME
    override val title: String = "Current time"
}
