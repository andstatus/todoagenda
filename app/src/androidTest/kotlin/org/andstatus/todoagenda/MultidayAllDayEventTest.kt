package org.andstatus.todoagenda

import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.DayHeader
import org.andstatus.todoagenda.widget.LastEntry
import org.junit.Assert
import org.junit.Test

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
        for (ind in factory.widgetEntries.indices) {
            val entry = factory.widgetEntries[ind]
            val logMsg = method + "; " + String.format("%02d ", ind) + entry.toString()
            if (entry.entryDay.isBefore(today)) {
                Assert.fail("Is present before today $logMsg")
            }
            if (entry.entryDay.isAfter(endOfRangeTime)) {
                Assert.fail("After end of range $logMsg")
            }
            val dayOfEntry = entry.entryDay.dayOfYear
            if (entry is DayHeader) {
                if (dayOfHeaderPrev == 0) {
                    if (entry.entryDate.withTimeAtStartOfDay().isAfter(today)) {
                        Assert.fail("No today's header $logMsg")
                    }
                } else {
                    Assert.assertEquals("No header $logMsg", (dayOfHeaderPrev + 1).toLong(), dayOfEntry.toLong())
                }
                dayOfHeaderPrev = dayOfEntry
            } else if (entry is LastEntry) {
                Assert.assertEquals(LastEntry.LastEntryType.LAST, entry.type)
            } else {
                if (dayOfEventEntryPrev == 0) {
                    if (entry.entryDate.withTimeAtStartOfDay().isAfter(today)) {
                        Assert.fail("Today not filled $logMsg")
                    }
                } else {
                    Assert.assertEquals(
                        "Day not filled $logMsg",
                        (dayOfEventEntryPrev + 1).toLong(),
                        dayOfEntry.toLong(),
                    )
                }
                dayOfEventEntryPrev = dayOfEntry
            }
        }
        Assert.assertEquals(
            "Wrong last day header $method",
            endOfRangeTime.dayOfYear.toLong(),
            dayOfHeaderPrev.toLong(),
        )
        Assert.assertEquals(
            "Wrong last filled day $method",
            endOfRangeTime.dayOfYear.toLong(),
            dayOfEventEntryPrev.toLong(),
        )
    }

    @Test
    fun testMultiDayOngoingEvent() {
        val method = "testMultiDayOngoingEvent"
        provider.loadResultsAndSettings(R.raw.multi_day_ongoing)
        playResults(method)
    }
}
