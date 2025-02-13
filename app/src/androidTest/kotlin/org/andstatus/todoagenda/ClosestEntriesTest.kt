package org.andstatus.todoagenda

import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.WidgetEntry
import org.junit.Assert
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
        val entries: List<WidgetEntry<*>> = factory.widgetEntries.filter { it.title == title }
        Assert.assertEquals("Only closest event", 1, entries.size)
        val today = settings.clock.dayOf(provider.executedAt)
        Assert.assertEquals(today, entries[0].shouldBeCalendarEntry.entryDay)

        val firstEntry = factory.widgetEntries.first()
        Assert.assertEquals(
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
        val entries: List<WidgetEntry<*>> = factory.widgetEntries.filter { it.title == title }
        Assert.assertEquals("Only one entry", 1, entries.size)
        Assert.assertEquals(settings.clock.dayOf(2025, 2, 3), entries[0].shouldBeCalendarEntry.entryDay)
    }
}
