package org.andstatus.todoagenda

import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.widget.CalendarEntry
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertNotNull(entry1)
        assertFalse("Is not active event", entry1!!.event.isOngoing)
        assertEquals("Days of event", 2, entry1.event.daysOfEvent)
        assertEquals("First day of event", 1, entry1.event.dayOfEvent(entry1.entryDay))
        assertTrue("Is Part of Multi Day Event", entry1.isPartOfMultiDayEvent)
        assertTrue("Is start of Multi Day Event", entry1.isStartOfMultiDayEvent)
        assertFalse("Is not an end of Multi Day Event", entry1.isEndOfMultiDayEvent)
        assertEquals("Start Time didn't change for today's event", event.startDate, entry1.entryDate)
        assertEquals("Entry end time should be the same as Event end time", event.endDate, entry1.endDate)
        assertNotNull(entry2)
        assertFalse("Is not active event", entry2!!.event.isOngoing)
        assertEquals("Second day of event", 2, entry2.event.dayOfEvent(entry2.entryDay))
        assertTrue("Is Part of Multi Day Event", entry2.isPartOfMultiDayEvent)
        assertFalse("Is not start of Multi Day Event", entry2.isStartOfMultiDayEvent)
        assertTrue("Is end of Multi Day Event", entry2.isEndOfMultiDayEvent)
        assertEquals("Start Time of tomorrow's entry is midnight", today.plusDays(1), entry2.entryDate)
        assertEquals(
            "Tomorrow event entry end time is the same as for the event",
            entry2.event.endDate,
            entry2.endDate,
        )
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
        assertEquals("Days of event", 3, event.daysOfEvent)
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
        assertEquals(sunday, entry1!!.entryDate)
        assertEquals(event.endDate, entry1.endDate)
        assertEquals(event.title, entry1.title)
        val timeString = entry1.eventTimeString
        assertTrue(timeString, timeString.contains(ARROW))
        assertEquals(timeString, timeString.indexOf(ARROW).toLong(), timeString.lastIndexOf(ARROW).toLong())
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
                    Assert.assertNull(sundayEntry)
                    sundayEntry = item
                }
            }
        }
        assertNotNull(sundayEntry)
        return sundayEntry
    }

    companion object {
        private const val ARROW = "→"
    }
}
