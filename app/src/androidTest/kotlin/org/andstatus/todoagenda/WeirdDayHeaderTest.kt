package org.andstatus.todoagenda

import org.andstatus.todoagenda.widget.LastEntryType
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class WeirdDayHeaderTest : BaseWidgetTest() {
    @Test
    fun testWeirdDayHeader() {
        val method = "testWeirdDayHeader"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.weird_day_header)
        provider.settings = settings.copy(showCurrentTimeLine = false)
        playResults(method)
        assertPosition(0, WidgetEntryPosition.END_OF_LIST_HEADER)
        assertPosition(1, WidgetEntryPosition.END_OF_LIST)
        assertPosition(2, WidgetEntryPosition.END_OF_LIST)
        assertLastEntry(3, LastEntryType.END_OF_LIST)
    }
}
