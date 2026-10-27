package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.prefs.EndedSomeTimeAgo
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.DayHeader
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * @author yvolk@yurivolkov.com
 */
class StartHourOfDayTest : BaseWidgetTest() {
    @Test
    fun startHourOfDayTest() {
        val method = "startHourOfDayTest"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.start_hour_of_day)

        assertSplitEvent(method, DateTime(2024, 12, 22, 23, 30, DateTimeZone.UTC))

        val startHourOfDay = 4
        provider.settings = settings.copy(startHourOfDayIn = startHourOfDay)
        assertSplitEvent(method, DateTime(2024, 12, 23, 3, 30, DateTimeZone.UTC))

        provider.settings = settings.copy(eventsEnded = EndedSomeTimeAgo.TODAY)
        assertSplitEvent(method, DateTime(2024, 12, 23, 3, 30, DateTimeZone.UTC))

        val thisDay: DateTime =
            settings.resultsStorage!!
                .executedAt
                .get()
                .withTimeAtStartOfDay()
        val today: DateTime = thisDay.plusHours(startHourOfDay)
        val firstEntry = factory.widgetEntries[0]
        withClue("First entry should be be today's header: $firstEntry") {
            (firstEntry is DayHeader && firstEntry.entryDay == thisDay) shouldBe true
        }
        val secondEntry = factory.widgetEntries[1]
        withClue("Second entry should not end before today: $secondEntry") {
            (secondEntry is CalendarEntry && !secondEntry.event.endDate.isBefore(today)) shouldBe true
        }
    }

    private fun assertSplitEvent(
        methodIn: String,
        entryStartDate: DateTime,
    ) {
        val method = "$methodIn $entryStartDate"
        val inputs: QueryResultsStorage = settings.resultsStorage!!
        val thisDay: DateTime = inputs.executedAt.get().withTimeAtStartOfDay()

        playResults(method)
        val indLastDec22 =
            factory.widgetEntries.indexOfLast {
                it.entryDay == thisDay && it is CalendarEntry
            }
        withClue("Should exist Dec 22 entry ${factory.widgetEntries}") {
            (indLastDec22 > 0) shouldBe true
        }
        val lastDec22Entry: CalendarEntry = factory.widgetEntries[indLastDec22] as CalendarEntry
        withClue("Last day entry start date $lastDec22Entry") {
            lastDec22Entry.event.startDate shouldBe entryStartDate
        }
        withClue(
            "Entry should continue on the next day ${lastDec22Entry.eventTimeString}, $lastDec22Entry",
        ) {
            lastDec22Entry.eventTimeString.endsWith(" →") shouldBe true
        }
        val firstDec23Entry: CalendarEntry = factory.widgetEntries[indLastDec22 + 2] as CalendarEntry
        withClue("Next day entry start date $firstDec23Entry") {
            firstDec23Entry.event.startDate shouldBe entryStartDate
        }
    }
}
