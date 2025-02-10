package org.andstatus.todoagenda

import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.WidgetEntry
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class ClosestAllDayDailyEventTest : BaseWidgetTest() {
    @Test
    fun testInsidePeriod() {
        val method = "testInsidePeriod"
        provider.loadResultsAndSettings(R.raw.daily_all_day_event)
        provider.settings = settings.copy(showOnlyClosestInstanceOfRecurringEvent = true)
        playResults(method)
        val title = "Daily all day event"
        val dailyEntries: List<WidgetEntry<*>> = factory.widgetEntries.filter { it.title == title }
        Assert.assertEquals("Only closest event", 1, dailyEntries.size)
        val entry: CalendarEntry = dailyEntries[0] as? CalendarEntry ?: error("Wrong type of ${dailyEntries[0]}")
        val today = settings.clock.dayOf(provider.executedAt)
        Assert.assertEquals(today, entry.entryDay)
    }
}
