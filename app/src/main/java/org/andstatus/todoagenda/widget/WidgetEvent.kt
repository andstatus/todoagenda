package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.OrderedEventSource

interface WidgetEvent {
    val eventSource: OrderedEventSource
    val eventId: Long
}
