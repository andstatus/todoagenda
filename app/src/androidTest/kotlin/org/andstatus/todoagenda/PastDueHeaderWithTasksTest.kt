package org.andstatus.todoagenda

import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntryType
import org.andstatus.todoagenda.widget.TaskEntry
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.junit.Assert
import org.junit.Test

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
        Assert.assertEquals("Past and Due header", MyClock.DATETIME_MIN, factory.widgetEntries[0].entryDate)
        Assert.assertEquals(WidgetEntryPosition.PAST_AND_DUE_HEADER, factory.widgetEntries[0].entryPosition)
        Assert.assertEquals("Closest event: Past Calendar Entry", "Call John Smith", factory.widgetEntries[1].title)
        Assert.assertEquals("Due task Entry", "Due task 002, without start date", factory.widgetEntries[2].title)
        Assert.assertEquals(
            "Due task Entry",
            dateTime(2019, 8, 1, 9, 0),
            factory.widgetEntries[2].entryDate,
        )
        Assert.assertEquals(
            "Tomorrow header",
            dateTime(2019, 8, 5),
            factory.widgetEntries[3].entryDate,
        )
        Assert.assertEquals("Future task Entry", TaskEntry::class.java, factory.widgetEntries[6].javaClass)
        Assert.assertEquals(
            "Future task Entry",
            dateTime(2019, 8, 8, 21, 0),
            factory.widgetEntries[6].entryDate,
        )
        Assert.assertEquals("End of list header", MyClock.DATETIME_MAX, factory.widgetEntries[7].entryDate)
        Assert.assertEquals(WidgetEntryPosition.END_OF_LIST_HEADER, factory.widgetEntries[7].entryPosition)
        Assert.assertEquals(WidgetEntryPosition.END_OF_LIST, factory.widgetEntries[8].entryPosition)
        Assert.assertEquals(
            "Last Entry",
            LastEntryType.END_OF_LIST,
            (factory.widgetEntries[factory.widgetEntries.size - 1] as LastEntry).type,
        )
        Assert.assertEquals("Number of entries", 10, factory.widgetEntries.size.toLong())
    }
}
