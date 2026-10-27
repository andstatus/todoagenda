package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.provider.QueryRow
import org.andstatus.todoagenda.widget.CalendarEntry
import java.util.concurrent.TimeUnit

/**
 * @author yvolk@yurivolkov.com
 */
class RecurringEventsTest : BaseWidgetTest() {
    private var eventId = 0

    /**
     * See [Issue 191](https://github.com/plusonelabs/calendar-widget/issues/191) and
     * [Issue 46](https://github.com/plusonelabs/calendar-widget/issues/46)
     */
    @Test
    fun testShowRecurringEvents() {
        generateEventInstances()
        withClue("Entries: " + factory.widgetEntries.size) {
            countCalendarEntries() shouldBe 15
        }
        provider.settings =
            settings.copy(
                showOnlyClosestInstanceOfRecurringEvent = true,
            )
        generateEventInstances()
        withClue("Entries: " + factory.widgetEntries.size) {
            countCalendarEntries() shouldBe 1
        }
    }

    fun countCalendarEntries(): Int {
        var count = 0
        for (widgetEntry in factory.widgetEntries) {
            if (CalendarEntry::class.java.isAssignableFrom(widgetEntry.javaClass)) {
                count++
            }
        }
        return count
    }

    fun generateEventInstances() {
        provider.clear()
        val date = settings.clock.now().withTimeAtStartOfDay()
        var millis = date.millis + TimeUnit.HOURS.toMillis(10)
        eventId++
        for (ind in 0..14) {
            millis += TimeUnit.DAYS.toMillis(1)
            provider.addRow(
                QueryRow()
                    .setEventId(eventId)
                    .setTitle("Work each day")
                    .setBegin(millis)
                    .setEnd(millis + TimeUnit.HOURS.toMillis(9)),
            )
        }
        playResults(BaseWidgetTest.Companion.TAG)
    }
}
