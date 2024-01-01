package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.prefs.OrderedEventSource;

public interface WidgetEvent {
    OrderedEventSource getEventSource();
    long getEventId();
}
