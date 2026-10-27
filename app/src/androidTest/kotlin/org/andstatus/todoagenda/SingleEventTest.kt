package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.widget.CalendarEntry
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * @author yvolk@yurivolkov.com
 */
class SingleEventTest : BaseWidgetTest() {
    private var eventId = 0L

    @Test
    fun testEventAttributes() {
        provider.settings = settings.copy(showCurrentTimeLine = false)
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Single Event today with all known attributes",
                startDateIn = today.plusHours(12),
                endDateIn = today.plusHours(13),
                color = -0x6d1e40,
                location = "somewhere",
                isAlarmActive = true,
                isRecurring = true,
            )
        val executedAt = today.plusHours(10)
        assertOneEvent(executedAt, event, true)
        val event2 = event.copy(isAlarmActive = false)
        assertOneEvent(executedAt, event2, true)
        assertOneEvent(
            executedAt,
            event2.copy(isRecurring = false),
            true,
        )
    }

    @Test
    fun testAlldayEventAttributes() {
        provider.settings = settings.copy(showCurrentTimeLine = false)
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = true,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Single AllDay event today with all known attributes",
                startDateIn = today.minusDays(1),
                endDateIn = today.plusDays(1),
                color = -0x6d1e40,
                location = "somewhere",
            )
        val executedAt = today.plusHours(10)
        assertOneEvent(executedAt, event, false)
        val event2 =
            event.copy(
                startDateIn = today,
                endDateIn = today.plusDays(1),
            )
        assertOneEvent(executedAt, event2, true)
    }

    @Test
    fun testAlldayEventMillis() {
        val today = settings.clock.now(DateTimeZone.UTC).withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = true,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Single All day event from millis",
                startMillisIn = today.millis,
            )
        withClue(event.startDate.toString()) {
            event.startMillis shouldBe today.millis
        }
        withClue(event.endDate.toString()) {
            event.endMillis shouldBe today.plusDays(1).millis
        }
    }

    private fun assertOneEvent(
        executedAt: DateTime,
        event: CalendarEvent,
        equal: Boolean,
    ) {
        provider.clear()
        provider.setExecutedAt(executedAt)
        provider.addRow(event)
        playResults(TAG)
        withClue(settings.toString()) {
            settings.getActiveEventSources(EventProviderType.CALENDAR).isEmpty() shouldBe false
        }
        val source = provider.firstActiveEventSource
        withClue(source.toString()) {
            source.source.isAvailable shouldBe true
        }
        withClue(settings.toString()) {
            settings
                .getActiveEventSource(
                    EventProviderType.CALENDAR,
                    source.source.id,
                ).source.isAvailable shouldBe true
        }
        withClue(factory.widgetEntries.toString()) {
            factory.widgetEntries.size shouldBe 3
        }
        val entry = factory.widgetEntries[1].shouldBeInstanceOf<CalendarEntry>()
        val eventOut = entry.event
        val msgLog =
            """
            Comparing events:
            in: $event
            out:$eventOut
            
            """.trimIndent()
        if (equal) {
            withClue(msgLog) {
                eventOut.toString() shouldBe event.toString()
            }
        } else {
            withClue(msgLog) {
                eventOut.toString() shouldNotBeSameInstanceAs event.toString()
            }
        }
    }
}
