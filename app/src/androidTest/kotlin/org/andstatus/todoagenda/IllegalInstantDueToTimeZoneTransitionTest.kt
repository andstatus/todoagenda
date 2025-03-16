package org.andstatus.todoagenda

import android.util.Log
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.prefs.MyLocale.APP_DEFAULT_LOCALE
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.provider.QueryRow
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.MyClock
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.json.JSONException
import org.junit.Assert
import org.junit.Test
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * @author yvolk@yurivolkov.com
 */
class IllegalInstantDueToTimeZoneTransitionTest : BaseWidgetTest() {
    private var eventId = 0

    /**
     * Issue 186
     * See http://joda-time.sourceforge.net/faq.html#illegalinstant
     * http://stackoverflow.com/questions/25233776/unable-to-create-a-specific-joda-datetime-illegalinstantexception
     * http://beust.com/weblog/2013/03/30/the-time-that-never-was/
     *
     *
     * I couldn't reproduce the problem though.
     */
    @Test
    @Throws(JSONException::class)
    fun testIllegalInstantDueToTimeZoneOffsetTransition() {
        reproducedTimeZoneOffsetTransitionException()
        oneTimeDst("2014-09-07T00:00:00+00:00")
        oneTimeDst("2015-03-29T00:00:00+00:00")
        oneTimeDst("2015-10-25T00:00:00+00:00")
        oneTimeDst("2011-03-27T00:00:00+00:00")
        oneTimeDst("1980-04-06T00:00:00+00:00")
        if (provider.firstActiveEventSource === OrderedEventSource.EMPTY) {
            Log.e(TAG, settings.toJson().toString(2))
            Assert.fail("No active event sources")
        }
        provider.addRow(
            CalendarEvent(
                settings = settings,
                context = provider.context,
                eventSource = provider.firstActiveEventSource,
                isAllDay = false,
                startDateIn = settings.clock.startOfTomorrow(),
                title = "This will be the only event that will be shown",
            ),
        )
        playResults(TAG)
        Assert.assertEquals(5, factory.widgetEntries.size.toLong())
    }

    private fun oneTimeDst(iso8601time: String) {
        val millis = toMillis(iso8601time)
        val title = "DST"
        for (ind in -25..25) {
            provider.addRow(
                QueryRow()
                    .setEventId(++eventId)
                    .setTitle("$title $ind")
                    .setBegin(millis + TimeUnit.HOURS.toMillis(ind.toLong()))
                    .setAllDay(1),
            )
        }
    }

    /**
     * from http://stackoverflow.com/a/5451245/297710
     */
    private fun reproducedTimeZoneOffsetTransitionException() {
        val dateTimeZone = DateTimeZone.forID("CET")
        var localDateTime =
            LocalDateTime(dateTimeZone)
                .withYear(2011)
                .withMonthOfYear(3)
                .withDayOfMonth(27)
                .withHourOfDay(2)

        // This code is here to illustrate that the problem exists
        try {
            val myDateBroken = localDateTime.toDateTime(dateTimeZone)
            Assert.fail("No exception for $localDateTime -> $myDateBroken")
        } catch (iae: IllegalArgumentException) {
            Log.v(
                TAG,
                "Sure enough, invalid instant due to time zone offset transition: " +
                    localDateTime,
            )
        }
        if (dateTimeZone.isLocalDateTimeGap(localDateTime)) {
            localDateTime = localDateTime.withHourOfDay(3)
        }
        val myDate = localDateTime.toDateTime(dateTimeZone)
        Log.v(TAG, "No problem with this date: $myDate")
    }

    /**
     * http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    private fun toMillis(iso8601time: String): Long {
        val date: Date =
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", APP_DEFAULT_LOCALE).parse(iso8601time)!!
            } catch (e: ParseException) {
                throw IllegalArgumentException(iso8601time, e)
            }
        return date.time
    }

    /** https://github.com/andstatus/todoagenda/issues/13   */
    @Test
    fun testPeriodicAlarmTimeDuringTimeGap() {
        try {
            val zone = DateTimeZone.forID("America/Winnipeg")
            MyClock.myDefaultTimeZone = zone
            val periodMinutes = 10
            val nowUtc =
                DateTime(
                    2020,
                    3,
                    8,
                    2,
                    15,
                    DateTimeZone.UTC,
                ).plusSeconds(4)
            Assert.assertEquals(
                DateTime(
                    2020,
                    3,
                    8,
                    2,
                    15 + 1 + periodMinutes,
                    DateTimeZone.UTC,
                ),
                DateUtil.exactMinutesPlusMinutes(nowUtc, periodMinutes),
            )
            val now1 = nowUtc.plusHours(5).withZone(zone)
            val next1 = DateUtil.exactMinutesPlusMinutes(now1, periodMinutes)
            Assert.assertEquals("Next time: $next1", 1, next1.hourOfDay.toLong())
            val now2 = nowUtc.plusHours(6).withZone(zone)
            val next2 = DateUtil.exactMinutesPlusMinutes(now2, periodMinutes)
            Assert.assertEquals("Next time: $next2", 3, next2.hourOfDay.toLong())
            val nowWinnipeg =
                DateTime(
                    2020,
                    3,
                    8,
                    1,
                    54,
                    zone,
                ).plusSeconds(37)
            val expWinnipeg =
                DateTime(
                    2020,
                    3,
                    8,
                    1 + 2,
                    54 + 1 + periodMinutes - 60,
                    zone,
                )
            Assert.assertEquals(expWinnipeg, DateUtil.exactMinutesPlusMinutes(nowWinnipeg, periodMinutes))
        } finally {
            MyClock.myDefaultTimeZone = null
        }
    }

    companion object {
        private val TAG = IllegalInstantDueToTimeZoneTransitionTest::class.java.simpleName
    }
}
