package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.WidgetEntry
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
        withClue("SnaphotDate") {
            settings.clock.now().withTimeAtStartOfDay() shouldBe dateTime(2020, 2, 15)
        }
        val entries =
            factory.widgetEntries
                .stream()
                .filter { e: WidgetEntry -> e.title.startsWith("Test event 2 that") }
                .collect(Collectors.toList())
        withClue("Number of entries of the test event $entries") {
            entries.size shouldBe 2
        }
        withClue("Entries should have different IDs\n$entries\n") {
            (entries[1] as CalendarEntry).event.eventId shouldNotBe
                (entries[0] as CalendarEntry).event.eventId
        }
    }
}
