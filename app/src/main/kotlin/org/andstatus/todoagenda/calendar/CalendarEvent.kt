package org.andstatus.todoagenda.calendar

import android.content.Context
import android.util.Log
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.util.StringUtil
import org.andstatus.todoagenda.widget.EventStatus
import org.andstatus.todoagenda.widget.WidgetEvent
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.IllegalInstantException
import org.joda.time.LocalDateTime

data class CalendarEvent(
    val settings: InstanceSettings,
    val context: Context,
    val isAllDay: Boolean,
    override val eventSource: OrderedEventSource,
    override val eventId: Long = 0L,
    val title: String = "",
    private val startMillisIn: Long? = null,
    private val endMillisIn: Long? = null,
    private val startDateIn: DateTime? = null,
    private val endDateIn: DateTime? = null,
    val color: Int = 0,
    private val calendarColorIn: Int? = null,
    val location: String? = null,
    val description: String? = null,
    val isAlarmActive: Boolean = false,
    val isRecurring: Boolean = false,
    val status: EventStatus = EventStatus.CONFIRMED,
) : WidgetEvent {
    var startDate: DateTime = startDateIn ?: startMillisIn?.let { dateFromMillis(it) } ?: error("")

    var endDate: DateTime =
        endDateIn?.let { if (isAllDay) it.withTimeAtStartOfDay() else it } ?: endMillisIn
            ?.let { dateFromMillis(it) }
            .let { toFix ->
                if (toFix == null || !toFix.isAfter(startDate)) {
                    if (isAllDay) startDate.plusDays(1) else startDate.plusSeconds(1)
                } else {
                    toFix
                }
            }

    val startMillis: Long = dateToMillis(startDate)
    val endMillis: Long = dateToMillis(endDate)

    private fun dateFromMillis(millis: Long): DateTime = if (isAllDay) fromAllDayMillis(millis) else DateTime(millis, settings.timeZone)

    /**
     * Implemented based on this answer: http://stackoverflow.com/a/5451245/297710
     */
    private fun fromAllDayMillis(millis: Long): DateTime {
        var msgLog = "millis=$millis"
        val fixed: DateTime
        try {
            val utcDate = DateTime(millis, DateTimeZone.UTC)
            var ldt =
                LocalDateTime()
                    .withYear(utcDate.year)
                    .withMonthOfYear(utcDate.monthOfYear)
                    .withDayOfMonth(utcDate.dayOfMonth)
                    .withMillisOfDay(0)
            var hour = 0
            while (settings.timeZone.isLocalDateTimeGap(ldt)) {
                Log.v("fixTimeOfAllDayEvent", "Local Date Time Gap: $ldt; $msgLog")
                ldt = ldt.withHourOfDay(++hour)
            }
            fixed = ldt.toDateTime(settings.timeZone)
            msgLog += " -> $fixed"
            if (Math.abs(System.currentTimeMillis() - fixTimeOfAllDayEventLoggedAt) > 1000) {
                fixTimeOfAllDayEventLoggedAt = System.currentTimeMillis()
                Log.v("fixTimeOfAllDayEvent", msgLog)
            }
        } catch (e: IllegalInstantException) {
            throw IllegalInstantException("$msgLog caused by: $e")
        }
        return fixed
    }

    private fun dateToMillis(date: DateTime): Long = if (isAllDay) toAllDayMillis(date) else date.millis

    private fun toAllDayMillis(date: DateTime): Long {
        val utcDate =
            DateTime(
                date.year,
                date.monthOfYear,
                date.dayOfMonth,
                0,
                0,
                DateTimeZone.UTC,
            )
        return utcDate.millis
    }

    val calendarColor: Int = calendarColorIn ?: color
    val hasDefaultCalendarColor: Boolean = calendarColorIn?.let { it == color } ?: true

    override fun toString(): String =
        (
            "CalendarEvent [eventId=" + eventId +
                (if (StringUtil.nonEmpty(title)) ", title=$title" else "") +
                ", startDate=" + startDate +
                (", endDate=$endDate") +
                ", color=" + color +
                (
                    if (hasDefaultCalendarColor) {
                        " is default"
                    } else {
                        ", calendarColor=" +
                            (
                                calendarColorIn
                                    ?.let { obj: Int? ->
                                        java.lang.String.valueOf(
                                            obj,
                                        )
                                    } ?: "???"
                            )
                    }
                ) +
                ", allDay=" + isAllDay +
                ", alarmActive=" + isAlarmActive +
                ", recurring=" + isRecurring +
                (if (location.isNullOrBlank()) "" else ", location='$location'") +
                (if (description.isNullOrBlank()) "" else ", description='$description'") +
                "; Source [" + eventSource + "]]"
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as CalendarEvent
        return if (eventId != that.eventId || startDate != that.startDate) {
            false
        } else {
            true
        }
    }

    override fun hashCode(): Int {
        var result = eventId.toInt()
        result += 31 * startDate.hashCode()
        return result
    }

    val isOngoing: Boolean =
        settings.clock.now().let { now ->
            !startDate.isAfter(now) && endDate.isAfter(now)
        }
    val firstDay: DateTime = if (isAllDay) startDate else settings.clock.dayOf(startDate)

    val lastDay: DateTime = if (isAllDay) endDate.minusDays(1) else settings.clock.dayOf(endDate)
    val daysOfEvent: Int = Days.daysBetween(firstDay, lastDay).days + 1
    val isPartOfMultiDayEvent: Boolean = daysOfEvent > 1

    fun dayOfEvent(day: DateTime): Int = Days.daysBetween(firstDay, day).days + 1

    val closestTime: DateTime =
        settings.clock.now().let { now ->
            when {
                startDate.isAfter(now) -> startDate
                endDate.isBefore(now) -> endDate
                else -> now
            }
        }

    companion object {
        @Volatile
        private var fixTimeOfAllDayEventLoggedAt: Long = 0
    }
}
