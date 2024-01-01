package org.andstatus.todoagenda.widget

/** The event status  */
enum class EventStatus(
    /** Values of [android.provider.CalendarContract.Instances.STATUS]  */
    val calendarStatus: Int
) {
    TENTATIVE(0),
    CONFIRMED(1),
    CANCELED(2);

    companion object {
        fun fromCalendarStatus(calendarStatus: Int): EventStatus {
            for (status in entries) {
                if (status.calendarStatus == calendarStatus) return status
            }
            return CONFIRMED
        }
    }
}
