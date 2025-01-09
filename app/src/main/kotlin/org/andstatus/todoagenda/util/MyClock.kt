package org.andstatus.todoagenda.util

import org.andstatus.todoagenda.prefs.SnapshotMode
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
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

    private fun getTimeMachineDate(timeZone: DateTimeZone?): DateTime {
        return if (snapshotDate == null) {
            DateTime.now(timeZone)
        } else {
            val millisElapsed = DateTime.now().millis - snapshotDateSetAt.millis
            DateTime(snapshotDate, timeZone).plusMillis(millisElapsed.toInt())
        }
    }

    fun isDayToday(date: DateTime?): Boolean = isDateDefined(date) && date!!.isEqual(thisDay(date.zone))

    fun isToday(date: DateTime?): Boolean {
        return isDateDefined(date) && dayOf(date!!).isEqual(thisDay(date.zone))
    }

    fun isDayBeforeToday(date: DateTime?): Boolean {
        return isDateDefined(date) && date!!.isBefore(thisDay(date.zone))
    }

    fun isBeforeToday(date: DateTime?): Boolean {
        return isDateDefined(date) && date!!.isBefore(startOfToday(date.zone))
    }

    fun isAfterToday(date: DateTime?): Boolean {
        return isDateDefined(date) && !date!!.isBefore(startOfTomorrow(date.zone))
    }

    fun isBeforeNow(date: DateTime?): Boolean {
        return isDateDefined(date) && date!!.isBefore(now(date.zone))
    }

    fun getNumberOfDaysTo(date: DateTime?): Int {
        return Days.daysBetween(
            now(date!!.zone).withTimeAtStartOfDay(),
            date.withTimeAtStartOfDay()
        )
            .days
    }

    fun getNumberOfMinutesTo(date: DateTime?): Int {
        return Minutes.minutesBetween(now(date!!.zone), date)
            .minutes
    }

    fun startOfTomorrow(timeZone: DateTimeZone? = this.timeZone): DateTime {
        return startOfToday(timeZone).plusDays(1)
    }

    fun startOfToday(timeZone: DateTimeZone? = this.timeZone): DateTime {
        return thisDay(timeZone).withTimeAtStartHourOfDayInner()
    }

    fun dayOf(date: DateTime): DateTime {
        return when {
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
    }

    fun isStartOfDay(date: DateTime): Boolean {
        return date.isEqual(startOfThisDay(date))
    }

    fun startOfNextDay(date: DateTime): DateTime {
        return startOfThisDay(date).plusDays(1)
    }

    fun startOfThisDay(date: DateTime): DateTime {
        return dayOf(date).withTimeAtStartHourOfDayInner()
    }

    fun withTimeAtStartHourOfDay(date: DateTime): DateTime {
        return date.withTimeAtStartHourOfDayInner()
    }

    private fun DateTime.withTimeAtStartHourOfDayInner(): DateTime {
        return withTimeAtStartOfDay().plusHours(startHourOfDay)
    }

    companion object {
        val DATETIME_MIN = DateTime(0, DateTimeZone.UTC).withTimeAtStartOfDay()
        val DATETIME_MAX = DateTime(5000, 1, 1, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay()

        @Volatile
        var myDefaultTimeZone: DateTimeZone? = null
        val defaultTimeZone: DateTimeZone get() = myDefaultTimeZone ?: DateTimeZone.forTimeZone(TimeZone.getDefault())

        fun isDateDefined(dateTime: DateTime?): Boolean {
            return dateTime != null && dateTime.isAfter(DATETIME_MIN) && dateTime.isBefore(DATETIME_MAX)
        }
    }
}
