package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.widget.CalendarEntry

/**
 * @author yvolk@yurivolkov.com
 */
class WrongDatesLostEventsTest : BaseWidgetTest() {
    /**
     * https://github.com/plusonelabs/calendar-widget/issues/205
     */
    @Test
    fun testIssue205() {
        val method = "testIssue205"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.wrong_dates_lost_events)
        provider.settings = settings.copy(showCurrentTimeLine = false)
        playResults(method)
        withClue("Number of entries") {
            factory.widgetEntries.size shouldBe 11
        }
        withClue("On Saturday") {
            (factory.widgetEntries[4] as CalendarEntry).event.title shouldBe "Maker Fair"
        }
        withClue("On Saturday") {
            factory.widgetEntries[4].entryDate.dayOfWeek shouldBe 6
        }
        withClue("On Sunday") {
            (factory.widgetEntries[7] as CalendarEntry).event.title shouldBe "Ribakovs"
        }
        withClue("On Sunday") {
            factory.widgetEntries[7].entryDate.dayOfWeek shouldBe 7
        }
    }
}
