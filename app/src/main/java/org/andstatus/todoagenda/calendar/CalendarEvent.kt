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
import org.joda.time.IllegalInstantException
import org.joda.time.LocalDateTime
import java.util.Optional
import kotlin.concurrent.Volatile

class CalendarEvent(
    val settings: InstanceSettings,
    val context: Context,
    private val widgetId: Int,
    val isAllDay: Boolean
) : WidgetEvent {
    override lateinit var eventSource: OrderedEventSource
        private set
    override var eventId = 0L
    var title: String = ""
        private set
    lateinit var startDate: DateTime
        private set
    lateinit var endDate: DateTime
        private set

    var color = 0
    var calendarColor = Optional.empty<Int>()
    var location: String = ""
        set(value) {
            field = StringUtil.notNull(value)
        }

    var isAlarmActive = false
    var isRecurring = false
    var status = EventStatus.CONFIRMED
    fun setEventSource(eventSource: OrderedEventSource): CalendarEvent {
        this.eventSource = eventSource
        return this
    }

    fun setStartDate(startDate: DateTime): CalendarEvent {
        this.startDate = if (isAllDay) startDate.withTimeAtStartOfDay() else startDate
        fixEndDate()
        return this
    }

    var startMillis: Long
        get() = dateToMillis(startDate)
        set(startMillis) {
            startDate = dateFromMillis(startMillis)
            fixEndDate()
        }

    private fun dateFromMillis(millis: Long): DateTime {
        return if (isAllDay) fromAllDayMillis(millis) else DateTime(millis, settings.clock().zone)
    }

    /**
     * Implemented based on this answer: http://stackoverflow.com/a/5451245/297710
     */
    private fun fromAllDayMillis(millis: Long): DateTime {
        var msgLog = "millis=$millis"
        val fixed: DateTime
        try {
            val utcDate = DateTime(millis, DateTimeZone.UTC)
            var ldt = LocalDateTime()
                .withYear(utcDate.year)
                .withMonthOfYear(utcDate.monthOfYear)
                .withDayOfMonth(utcDate.dayOfMonth)
                .withMillisOfDay(0)
            var hour = 0
            while (settings.clock().zone.isLocalDateTimeGap(ldt)) {
                Log.v("fixTimeOfAllDayEvent", "Local Date Time Gap: $ldt; $msgLog")
                ldt = ldt.withHourOfDay(++hour)
            }
            fixed = ldt.toDateTime(settings.clock().zone)
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

    private fun fixEndDate() {
        if (!this::endDate.isInitialized || !endDate.isAfter(startDate)) {
            endDate = if (isAllDay) startDate.plusDays(1) else startDate.plusSeconds(1)
        }
    }

    fun setEventId(eventId: Int) {
        this.eventId = eventId.toLong()
    }

    fun setTitle(title: String?): CalendarEvent {
        this.title = StringUtil.notNull(title)
        return this
    }

    fun setEndDate(endDate: DateTime) {
        this.endDate = if (isAllDay) endDate.withTimeAtStartOfDay() else endDate
        fixEndDate()
    }

    var endMillis: Long
        get() = dateToMillis(endDate)
        set(endMillis) {
            endDate = dateFromMillis(endMillis)
            fixEndDate()
        }

    private fun dateToMillis(date: DateTime?): Long {
        return if (isAllDay) toAllDayMillis(date) else date!!.millis
    }

    private fun toAllDayMillis(date: DateTime?): Long {
        val utcDate = DateTime(
            date!!.year, date.monthOfYear, date.dayOfMonth, 0, 0,
            DateTimeZone.UTC
        )
        return utcDate.millis
    }

    fun getCalendarColor(): Int {
        return calendarColor.orElse(color)
    }

    fun setCalendarColor(color: Int) {
        calendarColor = Optional.of(color)
    }

    fun hasDefaultCalendarColor(): Boolean {
        return calendarColor.map { cc: Int -> cc == color }.orElse(true)
    }

    override fun toString(): String {
        return ("CalendarEvent [eventId=" + eventId
            + (if (StringUtil.nonEmpty(title)) ", title=$title" else "")
            + ", startDate=" + startDate
            + (", endDate=$endDate")
            + ", color=" + color
            + (if (hasDefaultCalendarColor()) " is default" else ", calendarColor=" + calendarColor.map { obj: Int? ->
            java.lang.String.valueOf(
                obj
            )
        }
            .orElse("???"))
            + ", allDay=" + isAllDay
            + ", alarmActive=" + isAlarmActive
            + ", recurring=" + isRecurring
            + (if (StringUtil.nonEmpty(location)) ", location=$location" else "") +
            "; Source [" + eventSource + "]]")
    }

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
        } else true
    }

    override fun hashCode(): Int {
        var result = eventId.toInt()
        result += 31 * startDate.hashCode()
        return result
    }

    val isActive: Boolean
        get() {
            val now = settings.clock().now()
            return startDate.isBefore(now) && endDate.isAfter(now)
        }
    val isPartOfMultiDayEvent: Boolean
        get() = endDate.withTimeAtStartOfDay().isAfter(startDate.withTimeAtStartOfDay())

    companion object {
        @Volatile
        private var fixTimeOfAllDayEventLoggedAt: Long = 0
    }
}
