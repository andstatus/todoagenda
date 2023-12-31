package org.andstatus.todoagenda

import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class WeirdDayHeaderTest : BaseWidgetTest() {
    @Test
    fun testWeirdDayHeader() {
        val method = "testWeirdDayHeader"
        val inputs = provider!!.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.weird_day_header
        )
        provider!!.addResults(inputs)
        playResults(method)
        assertPosition(0, WidgetEntryPosition.END_OF_LIST_HEADER)
        assertPosition(1, WidgetEntryPosition.END_OF_LIST)
        assertPosition(2, WidgetEntryPosition.END_OF_LIST)
        assertPosition(3, WidgetEntryPosition.LIST_FOOTER)
    }
}
