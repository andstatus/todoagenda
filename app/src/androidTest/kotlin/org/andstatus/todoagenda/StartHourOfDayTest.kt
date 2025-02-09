package org.andstatus.todoagenda

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.andstatus.todoagenda.prefs.EndedSomeTimeAgo
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.DayHeader
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test

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
        assertTrue(
            "First entry should be be today's header: $firstEntry",
            firstEntry is DayHeader && firstEntry.entryDay == thisDay,
        )
        val secondEntry = factory.widgetEntries[1]
        assertTrue(
            "Second entry should not end before today: $secondEntry",
            secondEntry is CalendarEntry && !secondEntry.event.endDate.isBefore(today),
        )
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
        assertTrue(
            "Should exist Dec 22 entry ${factory.widgetEntries}",
            indLastDec22 > 0,
        )
        val lastDec22Entry: CalendarEntry = factory.widgetEntries[indLastDec22] as CalendarEntry
        assertEquals(
            "Last day entry start date $lastDec22Entry",
            entryStartDate,
            lastDec22Entry.event.startDate,
        )
        assertTrue(
            "Entry should continue on the next day ${lastDec22Entry.eventTimeString}, $lastDec22Entry",
            lastDec22Entry.eventTimeString.endsWith(" →"),
        )
        val firstDec23Entry: CalendarEntry = factory.widgetEntries[indLastDec22 + 2] as CalendarEntry
        assertEquals(
            "Next day entry start date $firstDec23Entry",
            entryStartDate,
            firstDec23Entry.event.startDate,
        )
    }
}
