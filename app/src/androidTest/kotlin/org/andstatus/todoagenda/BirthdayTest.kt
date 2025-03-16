package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.AllDayEventsPlacement
import org.andstatus.todoagenda.prefs.EndedSomeTimeAgo
import org.andstatus.todoagenda.prefs.LastEntryAppearance
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntryType
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class BirthdayTest : BaseWidgetTest() {
    @Test
    fun testBirthdayOneDayOnly() {
        val inputs = provider.loadResultsAndSettings(org.andstatus.todoagenda.test.R.raw.birthday)
        provider.settings =
            settings.copy(
                widgetHeaderDateFormat = DateFormatValue.of(DateFormatType.CUSTOM, "YYYY-MM-dd"),
                allDayEventsPlacement = AllDayEventsPlacement.TOP_DAY,
                eventsEnded = EndedSomeTimeAgo.NONE,
                showPastEventsWithDefaultColor = false,
                eventRange = 30,
                showCurrentTimeLine = false,
            )
        playAtOneTime(inputs, dateTime(2015, 8, 1, 17, 0), 0, LastEntryType.NO_UPCOMING)
        playAtOneTime(inputs, dateTime(2015, 8, 9, 23, 59), 0, LastEntryType.NO_UPCOMING)
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 0).plusMillis(1), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 1), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 8, 17, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 8, 23, 30), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 0, 30), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 11, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 17, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 23, 30), 2, LastEntryType.END_OF_LIST)
        provider.settings =
            settings.copy(
                lastEntryAppearance = LastEntryAppearance.HIDDEN,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 9, 23, 30), 2, null)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 0, null)
        provider.settings =
            settings.copy(
                lastEntryAppearance = LastEntryAppearance.WITH_MESSAGE,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 0, LastEntryType.NO_UPCOMING)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 0, LastEntryType.NO_UPCOMING)
        provider.settings =
            settings.copy(
                eventsEnded = EndedSomeTimeAgo.ONE_HOUR,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0, LastEntryType.NO_EVENTS)
        provider.settings =
            settings.copy(
                eventsEnded = EndedSomeTimeAgo.TODAY,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0, LastEntryType.NO_EVENTS)
        provider.settings =
            settings.copy(
                eventsEnded = EndedSomeTimeAgo.FOUR_HOURS,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 3, 59), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 0, LastEntryType.NO_EVENTS)
        provider.settings =
            settings.copy(
                eventsEnded = EndedSomeTimeAgo.YESTERDAY,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 17, 0), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 23, 30), 2, LastEntryType.END_OF_LIST)
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 0), 0, LastEntryType.NO_EVENTS)
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0, LastEntryType.NO_EVENTS)
        provider.settings =
            settings.copy(
                showPastEventsWithDefaultColor = true,
            )
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0, LastEntryType.NO_EVENTS)
    }

    private fun playAtOneTime(
        inputs: QueryResultsStorage,
        now: DateTime?,
        entriesWithoutLastExpected: Int,
        lastEntryType: LastEntryType?,
    ) {
        inputs.executedAt.set(now)
        provider.clear()
        provider.addResults(inputs)
        playResults(TAG)
        Assert.assertEquals(
            (entriesWithoutLastExpected + (if (lastEntryType == null) 0 else 1)),
            factory.widgetEntries.size,
        )
        if (entriesWithoutLastExpected > 0) {
            val birthday = factory.widgetEntries[1] as CalendarEntry
            Assert.assertEquals(
                9,
                birthday.entryDate
                    .dayOfMonth()
                    .get()
                    .toLong(),
            )
            Assert.assertEquals(
                0,
                birthday.entryDate
                    .hourOfDay()
                    .get()
                    .toLong(),
            )
            Assert.assertEquals(
                0,
                birthday.entryDate
                    .minuteOfHour()
                    .get()
                    .toLong(),
            )
            Assert.assertEquals(
                0,
                birthday.entryDate
                    .millisOfDay()
                    .get()
                    .toLong(),
            )
            Assert.assertEquals(true, birthday.allDay)
        }
        if (lastEntryType != null) {
            val entry = factory.widgetEntries[factory.widgetEntries.size - 1]
            val lastEntry = entry as? LastEntry
            Assert.assertEquals("Last entry: $entry", lastEntryType, lastEntry?.type)
        }
    }
}
