package org.andstatus.todoagenda

import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.widget.CalendarEntry
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class SingleEventTest : BaseWidgetTest() {
    private var eventId = 0
    @Test
    fun testEventAttributes() {
        val today = settings.clock().now().withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, false)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.title = "Single Event today with all known attributes"
        event.setStartDate(today.plusHours(12))
        event.setEndDate(today.plusHours(13))
        event.color = -0x6d1e40
        event.location = "somewhere"
        event.isAlarmActive = true
        event.isRecurring = true
        val executedAt = today.plusHours(10)
        assertOneEvent(executedAt, event, true)
        event.isAlarmActive = false
        assertOneEvent(executedAt, event, true)
        event.isRecurring = false
        assertOneEvent(executedAt, event, true)
    }

    @Test
    fun testAlldayEventAttributes() {
        val today = settings.clock().now().withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, true)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.title = "Single AllDay event today with all known attributes"
        event.setStartDate(today.minusDays(1))
        event.setEndDate(today.plusDays(1))
        event.color = -0x6d1e40
        event.location = "somewhere"
        val executedAt = today.plusHours(10)
        assertOneEvent(executedAt, event, false)
        event.setStartDate(today)
        event.setEndDate(today.plusDays(1))
        assertOneEvent(executedAt, event, true)
    }

    @Test
    fun testAlldayEventMillis() {
        val today = settings.clock().now(DateTimeZone.UTC).withTimeAtStartOfDay()
        val event = CalendarEvent(settings, provider.context, true)
        event.setEventSource(provider.firstActiveEventSource)
        event.setEventId(++eventId)
        event.title = "Single All day event from millis"
        event.startMillis = today.millis
        Assert.assertEquals(event.startDate.toString(), today.millis, event.startMillis)
        Assert.assertEquals(event.endDate.toString(), today.plusDays(1).millis, event.endMillis)
    }

    private fun assertOneEvent(executedAt: DateTime, event: CalendarEvent, equal: Boolean) {
        provider.clear()
        provider.setExecutedAt(executedAt)
        provider.addRow(event)
        playResults(TAG)
        Assert.assertFalse(
            settings.toString(),
            settings.getActiveEventSources(EventProviderType.CALENDAR).isEmpty()
        )
        val source = provider.firstActiveEventSource
        Assert.assertTrue(source.toString(), source.source.isAvailable)
        Assert.assertTrue(
            settings.toString(),
            settings.getActiveEventSource(
                EventProviderType.CALENDAR,
                source.source.id
            ).source.isAvailable
        )
        Assert.assertEquals(factory.widgetEntries.toString(), 3, factory.widgetEntries.size.toLong())
        val entry = factory.widgetEntries[1]
        Assert.assertTrue(entry is CalendarEntry)
        val eventOut = (entry as CalendarEntry).event
        val msgLog = """
            Comparing events:
            in: $event
            out:$eventOut
            
            """.trimIndent()
        if (equal) {
            Assert.assertEquals(msgLog, event.toString(), eventOut.toString())
        } else {
            Assert.assertNotSame(msgLog, event.toString(), eventOut.toString())
        }
    }
}
