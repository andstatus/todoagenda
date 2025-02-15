package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.TasksWithoutDates
import org.andstatus.todoagenda.widget.WidgetEntry
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class RealTasksTest : BaseWidgetTest() {
    @Test
    fun testTasksWithoutStartAndDueDates() {
        val method = "testTasksWithoutStartAndDueDates"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.real_tasks)
        playResults(method)
        Assert.assertTrue(
            "No End of list entries although filters are off",
            factory.widgetEntries
                .stream()
                .anyMatch { entry: WidgetEntry -> entry.entryPosition == WidgetEntryPosition.END_OF_LIST },
        )
        provider.settings =
            settings.copy(
                filterModeIn = FilterMode.DEBUG_FILTER,
                taskWithoutDates = TasksWithoutDates.HIDE,
            )
        playResults(method)
        Assert.assertFalse(
            "End of list entries although filters are on",
            factory.widgetEntries
                .stream()
                .anyMatch { entry: WidgetEntry -> entry.entryPosition == WidgetEntryPosition.END_OF_LIST },
        )
    }
}
