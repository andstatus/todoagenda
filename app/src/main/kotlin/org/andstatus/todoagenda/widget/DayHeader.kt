package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.joda.time.DateTime

class DayHeader(
    settings: InstanceSettings,
    entryPosition: WidgetEntryPosition,
    date: DateTime,
) : WidgetEntry(settings, entryPosition, date, true, false, null) {
    override val source: OrderedEventSource
        get() = OrderedEventSource.DAY_HEADER
}
