package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import org.andstatus.todoagenda.prefs.AllDayEventsPlacement
import org.andstatus.todoagenda.widget.WidgetEntryPosition

/**
 * @author yvolk@yurivolkov.com
 */
class AllDayEventsTest : BaseWidgetTest() {
    @Test
    fun testAllDayEventsPlacement() {
        val method = "testAllDayEventsPlacement"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.all_day_tasks)
        playResults(method)
        assertPosition(8, WidgetEntryPosition.END_OF_DAY)
        assertPosition(9, WidgetEntryPosition.END_OF_DAY)
        assertPosition(10, WidgetEntryPosition.DAY_HEADER)
        withClue("Current time line") {
            assertPosition(11, WidgetEntryPosition.ENTRY_DATE)
        }
        assertPosition(12, WidgetEntryPosition.END_OF_DAY)
        provider.settings =
            settings.copy(
                allDayEventsPlacement = AllDayEventsPlacement.TOP_DAY,
            )
        playResults(method)
        assertPosition(1, WidgetEntryPosition.START_OF_DAY)
        assertPosition(2, WidgetEntryPosition.START_OF_DAY)
        assertPosition(10, WidgetEntryPosition.DAY_HEADER)
        assertPosition(11, WidgetEntryPosition.START_OF_DAY)
        withClue("Current time line") {
            assertPosition(12, WidgetEntryPosition.ENTRY_DATE)
        }
    }
}
