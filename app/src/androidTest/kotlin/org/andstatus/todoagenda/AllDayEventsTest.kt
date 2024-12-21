package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.AllDayEventsPlacement
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class AllDayEventsTest : BaseWidgetTest() {
    @Test
    fun testAllDayEventsPlacement() {
        val method = "testAllDayEventsPlacement"
        val inputs = provider.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.all_day_tasks
        )
        provider.addResults(inputs)
        playResults(method)
        assertPosition(8, WidgetEntryPosition.END_OF_DAY)
        assertPosition(9, WidgetEntryPosition.END_OF_DAY)
        assertPosition(10, WidgetEntryPosition.DAY_HEADER)
        assertPosition(11, WidgetEntryPosition.END_OF_DAY)
        provider.settings = settings.copy(
            allDayEventsPlacement = AllDayEventsPlacement.TOP_DAY
        )
        playResults(method)
        assertPosition(1, WidgetEntryPosition.START_OF_DAY)
        assertPosition(2, WidgetEntryPosition.START_OF_DAY)
        assertPosition(10, WidgetEntryPosition.DAY_HEADER)
        assertPosition(11, WidgetEntryPosition.START_OF_DAY)
    }
}
