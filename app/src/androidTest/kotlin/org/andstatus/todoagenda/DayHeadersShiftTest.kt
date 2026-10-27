package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.prefs.TestLocale
import org.andstatus.todoagenda.widget.DayHeader

/**
 * @author yvolk@yurivolkov.com
 */
class DayHeadersShiftTest : BaseWidgetTest() {
    @Test
    fun testDayHeadersShift() {
        val method = "testDayHeadersShift"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.day_headers_shift)
        playResults(method)
        val dayHeader0 = factory.widgetEntries[0] as DayHeader
        withClue("First day header should be Jan 8 ${factory.widgetEntries}") {
            dayHeader0.entryDate.dayOfMonth().get() shouldBe 8
        }

        TestLocale.withEnUsLocale {
            val dayHeaderTitle = settings.dayHeaderDateFormatter().formatDate(dayHeader0.entryDate)
            withClue("First day header should show Jan 8 ${factory.widgetEntries}") {
                dayHeaderTitle shouldBe "Wednesday, January 8, 2020"
            }
        }
    }
}
