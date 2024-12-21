package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 * https://github.com/andstatus/todoagenda/issues/102
 */
class TomorrowsTasksInTodaysFilterTest : BaseWidgetTest() {
    @Test
    fun testIssue102() {
        val method = "testIssue102"
        val inputs = provider.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.tomorrows_tasks_one_week
        )
        provider.addResults(inputs)
        playResults(method)
        assertPosition(10, WidgetEntryPosition.LIST_FOOTER)
        provider.settings = settings.copy(
            eventRange = InstanceSettings.EVENT_RANGE_TODAY
        )
        playResults(method)
        assertPosition(0, WidgetEntryPosition.LIST_FOOTER)
        provider.settings = settings.copy(
            eventRange = InstanceSettings.EVENT_RANGE_TODAY_AND_TOMORROW
        )
        playResults(method)
        assertPosition(7, WidgetEntryPosition.LIST_FOOTER)
    }
}
