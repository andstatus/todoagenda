package org.andstatus.todoagenda

import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.WidgetEntry
import org.junit.Assert
import org.junit.Test
import java.util.stream.Collectors

/**
 * @author yvolk@yurivolkov.com
 */
class ShowOnlyClosestInstanceTest : BaseWidgetTest() {
    @Test
    fun testShowOnlyClosestInstance() {
        val method = "testShowOnlyClosestInstance"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.closest_event)
        playResults(method)
        Assert.assertEquals(
            "SnaphotDate",
            dateTime(2020, 2, 15),
            settings.clock.now().withTimeAtStartOfDay(),
        )
        val entries =
            factory.widgetEntries
                .stream()
                .filter { e: WidgetEntry -> e.title.startsWith("Test event 2 that") }
                .collect(Collectors.toList())
        Assert.assertEquals("Number of entries of the test event $entries", 2, entries.size.toLong())
        Assert.assertNotEquals(
            "Entries should have different IDs\n$entries\n",
            (entries[0] as CalendarEntry).event.eventId,
            (entries[1] as CalendarEntry).event.eventId,
        )
    }
}
