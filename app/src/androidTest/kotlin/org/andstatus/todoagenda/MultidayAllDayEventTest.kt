package org.andstatus.todoagenda

import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.DayHeader
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntryType

/**
 * @author yvolk@yurivolkov.com
 */
class MultidayAllDayEventTest : BaseWidgetTest() {
    @Test
    fun testInsidePeriod() {
        val method = "testInsidePeriod"
        provider.loadResultsAndSettings(R.raw.multi_day)
        val dateRange = 30
        provider.settings = settings.copy(eventRange = dateRange)
        playResults(method)
        val today = provider.executedAt.withTimeAtStartOfDay()
        val endOfRangeTime = today.plusDays(dateRange)
        var dayOfEventEntryPrev = 0
        var dayOfHeaderPrev = 0
        factory.widgetEntries.forEachIndexed { ind, entry ->
            val logMsg = method + "; " + String.format("%02d ", ind) + entry.toString()
            if (entry.entryDay.isBefore(today)) {
                fail("Is present before today $logMsg")
            }
            if (entry.entryDay.isAfter(endOfRangeTime)) {
                fail("After end of range $logMsg")
            }
            val dayOfEntry = entry.entryDay.dayOfYear
            if (entry is DayHeader) {
                if (dayOfHeaderPrev == 0) {
                    if (entry.entryDate.withTimeAtStartOfDay().isAfter(today)) {
                        fail("No today's header $logMsg")
                    }
                } else {
                    withClue("No header $logMsg") {
                        dayOfEntry shouldBe dayOfHeaderPrev + 1
                    }
                }
                dayOfHeaderPrev = dayOfEntry
            } else if (entry is LastEntry) {
                entry.type shouldBe LastEntryType.END_OF_LIST
            } else if (entry is CalendarEntry) {
                if (dayOfEventEntryPrev == 0) {
                    if (entry.entryDate.withTimeAtStartOfDay().isAfter(today)) {
                        fail("Today not filled $logMsg")
                    }
                    withClue("Day of event") {
                        entry.event.dayOfEvent(entry.entryDay) shouldBe 4
                    }
                    withClue("Days of event") {
                        entry.event.daysOfEvent shouldBe 37
                    }
                } else {
                    withClue("Day not filled $logMsg") {
                        dayOfEntry shouldBe dayOfEventEntryPrev + 1
                    }
                }
                dayOfEventEntryPrev = dayOfEntry
            }
        }
        withClue("Wrong last day header $method") {
            dayOfHeaderPrev shouldBe endOfRangeTime.dayOfYear
        }
        withClue("Wrong last filled day $method") {
            dayOfEventEntryPrev shouldBe endOfRangeTime.dayOfYear
        }
    }
}
