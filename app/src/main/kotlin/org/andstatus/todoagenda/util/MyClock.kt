package org.andstatus.todoagenda.util

import org.andstatus.todoagenda.prefs.SnapshotMode
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days
import org.joda.time.Minutes
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicInteger
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
) {
    private val snapshotDateSetAt: DateTime = DateTime.now()

    private val startHourOfDayRef: AtomicInteger = AtomicInteger(0)
    var startHourOfDay: Int
        get() = startHourOfDayRef.get()
        set(value) {
            startHourOfDayRef.set(value)
        }

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

    private fun getTimeMachineDate(zone: DateTimeZone?): DateTime {
        return if (snapshotDate == null) {
            DateTime.now(zone)
        } else {
            val millisElapsed = DateTime.now().millis - snapshotDateSetAt.millis
            DateTime(snapshotDate, zone).plusMillis(millisElapsed.toInt())
        }
    }

    fun isToday(date: DateTime?): Boolean {
        return isDateDefined(date) && !isBeforeToday(date) && date!!.isBefore(
            now(date.zone).plusDays(1).withTimeAtStartOfDay()
        )
    }

    fun isBeforeToday(date: DateTime?): Boolean {
        return isDateDefined(date) && date!!.isBefore(now(date.zone).withTimeAtStartOfDay())
    }

    fun isAfterToday(date: DateTime?): Boolean {
        return isDateDefined(date) && !date!!.isBefore(now(date.zone).withTimeAtStartOfDay().plusDays(1))
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

    fun startOfTomorrow(): DateTime {
        return startOfNextDay(now(timeZone))
    }

    companion object {
        val DATETIME_MIN = DateTime(0, DateTimeZone.UTC).withTimeAtStartOfDay()
        val DATETIME_MAX = DateTime(5000, 1, 1, 0, 0, DateTimeZone.UTC).withTimeAtStartOfDay()

        @Volatile
        var myDefaultTimeZone: DateTimeZone? = null
        val defaultTimeZone: DateTimeZone get() = myDefaultTimeZone ?: DateTimeZone.forTimeZone(TimeZone.getDefault())

        fun startOfNextDay(date: DateTime): DateTime {
            return date.plusDays(1).withTimeAtStartOfDay()
        }

        fun isDateDefined(dateTime: DateTime?): Boolean {
            return dateTime != null && dateTime.isAfter(DATETIME_MIN) && dateTime.isBefore(DATETIME_MAX)
        }
    }
}
