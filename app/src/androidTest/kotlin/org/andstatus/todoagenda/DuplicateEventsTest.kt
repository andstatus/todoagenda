package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe

/**
 * @author yvolk@yurivolkov.com
 */
class DuplicateEventsTest : BaseWidgetTest() {
    /**
     * https://github.com/plusonelabs/calendar-widget/issues/354
     */
    @Test
    fun testIssue354() {
        val method = "testIssue354"
        provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.duplicates)
        playResults(method)
        withClue("Number of entries") {
            factory.widgetEntries.size shouldBe 42
        }
    }
}
