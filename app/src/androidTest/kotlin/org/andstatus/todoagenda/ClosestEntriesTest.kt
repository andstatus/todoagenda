package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.WidgetEntry

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
        withClue("Only closest event") {
            entries.size shouldBe 1
        }
        val today = settings.clock.dayOf(provider.executedAt)
        entries[0].shouldBeCalendarEntry.entryDay shouldBe today

        val firstEntry = factory.widgetEntries.first()
        withClue(
            "Past events filter check. First entry should not be more than a week ago: $firstEntry",
        ) {
            firstEntry.entryDay shouldBe settings.clock.dayOf(2025, 2, 2)
        }
    }

    @Test
    fun testClosestEntryOfNotFilledMultiDayEvent() {
        val method = "testClosestEntryOfNotFilledMultiDayEvent"
        provider.loadResultsAndSettings(R.raw.daily_all_day_event)
        provider.settings = settings.copy(fillAllDayEvents = false)
        playResults(method)
        val title = "Monday's one week event"
        val entries: List<WidgetEntry> = factory.widgetEntries.filter { it.title == title }
        withClue("Only one entry") {
            entries.size shouldBe 1
        }
        val entry = entries[0].shouldBeCalendarEntry
        entry.entryDay shouldBe settings.clock.dayOf(2025, 2, 3)
        withClue("Days of event") {
            entry.event.daysOfEvent shouldBe 8
        }
        withClue("The last day of event") {
            entry.event.dayOfEvent(entry.entryDay) shouldBe 8
        }
    }
}
