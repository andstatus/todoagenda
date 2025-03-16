package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.widget.LastEntryType
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 * https://github.com/andstatus/todoagenda/issues/102
 */
class TomorrowsTasksInTodaysFilterTest : BaseWidgetTest() {
    @Test
    fun testIssue102() {
        val method = "testIssue102"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.tomorrows_tasks_one_week)
        playResults(method)
        assertLastEntry(12, LastEntryType.END_OF_LIST)
        provider.settings =
            settings.copy(
                eventRange = InstanceSettings.EVENT_RANGE_TODAY,
            )
        playResults(method)
        assertLastEntry(0, LastEntryType.NO_EVENTS)
        provider.settings =
            settings.copy(
                eventRange = InstanceSettings.EVENT_RANGE_TODAY_AND_TOMORROW,
            )
        playResults(method)
        assertLastEntry(9, LastEntryType.END_OF_LIST)
    }
}
