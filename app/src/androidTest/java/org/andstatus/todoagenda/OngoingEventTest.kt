package org.andstatus.todoagenda

import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.widget.CalendarEntry
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class OngoingEventTest : BaseWidgetTest() {
    private var eventId = 0

    /**
     * @see [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testTodaysOngoingEvent() {
        val today = settings.clock().now().withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, provider.widgetId, false)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.setTitle("Ongoing event shows original start time")
        event.setStartDate(today.plusHours(9))
        event.endDate = today.plusHours(12)
        provider!!.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider!!.addRow(event)
        playResults(BaseWidgetTest.Companion.TAG)
        var entry: CalendarEntry? = null
        for (item in getFactory().widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isActive)
        Assert.assertFalse("Is not part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertEquals("Start Time didn't change for today's event", event.startDate, entry.entryDate)
        Assert.assertEquals("End Time didn't change for today's event", event.endDate, entry.endDate)
    }

    /**
     * @see [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testYesterdaysOngoingEvent() {
        val today = settings.clock().now().withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, provider.widgetId, false)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.setTitle("Ongoing event, which started yesterday, shows no start time")
        event.setStartDate(today.minusDays(1).plusHours(9))
        event.endDate = today.plusHours(12)
        provider!!.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider!!.addRow(event)
        playResults(BaseWidgetTest.Companion.TAG)
        var entry: CalendarEntry? = null
        for (item in getFactory().widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isActive)
        Assert.assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertFalse("Is not start of Multi Day Event", entry.isStartOfMultiDayEvent)
        Assert.assertTrue("Is end of Multi Day Event", entry.isEndOfMultiDayEvent)
        Assert.assertEquals("Yesterday's event entry start time is midnight", today, entry.entryDate)
        Assert.assertEquals("End Time didn't change for yesterday's event", event.endDate, entry.endDate)
    }

    @Test
    fun testEventWhichCarryOverToTheNextDay() {
        val today = settings.clock().now().withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, provider.widgetId, false)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.setTitle("Event that carry over to the next day, show as ending midnight")
        event.setStartDate(today.plusHours(19))
        event.endDate = today.plusDays(1).plusHours(7)
        provider!!.setExecutedAt(today.plusHours(20).plusMinutes(33))
        provider!!.addRow(event)
        playResults(BaseWidgetTest.Companion.TAG)
        var entry: CalendarEntry? = null
        for (item in getFactory().widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
                break
            }
        }
        Assert.assertNotNull(entry)
        Assert.assertTrue("Is active event", entry!!.event.isActive)
        Assert.assertTrue("Is Part of Multi Day Event", entry.isPartOfMultiDayEvent)
        Assert.assertTrue("Is start of Multi Day Event", entry.isStartOfMultiDayEvent)
        Assert.assertFalse("Is not an end of Multi Day Event", entry.isEndOfMultiDayEvent)
        Assert.assertEquals("Start Time didn't change for today's event", event.startDate, entry.entryDate)
        Assert.assertEquals("Entry end time is the same as Event end time", event.endDate, entry.endDate)
    }
}
