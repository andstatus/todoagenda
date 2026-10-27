package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.TasksWithoutDates
import org.andstatus.todoagenda.widget.WidgetEntry
import org.andstatus.todoagenda.widget.WidgetEntryPosition

/**
 * @author yvolk@yurivolkov.com
 */
class RealTasksTest : BaseWidgetTest() {
    @Test
    fun testTasksWithoutStartAndDueDates() {
        val method = "testTasksWithoutStartAndDueDates"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.real_tasks)
        playResults(method)
        withClue("No End of list entries although filters are off") {
            val hasEndOfList =
                factory.widgetEntries
                    .stream()
                    .anyMatch { entry: WidgetEntry -> entry.entryPosition == WidgetEntryPosition.END_OF_LIST }
            hasEndOfList shouldBe true
        }
        provider.settings =
            settings.copy(
                filterModeIn = FilterMode.DEBUG_FILTER,
                taskWithoutDates = TasksWithoutDates.HIDE,
            )
        playResults(method)
        withClue("End of list entries although filters are on") {
            val hasEndOfList =
                factory.widgetEntries
                    .stream()
                    .anyMatch { entry: WidgetEntry -> entry.entryPosition == WidgetEntryPosition.END_OF_LIST }
            hasEndOfList shouldBe false
        }
    }
}
