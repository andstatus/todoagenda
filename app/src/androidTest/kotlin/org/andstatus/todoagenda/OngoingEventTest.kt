package org.andstatus.todoagenda

import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.andstatus.todoagenda.calendar.CalendarEvent
import org.andstatus.todoagenda.test.R
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.WidgetEntry

/**
 * @author yvolk@yurivolkov.com
 */
class OngoingEventTest : BaseWidgetTest() {
    private var eventId = 0L

    /**
     * See [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testTodaysOngoingEvent() {
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Ongoing event shows original start time",
                startDateIn = today.plusHours(9),
                endDateIn = today.plusHours(12),
            )
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        entry.shouldNotBeNull()
        withClue("Is active event") {
            entry!!.event.isOngoing shouldBe true
        }
        withClue("Is not part of Multi Day Event") {
            entry!!.isPartOfMultiDayEvent shouldBe false
        }
        withClue("Start Time didn't change for today's event") {
            entry!!.entryDate shouldBe event.startDate
        }
        withClue("End Time didn't change for today's event") {
            entry!!.endDate shouldBe event.endDate
        }
    }

    /**
     * See [Issue 199](https://github.com/plusonelabs/calendar-widget/issues/199)
     */
    @Test
    fun testYesterdaysOngoingEvent() {
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Ongoing event, which started yesterday, shows no start time",
                startDateIn = today.minusDays(1).plusHours(9),
                endDateIn = today.plusHours(12),
            )
        provider.setExecutedAt(today.plusHours(10).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
            }
        }
        entry.shouldNotBeNull()
        withClue("Is active event") {
            entry!!.event.isOngoing shouldBe true
        }
        withClue("Is Part of Multi Day Event") {
            entry!!.isPartOfMultiDayEvent shouldBe true
        }
        withClue("Is not start of Multi Day Event") {
            entry!!.isStartOfMultiDayEvent shouldBe false
        }
        withClue("Is end of Multi Day Event") {
            entry!!.isEndOfMultiDayEvent shouldBe true
        }
        withClue("Yesterday's event entry start time is midnight") {
            entry!!.entryDate shouldBe today
        }
        withClue("End Time didn't change for yesterday's event") {
            entry!!.endDate shouldBe event.endDate
        }
    }

    @Test
    fun testEventWhichCarryOverToTheNextDay() {
        val today = settings.clock.now().withTimeAtStartOfDay()
        val event =
            CalendarEvent(
                settings = settings,
                context = provider.context,
                isAllDay = false,
                eventSource = provider.firstActiveEventSource,
                eventId = ++eventId,
                title = "Event that carry over to the next day, show as ending midnight",
                startDateIn = today.plusHours(19),
                endDateIn = today.plusDays(1).plusHours(7),
            )
        provider.setExecutedAt(today.plusHours(20).plusMinutes(33))
        provider.addRow(event)
        playResults(TAG)
        var entry: CalendarEntry? = null
        for (item in factory.widgetEntries) {
            if (item is CalendarEntry) {
                entry = item
                break
            }
        }
        entry.shouldNotBeNull()
        withClue("Is active event") {
            entry!!.event.isOngoing shouldBe true
        }
        withClue("Is Part of Multi Day Event") {
            entry!!.isPartOfMultiDayEvent shouldBe true
        }
        withClue("Is start of Multi Day Event") {
            entry!!.isStartOfMultiDayEvent shouldBe true
        }
        withClue("Is not an end of Multi Day Event") {
            entry!!.isEndOfMultiDayEvent shouldBe false
        }
        withClue("Start Time didn't change for today's event") {
            entry!!.entryDate shouldBe event.startDate
        }
        withClue("Entry end time is the same as Event end time") {
            entry!!.endDate shouldBe event.endDate
        }
    }

    @Test
    fun testMultiDayOngoingEvents() {
        val method = "testMultiDayOngoingEvents"
        provider.loadResultsAndSettings(R.raw.multi_day_ongoing)
        playResults(method)
        val thisDay = settings.clock.dayOf(provider.executedAt)
        thisDay shouldBe settings.clock.dayOf(2025, 2, 8)
        settings.startHourOfDay shouldBe 2
        val title = "Event that lasts till next week"
        val entries: List<WidgetEntry> = factory.widgetEntries.filter { it.title == title }
        withClue("Only one entry") {
            entries.size shouldBe 1
        }
        val entry = entries[0].shouldBeCalendarEntry
        entry.entryDay shouldBe thisDay
        val event = entry.event
        withClue("Days of event $event, ${event.firstDay} - ${event.lastDay}") {
            event.daysOfEvent shouldBe 4
        }
        withClue("The second day of event") {
            event.dayOfEvent(entry.entryDay) shouldBe 2
        }
    }
}
