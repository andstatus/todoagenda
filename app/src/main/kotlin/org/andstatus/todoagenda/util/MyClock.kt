package org.andstatus.todoagenda.util

import org.andstatus.todoagenda.prefs.SnapshotMode
import org.andstatus.todoagenda.widget.WidgetEntry
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.Minutes
import java.util.TimeZone
import kotlin.concurrent.Volatile

/**
 * A clock that can be changed independently from a Device clock
 *
 * @author yvolk@yurivolkov.com
 */
class MyClock(
    private val snapshotMode: SnapshotMode,
    private val snapshotDate: DateTime?,
    private val timeZone: DateTimeZone,
    private val startHourOfDay: Int,
) {
    private val snapshotDateSetAt: DateTime = DateTime.now()

    fun thisDay(timeZone: DateTimeZone? = this.timeZone): DateTime = dayOf(now(timeZone))

    /**
     * Usually returns real "now", but may be #setNow to some other time for testing purposes
     */
    fun now(zone: DateTimeZone? = this.timeZone): DateTime {
        val snapshotDate = snapshotDate
        return if (snapshotMode == SnapshotMode.SNAPSHOT_TIME && snapshotDate != null) {
            if (PermissionsUtil.isTestMode) getTimeMachineDate(zone) else snapshotDate.withZone(zone)
        } else {
            DateTime.now(zone)
        }
    }

    private fun getTimeMachineDate(timeZone: DateTimeZone?): DateTime =
        if (snapshotDate == null) {
            DateTime.now(timeZone)
        } else {
            val millisElapsed = DateTime.now().millis - snapshotDateSetAt.millis
            DateTime(snapshotDate, timeZone).plusMillis(millisElapsed.toInt())
        }

    fun isDayToday(date: DateTime?): Boolean = isDateDefined(date) && date!!.isEqual(thisDay(date.zone))

    fun isToday(date: DateTime?): Boolean = isDateDefined(date) && dayOf(date!!).isEqual(thisDay(date.zone))

    fun isDayBeforeToday(date: DateTime?): Boolean = isDateDefined(date) && date!!.isBefore(thisDay(date.zone))

    fun isBeforeToday(date: DateTime?): Boolean = isDateDefined(date) && date!!.isBefore(startOfToday(date.zone))

    fun isAfterToday(date: DateTime?): Boolean = isDateDefined(date) && !date!!.isBefore(startOfTomorrow(date.zone))

    fun isBeforeNow(date: DateTime?): Boolean = isDateDefined(date) && date!!.isBefore(now(date.zone))

    fun getNumberOfDaysTo(date: DateTime?): Int =
        Days
            .daysBetween(
                now(date!!.zone).withTimeAtStartOfDay(),
                date.withTimeAtStartOfDay(),
            ).days

    fun timeUntil(entry: WidgetEntry): TimeUntil = timeUntil(entry.entryDate, entry.entryDay)

    fun timeUntil(
        date: DateTime,
        day: DateTime? = null,
    ): TimeUntil =
        minutesTo(date).let { minutes ->
            if (minutes < 1) {
                TimeUntil(hours = 0, minutes = 0)
            } else if (minutes < MINUTES_IN_DAY) {
                val hours: Int = minutes / 60
                val min: Int = minutes - hours * 60
                TimeUntil(hours = hours, minutes = min)
            } else {
                TimeUntil(daysTo(day ?: dayOf(date)))
            }
        }

    fun minutesTo(date: DateTime): Int = Minutes.minutesBetween(now(date.zone), date).minutes

    fun daysTo(day: DateTime): Int = Days.daysBetween(thisDay(day.zone), day).days

    fun startOfTomorrow(timeZone: DateTimeZone? = this.timeZone): DateTime = startOfToday(timeZone).plusDays(1)

    fun startOfToday(timeZone: DateTimeZone? = this.timeZone): DateTime = thisDay(timeZone).withTimeAtStartHourOfDayInner()

    fun dayOf(
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        timeZone: DateTimeZone? = this.timeZone,
    ): DateTime = LocalDate(year, monthOfYear, dayOfMonth).toDateTimeAtStartOfDay(timeZone)

    /** This is like LocalDate without time.
     * Always 0 hours, for any StartHourOfDay */
    fun dayOf(date: DateTime): DateTime =
        when {
            startHourOfDay == 0 -> date.withTimeAtStartOfDay()
            startHourOfDay > 0 -> {
                val startOfThisDay = date.withTimeAtStartHourOfDayInner()
                if (startOfThisDay.isAfter(date)) {
                    date.minusDays(1).withTimeAtStartOfDay()
                } else {
                    date.withTimeAtStartOfDay()
                }
            }

            else -> {
                val startOfNextDay = date.withTimeAtStartHourOfDayInner().plusDays(1)
                if (date.isBefore(startOfNextDay)) {
                    date.withTimeAtStartOfDay()
                } else {
                    date.plusDays(1).withTimeAtStartOfDay()
                }
            }
        }

    fun isStartOfDay(date: DateTime): Boolean = date.isEqual(startOfThisDay(date))

    fun startOfNextDay(date: DateTime): DateTime = startOfThisDay(date).plusDays(1)

    fun startOfThisDay(date: DateTime): DateTime = dayOf(date).withTimeAtStartHourOfDayInner()

    fun withTimeAtStartHourOfDay(date: DateTime): DateTime = date.withTimeAtStartHourOfDayInner()

    private fun DateTime.withTimeAtStartHourOfDayInner(): DateTime = withTimeAtStartOfDay().plusHours(startHourOfDay)

    companion object {
        const val MINUTES_IN_DAY = 60 * 24
        val DATETIME_MIN = DateTime(0, DateTimeZone.UTC).withTimeAtStartOfDay()
        val DATETIME_MAX = DateTime(5000, 1, 1, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay()

        @Volatile
        var myDefaultTimeZone: DateTimeZone? = null
        val defaultTimeZone: DateTimeZone get() = myDefaultTimeZone ?: DateTimeZone.forTimeZone(TimeZone.getDefault())

        fun isDateDefined(dateTime: DateTime?): Boolean =
            dateTime != null && dateTime.isAfter(DATETIME_MIN) && dateTime.isBefore(DATETIME_MAX)
    }
}
