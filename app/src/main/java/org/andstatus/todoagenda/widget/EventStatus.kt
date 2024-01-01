package org.andstatus.todoagenda.widget;

/** The event status */
public enum EventStatus {
    TENTATIVE(0),
    CONFIRMED(1),
    CANCELED(2);

    /** Values of {@link android.provider.CalendarContract.Instances#STATUS} */
    final int calendarStatus;

    EventStatus(int calendarStatus) {
        this.calendarStatus = calendarStatus;
    }

    public static EventStatus fromCalendarStatus(int calendarStatus) {
        for(EventStatus status: values()) {
            if(status.calendarStatus == calendarStatus) return status;
        }
        return CONFIRMED;
    }
}
