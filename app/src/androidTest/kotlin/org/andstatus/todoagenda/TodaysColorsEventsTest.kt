package org.andstatus.todoagenda

import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class TodaysColorsEventsTest : BaseWidgetTest() {
    /**
     * https://github.com/plusonelabs/calendar-widget/issues/327
     */
    @Test
    fun testIssue327() {
        val method = "testIssue327"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.todays_colors)
        playResults(method)
        Assert.assertEquals("Number of entries", 43, factory.widgetEntries.size.toLong())
    }
}
