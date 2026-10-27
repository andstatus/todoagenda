package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntryType
import org.andstatus.todoagenda.widget.TaskEntry
import org.andstatus.todoagenda.widget.WidgetEntryPosition

/**
 * @author yvolk@yurivolkov.com
 */
class PastDueHeaderWithTasksTest : BaseWidgetTest() {
    /**
     * [Issue 205](https://github.com/plusonelabs/calendar-widget/issues/205)
     */
    @Test
    fun testPastDueHeaderWithTasks() {
        val method = "testPastDueHeaderWithTasks"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.past_due_header_with_tasks)
        provider.settings = settings.copy(showCurrentTimeLine = false)
        playResults(method)
        withClue("Past and Due header") {
            factory.widgetEntries[0].entryDate shouldBe MyClock.DATETIME_MIN
        }
        factory.widgetEntries[0].entryPosition shouldBe WidgetEntryPosition.PAST_AND_DUE_HEADER
        withClue("Closest event: Past Calendar Entry") {
            factory.widgetEntries[1].title shouldBe "Call John Smith"
        }
        withClue("Due task Entry") {
            factory.widgetEntries[2].title shouldBe "Due task 002, without start date"
        }
        withClue("Due task Entry") {
            factory.widgetEntries[2].entryDate shouldBe dateTime(2019, 8, 1, 9, 0)
        }
        withClue("Tomorrow header") {
            factory.widgetEntries[3].entryDate shouldBe dateTime(2019, 8, 5)
        }
        withClue("Future task Entry") {
            factory.widgetEntries[6].javaClass shouldBe TaskEntry::class.java
        }
        withClue("Future task Entry") {
            factory.widgetEntries[6].entryDate shouldBe dateTime(2019, 8, 8, 21, 0)
        }
        withClue("End of list header") {
            factory.widgetEntries[7].entryDate shouldBe MyClock.DATETIME_MAX
        }
        factory.widgetEntries[7].entryPosition shouldBe WidgetEntryPosition.END_OF_LIST_HEADER
        factory.widgetEntries[8].entryPosition shouldBe WidgetEntryPosition.END_OF_LIST
        withClue("Last Entry") {
            (factory.widgetEntries[factory.widgetEntries.size - 1] as LastEntry).type shouldBe
                LastEntryType.END_OF_LIST
        }
        withClue("Number of entries") {
            factory.widgetEntries.size shouldBe 10
        }
    }
}
