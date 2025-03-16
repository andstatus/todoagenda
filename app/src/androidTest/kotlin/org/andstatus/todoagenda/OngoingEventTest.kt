package org.andstatus.todoagenda

import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.WidgetEntry
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class OngoingEventTest : BaseWidgetTest() {
    private var eventId = 0L

    /**
     * See [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testTodaysOngoingEvent() {
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Ongoing event shows original start time",
                startDateIn = today.plusHours(9),
                endDateIn = today.plusHours(12),
            )
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isOngoing)
        Assert.assertFalse("Is not part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertEquals("Start Time didn't change for today's event", event.startDate, entry.entryDate)
        Assert.assertEquals("End Time didn't change for today's event", event.endDate, entry.endDate)
    }

    /**
     * See [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testYesterdaysOngoingEvent() {
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Ongoing event, which started yesterday, shows no start time",
                startDateIn = today.minusDays(1).plusHours(9),
                endDateIn = today.plusHours(12),
            )
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isOngoing)
        Assert.assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertFalse("Is not start of Multi Day Event", entry.isStartOfMultiDayEvent)
        Assert.assertTrue("Is end of Multi Day Event", entry.isEndOfMultiDayEvent)
        Assert.assertEquals("Yesterday's event entry start time is midnight", today, entry.entryDate)
        Assert.assertEquals("End Time didn't change for yesterday's event", event.endDate, entry.endDate)
    }

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
        provider.setExecutedAt(today.plusHours(20).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
                break
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isOngoing)
        Assert.assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertTrue("Is start of Multi Day Event", entry.isStartOfMultiDayEvent)
        Assert.assertFalse("Is not an end of Multi Day Event", entry.isEndOfMultiDayEvent)
        Assert.assertEquals("Start Time didn't change for today's event", event.startDate, entry.entryDate)
        Assert.assertEquals("Entry end time is the same as Event end time", event.endDate, entry.endDate)
    }

    @Test
    fun testMultiDayOngoingEvents() {
        val method = "testMultiDayOngoingEvents"
        provider.loadResultsAndSettings(R.raw.multi_day_ongoing)
        playResults(method)
        val thisDay = settings.clock.dayOf(provider.executedAt)
        assertEquals(settings.clock.dayOf(2025, 2, 8), thisDay)
        assertEquals(2, settings.startHourOfDay)
        val title = "Event that lasts till next week"
        val entries: List<WidgetEntry> = factory.widgetEntries.filter { it.title == title }
        assertEquals("Only one entry", 1, entries.size)
        val entry = entries[0].shouldBeCalendarEntry
        assertEquals(thisDay, entry.entryDay)
        val event = entry.event
        assertEquals("Days of event $event, ${event.firstDay} - ${event.lastDay}", 4, event.daysOfEvent)
        assertEquals("The second day of event", 2, event.dayOfEvent(entry.entryDay))
    }
}
