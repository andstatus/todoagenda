package org.andstatus.todoagenda

import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.prefs.SnapshotMode
import org.andstatus.todoagenda.prefs.TestLocale
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.TimeUntil
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

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
            clock.timeUntil(now.plusMinutes(3)) shouldBe TimeUntil(hours = 0, minutes = 3)
            val timeUntil2 = clock.timeUntil(now.plusHours(23).plusMinutes(3))
            timeUntil2 shouldBe TimeUntil(hours = 23, minutes = 3)
            timeUntil2.format(settings) shouldBe "in 23:03"
            val timeUntil3 = clock.timeUntil(now.plusHours(24).plusMinutes(1))
            timeUntil3 shouldBe TimeUntil(days = 1)
            timeUntil3.format(settings) shouldBe "Tomorrow"
            val timeUntil4 = clock.timeUntil(DateTime(2025, 3, 10, 0, 1, zone))
            timeUntil4 shouldBe TimeUntil(days = 2)
            timeUntil4.format(settings) shouldBe "in 2 days"
            clock.timeUntil(now.plusDays(2)) shouldBe TimeUntil(days = 2)
            clock.timeUntil(now.plusDays(2).plusHours(2)) shouldBe TimeUntil(days = 2)
            clock.timeUntil(now.plusDays(2).minusHours(2)) shouldBe TimeUntil(days = 2)
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
        clock.timeUntil(now.plusMinutes(3)) shouldBe TimeUntil(hours = 0, minutes = 3)
        clock.timeUntil(now.plusHours(23).plusMinutes(3)) shouldBe TimeUntil(hours = 23, minutes = 3)
        clock.timeUntil(now.plusHours(24).plusMinutes(1)) shouldBe TimeUntil(days = 1)
        clock.timeUntil(DateTime(2025, 3, 10, 0, 1, zone)) shouldBe TimeUntil(days = 1)
        clock.timeUntil(DateTime(2025, 3, 10, 2, 1, zone)) shouldBe TimeUntil(days = 2)
        clock.timeUntil(now.plusDays(2)) shouldBe TimeUntil(days = 2)
        clock.timeUntil(now.plusDays(2).plusHours(2)) shouldBe TimeUntil(days = 2)
        clock.timeUntil(now.plusDays(2).minusHours(2)) shouldBe TimeUntil(days = 2)
    }
}
