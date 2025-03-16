package org.andstatus.todoagenda.widget

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.prefs.TaskScheduling
import org.andstatus.todoagenda.task.TaskEvent
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.MyClock
import org.joda.time.DateTime

class TaskEntry private constructor(
    settings: InstanceSettings,
    entryPosition: WidgetEntryPosition,
    private val mainDate: DateTime?,
    entryDate: DateTime,
    override val event: TaskEvent,
) : WidgetEntry(settings, entryPosition, entryDate, event.isAllDay, false, event.dueDate) {
    override val source: OrderedEventSource
        get() = event.eventSource
    override val title: String
        get() = event.title
    override val eventTimeString: String
        get() =
            if (allDay || entryPosition != WidgetEntryPosition.ENTRY_DATE) {
                ""
            } else {
                DateUtil.formatTime(
                    { settings },
                    mainDate,
                )
            }

    override fun toString(): String =
        super.toString() + ", TaskEntry [title='" + event.title + "', startDate=" + event.startDate +
            ", dueDate=" + event.dueDate + "]"

    companion object {
        fun fromEvent(
            settings: InstanceSettings,
            event: TaskEvent,
        ): TaskEntry {
            val mainDate = calcMainDate(settings, event)
            val entryPosition = getEntryPosition(settings, mainDate, event)
            return TaskEntry(settings, entryPosition, mainDate, getEntryDate(settings, entryPosition, event), event)
        }

        /** See https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887  */
        private fun getEntryPosition(
            settings: InstanceSettings,
            mainDate: DateTime?,
            event: TaskEvent,
        ): WidgetEntryPosition {
            if (!event.hasStartDate() && !event.hasDueDate()) return settings.taskWithoutDates.widgetEntryPosition
            if (mainDate != null) {
                if (mainDate.isAfter(settings.endOfTimeRange)) return WidgetEntryPosition.END_OF_LIST
            }
            val otherDate = otherDate(settings, event)
            if (settings.taskScheduling == TaskScheduling.DATE_DUE) {
                if (!event.hasDueDate()) {
                    if (settings.clock.isBeforeToday(event.startDate)) return WidgetEntryPosition.START_OF_TODAY
                    if (event.startDate!!.isAfter(settings.endOfTimeRange)) return WidgetEntryPosition.END_OF_LIST
                }
            } else {
                if (!event.hasStartDate() || settings.clock.isBeforeToday(event.startDate)) {
                    if (!settings.clock.isBeforeToday(event.dueDate)) return WidgetEntryPosition.START_OF_TODAY
                }
            }
            return WidgetEntry.getEntryPosition(settings, event.isAllDay, mainDate, otherDate)
        }

        private fun calcMainDate(
            settings: InstanceSettings,
            event: TaskEvent,
        ): DateTime? = if (settings.taskScheduling == TaskScheduling.DATE_DUE) event.dueDate else event.startDate

        private fun otherDate(
            settings: InstanceSettings,
            event: TaskEvent,
        ): DateTime? = if (settings.taskScheduling == TaskScheduling.DATE_DUE) event.startDate else event.dueDate

        private fun getEntryDate(
            settings: InstanceSettings,
            entryPosition: WidgetEntryPosition?,
            event: TaskEvent,
        ): DateTime =
            when (entryPosition) {
                WidgetEntryPosition.END_OF_TODAY,
                WidgetEntryPosition.END_OF_DAY,
                WidgetEntryPosition.END_OF_LIST,
                WidgetEntryPosition.END_OF_LIST_HEADER,
                WidgetEntryPosition.LIST_FOOTER,
                WidgetEntryPosition.HIDDEN,
                ->
                    getEntryDateOrElse(
                        settings,
                        event,
                        MyClock.DATETIME_MAX,
                    )

                else -> getEntryDateOrElse(settings, event, MyClock.DATETIME_MIN)
            }

        private fun getEntryDateOrElse(
            settings: InstanceSettings,
            event: TaskEvent,
            defaultDate: DateTime,
        ): DateTime =
            if (settings.taskScheduling == TaskScheduling.DATE_DUE) {
                if (event.hasDueDate()) {
                    event.dueDate
                } else if (event.hasStartDate()) {
                    event.startDate
                } else {
                    defaultDate
                }
            } else {
                if (event.hasStartDate()) {
                    if (settings.clock.isBeforeToday(event.startDate)) {
                        if (event.hasDueDate()) event.dueDate else defaultDate
                    } else {
                        event.startDate
                    }
                } else {
                    if (event.hasDueDate()) event.dueDate else defaultDate
                }
            }!!
    }
}
