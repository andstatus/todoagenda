package org.andstatus.todoagenda

import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class DefaultColorsEventsTest : BaseWidgetTest() {
    /**
     * [#327 Today's event color...](https://github.com/plusonelabs/calendar-widget/issues/327)
     * Updated results file to have an ongoing event also.
     * Default colors will be shown (no colors in the JSON file)
     */
    @Test
    fun testIssue327() {
        val method = "testIssue327"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.default_colors)
        playResults(method)
        Assert.assertEquals("Number of entries", 14, factory.widgetEntries.size.toLong())
    }
}
