package org.andstatus.todoagenda

import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class DuplicateEventsTest : BaseWidgetTest() {
    /**
     * https://github.com/plusonelabs/calendar-widget/issues/354
     */
    @Test
    fun testIssue354() {
        val method = "testIssue354"
        val inputs = provider!!.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.duplicates
        )
        provider!!.addResults(inputs)
        playResults(method)
        Assert.assertEquals("Number of entries", 40, getFactory().widgetEntries.size.toLong())
    }
}
