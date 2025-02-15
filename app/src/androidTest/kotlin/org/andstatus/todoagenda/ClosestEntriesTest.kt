package org.andstatus.todoagenda

import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.WidgetEntry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class ClosestEntriesTest : BaseWidgetTest() {
    @Test
    fun testClosestAllDayDailyEvent() {
        val method = "testClosestAllDayDailyEvent"
        provider.loadResultsAndSettings(R.raw.daily_all_day_event)
        provider.settings = settings.copy(showOnlyClosestInstanceOfRecurringEvent = true)
        playResults(method)
        val title = "Daily all day event"
        val entries: List<WidgetEntry> = factory.widgetEntries.filter { it.title == title }
        assertEquals("Only closest event", 1, entries.size)
        val today = settings.clock.dayOf(provider.executedAt)
        assertEquals(today, entries[0].shouldBeCalendarEntry.entryDay)

        val firstEntry = factory.widgetEntries.first()
        assertEquals(
            "Past events filter check. First entry should not be more than a week ago: $firstEntry",
            settings.clock.dayOf(2025, 2, 2),
            firstEntry.entryDay,
        )
    }

    @Test
    fun testClosestEntryOfNotFilledMultiDayEvent() {
        val method = "testClosestEntryOfNotFilledMultiDayEvent"
        provider.loadResultsAndSettings(R.raw.daily_all_day_event)
        provider.settings = settings.copy(fillAllDayEvents = false)
        playResults(method)
        val title = "Monday's one week event"
        val entries: List<WidgetEntry> = factory.widgetEntries.filter { it.title == title }
        assertEquals("Only one entry", 1, entries.size)
        val entry = entries[0].shouldBeCalendarEntry
        assertEquals(settings.clock.dayOf(2025, 2, 3), entry.entryDay)
        assertEquals("Days of event", 8, entry.event.daysOfEvent)
        assertEquals("The last day of event", 8, entry.event.dayOfEvent(entry.entryDay))
    }
}
