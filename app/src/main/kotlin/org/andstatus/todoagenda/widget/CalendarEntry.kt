package org.andstatus.todoagenda.widget

import android.content.Context
import android.text.TextUtils
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.StringUtil
import org.joda.time.DateTime

class CalendarEntry private constructor(
    settings: InstanceSettings,
    override val event: CalendarEvent,
    entryDate: DateTime,
) : WidgetEntry(
        settings,
        getEntryPosition(settings, event.isAllDay, entryDate, event.endDate),
        entryDate,
        event.isAllDay,
        event.isOngoing,
        event.endDate,
    ) {
    override val title: String
        get() {
            var title = event.title
            if (TextUtils.isEmpty(title)) {
                title = context.resources.getString(R.string.no_title)
            }
            return title
        }
    override val status: EventStatus
        get() = event.status
    val color: Int
        get() = event.color
    override val location: String?
        get() = event.location
    override val description: String?
        get() = event.description
    val isAlarmActive: Boolean
        get() = event.isAlarmActive
    val isRecurring: Boolean
        get() = event.isRecurring
    val isPartOfMultiDayEvent: Boolean
        get() = event.isPartOfMultiDayEvent
    val isStartOfMultiDayEvent: Boolean
        get() = isPartOfMultiDayEvent && !event.startDate.isBefore(entryDate)
    val isEndOfMultiDayEvent: Boolean
        get() = isPartOfMultiDayEvent && isLastEntryOfEvent

    fun spansOneFullDay(): Boolean = entryDate.plusDays(1).isEqual(event.endDate)

    override val eventTimeString: String
        get() = if (hideEventTime()) "" else timeSpanString

    private fun hideEventTime(): Boolean =
        spansOneFullDay() &&
            !(isStartOfMultiDayEvent || isEndOfMultiDayEvent) ||
            allDay

    private val timeSpanString: String
        get() {
            val startStr: String?
            val endStr: String?
            var separator = SPACE_DASH_SPACE
            if (!MyClock.isDateDefined(entryDate) ||
                (
                    isPartOfMultiDayEvent &&
                        settings.clock.isStartOfDay(entryDate) &&
                        !isStartOfMultiDayEvent
                )
            ) {
                startStr = ARROW
                separator = SPACE
            } else {
                startStr = DateUtil.formatTime({ this.settings }, entryDate)
            }
            if (this.settings.showEndTime) {
                if (!MyClock.isDateDefined(event.endDate) || isPartOfMultiDayEvent && !isLastEntryOfEvent) {
                    endStr = ARROW
                    separator = SPACE
                } else {
                    endStr = DateUtil.formatTime({ this.settings }, event.endDate)
                }
            } else {
                separator = DateUtil.EMPTY_STRING
                endStr = DateUtil.EMPTY_STRING
            }
            return if (startStr == endStr) {
                startStr
            } else {
                startStr + separator + endStr
            }
        }
    val context: Context
        get() = event.context

    override val source: OrderedEventSource
        get() = event.eventSource

    override fun toString(): String {
        val timeString = eventTimeString
        return (
            super.toString() + ", CalendarEntry [" +
                (if (allDay) "allDay" else "") +
                (if (StringUtil.nonEmpty(timeString)) ", time=$timeString" else "") +
                (if (locationShown.isNullOrBlank()) "" else ", location='$locationShown'") +
                (if (descriptionShown.isNullOrBlank()) "" else ", description='$descriptionShown'") +
                ", event=" + event +
                "]"
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as CalendarEntry
        return event == that.event && entryDate == that.entryDate
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result += 31 * event.hashCode()
        result += 31 * entryDate.hashCode()
        return result
    }

    companion object {
        private const val ARROW = "→"
        private const val SPACE = " "
        const val SPACE_DASH_SPACE = " - "

        fun fromEvent(
            settings: InstanceSettings,
            event: CalendarEvent,
            entryDate: DateTime,
        ): CalendarEntry = CalendarEntry(settings, event, entryDate)
    }
}
