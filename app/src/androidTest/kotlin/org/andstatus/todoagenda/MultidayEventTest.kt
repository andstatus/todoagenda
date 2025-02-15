package org.andstatus.todoagenda

import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.widget.CalendarEntry
import org.joda.time.DateTime
import org.junit.Assert
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
        val today = settings.clock.now().withTimeAtStartOfDay()
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
        Assert.assertNotNull(entry1)
        Assert.assertFalse("Is not active event", entry1!!.event.isActive)
        Assert.assertTrue("Is Part of Multi Day Event", entry1.isPartOfMultiDayEvent)
        Assert.assertTrue("Is start of Multi Day Event", entry1.isStartOfMultiDayEvent)
        Assert.assertFalse("Is not an end of Multi Day Event", entry1.isEndOfMultiDayEvent)
        Assert.assertEquals("Start Time didn't change for today's event", event.startDate, entry1.entryDate)
        Assert.assertEquals("Entry end time should be the same as Event end time", event.endDate, entry1.endDate)
        Assert.assertNotNull(entry2)
        Assert.assertFalse("Is not active event", entry2!!.event.isActive)
        Assert.assertTrue("Is Part of Multi Day Event", entry2.isPartOfMultiDayEvent)
        Assert.assertFalse("Is not start of Multi Day Event", entry2.isStartOfMultiDayEvent)
        Assert.assertTrue("Is end of Multi Day Event", entry2.isEndOfMultiDayEvent)
        Assert.assertEquals("Start Time of tomorrow's entry is midnight", today.plusDays(1), entry2.entryDate)
        Assert.assertEquals(
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
        Assert.assertEquals(sunday, entry1!!.entryDate)
        Assert.assertEquals(event.endDate, entry1.endDate)
        Assert.assertEquals(event.title, entry1.title)
        val timeString = entry1.eventTimeString
        Assert.assertTrue(timeString, timeString.contains(ARROW))
        Assert.assertEquals(timeString, timeString.indexOf(ARROW).toLong(), timeString.lastIndexOf(ARROW).toLong())
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
        Assert.assertNotNull(sundayEntry)
        return sundayEntry
    }

    companion object {
        private const val ARROW = "→"
    }
}
