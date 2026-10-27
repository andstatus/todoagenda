package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.widget.CalendarEntry
import org.joda.time.DateTime

/**
 * @author yvolk@yurivolkov.com
 */
class MultidayEventTest : BaseWidgetTest() {
    private var eventId = 0L

    /**
     * Issue #206 https://github.com/plusonelabs/calendar-widget/issues/206
     */
    @Test
    fun testEventWhichCarryOverToTheNextDay() {
        val today = settings.clock.thisDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Event that carry over to the next day, show as ending midnight",
                startDateIn = today.plusHours(19),
                endDateIn = today.plusDays(1).plusHours(7),
            )
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry1: CalendarEntry? = null
        var entry2: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                if (entry1 == null) {
                    entry1 = item
                } else {
                    entry2 = item
                }
            }
        }
        entry1.shouldNotBeNull()
        withClue("Is not active event") {
            entry1!!.event.isOngoing shouldBe false
        }
        withClue("Days of event") {
            entry1!!.event.daysOfEvent shouldBe 2
        }
        withClue("First day of event") {
            entry1!!.event.dayOfEvent(entry1!!.entryDay) shouldBe 1
        }
        withClue("Is Part of Multi Day Event") {
            entry1!!.isPartOfMultiDayEvent shouldBe true
        }
        withClue("Is start of Multi Day Event") {
            entry1!!.isStartOfMultiDayEvent shouldBe true
        }
        withClue("Is not an end of Multi Day Event") {
            entry1!!.isEndOfMultiDayEvent shouldBe false
        }
        withClue("Start Time didn't change for today's event") {
            entry1!!.entryDate shouldBe event.startDate
        }
        withClue("Entry end time should be the same as Event end time") {
            entry1!!.endDate shouldBe event.endDate
        }
        entry2.shouldNotBeNull()
        withClue("Is not active event") {
            entry2!!.event.isOngoing shouldBe false
        }
        withClue("Second day of event") {
            entry2!!.event.dayOfEvent(entry2!!.entryDay) shouldBe 2
        }
        withClue("Is Part of Multi Day Event") {
            entry2!!.isPartOfMultiDayEvent shouldBe true
        }
        withClue("Is not start of Multi Day Event") {
            entry2!!.isStartOfMultiDayEvent shouldBe false
        }
        withClue("Is end of Multi Day Event") {
            entry2!!.isEndOfMultiDayEvent shouldBe true
        }
        withClue("Start Time of tomorrow's entry is midnight") {
            entry2!!.entryDate shouldBe today.plusDays(1)
        }
        withClue("Tomorrow event entry end time is the same as for the event") {
            entry2!!.endDate shouldBe entry2!!.event.endDate
        }
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/184#issuecomment-142671469
     */
    @Test
    fun testThreeDaysEvent() {
        val friday = dateTime(2015, 9, 18)
        val sunday = friday.plusDays(2)
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Leader's weekend",
                startDateIn = friday.plusHours(19),
                endDateIn = sunday.plusHours(15),
            )
        withClue("Days of event") {
            event.daysOfEvent shouldBe 3
        }
        assertSundayEntryAt(event, sunday, friday.plusHours(14))
        assertSundayEntryAt(event, sunday, friday.plusDays(1).plusHours(14))
        assertSundayEntryAt(event, sunday, friday.plusDays(2).plusHours(14))
    }

    private fun assertSundayEntryAt(
        event: CalendarEvent,
        sunday: DateTime,
        currentDateTime: DateTime,
    ) {
        val entry1 = getSundayEntryAt(event, currentDateTime)
        entry1!!.entryDate shouldBe sunday
        entry1.endDate shouldBe event.endDate
        entry1.title shouldBe event.title
        val timeString = entry1.eventTimeString
        withClue(timeString) {
            timeString.contains(ARROW) shouldBe true
            timeString.lastIndexOf(ARROW) shouldBe timeString.indexOf(ARROW)
        }
    }

    private fun getSundayEntryAt(
        event: CalendarEvent,
        currentDateTime: DateTime,
    ): CalendarEntry? {
        provider.clear()
        provider.setExecutedAt(currentDateTime)
        provider.addRow(event)
        playResults(TAG)
        var sundayEntry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                if (item.entryDate.dayOfMonth == 20) {
                    sundayEntry.shouldBeNull()
                    sundayEntry = item
                }
            }
        }
        sundayEntry.shouldNotBeNull()
        return sundayEntry
    }

    companion object {
        private const val ARROW = "→"
    }
}
