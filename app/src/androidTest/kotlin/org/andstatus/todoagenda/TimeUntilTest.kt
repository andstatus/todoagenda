package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.SnapshotMode
import org.andstatus.todoagenda.prefs.TestLocale
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.TimeUntil
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUntilTest : BaseWidgetTest() {
    @Test
    fun timeCalculation() =
        TestLocale.withEnUsLocale {
            val zone = DateTimeZone.forID("Europe/Moscow")
            val now = DateTime(2025, 3, 8, 6, 36, zone).withSecondOfMinute(10)
            val clock =
                MyClock(
                    snapshotMode = SnapshotMode.SNAPSHOT_TIME,
                    snapshotDate = now.minusSeconds(5),
                    timeZone = zone,
                    startHourOfDay = 0,
                )
            assertEquals(TimeUntil(hours = 0, minutes = 3), clock.timeUntil(now.plusMinutes(3)))
            val timeUntil2 = clock.timeUntil(now.plusHours(23).plusMinutes(3))
            assertEquals(TimeUntil(hours = 23, minutes = 3), timeUntil2)
            assertEquals("in 23:03", timeUntil2.format(settings))
            val timeUntil3 = clock.timeUntil(now.plusHours(24).plusMinutes(1))
            assertEquals(TimeUntil(days = 1), timeUntil3)
            assertEquals("Tomorrow", timeUntil3.format(settings))
            val timeUntil4 = clock.timeUntil(DateTime(2025, 3, 10, 0, 1, zone))
            assertEquals(TimeUntil(days = 2), timeUntil4)
            assertEquals("in 2 days", timeUntil4.format(settings))
            assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2)))
            assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2).plusHours(2)))
            assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2).minusHours(2)))
        }

    @Test
    fun timeCalculationWithStartHourOfDay() {
        val zone = DateTimeZone.forID("Europe/Moscow")
        val now = DateTime(2025, 3, 8, 6, 36, zone).withSecondOfMinute(10)
        val clock =
            MyClock(
                snapshotMode = SnapshotMode.SNAPSHOT_TIME,
                snapshotDate = now.minusSeconds(5),
                timeZone = zone,
                startHourOfDay = 2,
            )
        assertEquals(TimeUntil(hours = 0, minutes = 3), clock.timeUntil(now.plusMinutes(3)))
        assertEquals(TimeUntil(hours = 23, minutes = 3), clock.timeUntil(now.plusHours(23).plusMinutes(3)))
        assertEquals(TimeUntil(days = 1), clock.timeUntil(now.plusHours(24).plusMinutes(1)))
        assertEquals(TimeUntil(days = 1), clock.timeUntil(DateTime(2025, 3, 10, 0, 1, zone)))
        assertEquals(TimeUntil(days = 2), clock.timeUntil(DateTime(2025, 3, 10, 2, 1, zone)))
        assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2)))
        assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2).plusHours(2)))
        assertEquals(TimeUntil(days = 2), clock.timeUntil(now.plusDays(2).minusHours(2)))
    }
}
