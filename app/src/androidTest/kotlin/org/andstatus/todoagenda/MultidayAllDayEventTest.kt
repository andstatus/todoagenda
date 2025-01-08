package org.andstatus.todoagenda

import org.andstatus.todoagenda.widget.DayHeader
import org.andstatus.todoagenda.widget.LastEntry
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class MultidayAllDayEventTest : BaseWidgetTest() {
    @Test
    fun testInsidePeriod() {
        val method = "testInsidePeriod"
        val inputs = provider.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.multi_day
        )
        val now = DateTime(2015, 8, 30, 0, 0, 1, 0, settings.timeZone)
        inputs.executedAt.set(now)
        provider.addResults(inputs)
        val dateRange = 30
        provider.settings = settings.copy(eventRange = dateRange)
        playResults(method)
        val today = now.withTimeAtStartOfDay()
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
                        dayOfEntry.toLong()
                    )
                }
                dayOfEventEntryPrev = dayOfEntry
            }
        }
        Assert.assertEquals(
            "Wrong last day header $method",
            endOfRangeTime.dayOfYear.toLong(),
            dayOfHeaderPrev.toLong()
        )
        Assert.assertEquals(
            "Wrong last filled day $method",
            endOfRangeTime.dayOfYear.toLong(),
            dayOfEventEntryPrev.toLong()
        )
    }
}
