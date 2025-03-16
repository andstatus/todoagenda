package org.andstatus.todoagenda

import org.andstatus.todoagenda.widget.CalendarEntry
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class WrongDatesLostEventsTest : BaseWidgetTest() {
    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    @Test
    fun testIssue205() {
        val method = "testIssue205"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.wrong_dates_lost_events)
        provider.settings = settings.copy(showCurrentTimeLine = false)
        playResults(method)
        Assert.assertEquals("Number of entries", 11, factory.widgetEntries.size.toLong())
        Assert.assertEquals("On Saturday", "Maker Fair", (factory.widgetEntries[4] as CalendarEntry).event.title)
        Assert.assertEquals(
            "On Saturday",
            6,
            factory.widgetEntries[4]
                .entryDate.dayOfWeek
                .toLong(),
        )
        Assert.assertEquals("On Sunday", "Ribakovs", (factory.widgetEntries[7] as CalendarEntry).event.title)
        Assert.assertEquals(
            "On Sunday",
            7,
            factory.widgetEntries[7]
                .entryDate.dayOfWeek
                .toLong(),
        )
    }
}
