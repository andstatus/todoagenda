package org.andstatus.todoagenda

import org.andstatus.todoagenda.prefs.AllDayEventsPlacement
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.EndedSomeTimeAgo
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.LastEntry
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class BirthdayTest : BaseWidgetTest() {
    @Test
    fun testBirthdayOneDayOnly() {
        val inputs = provider!!.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.birthday
        )
        provider!!.startEditingPreferences()
        ApplicationPreferences.setWidgetHeaderDateFormat(
            provider.context,
            DateFormatValue.of(DateFormatType.CUSTOM, "YYYY-MM-dd")
        )
        ApplicationPreferences.setAllDayEventsPlacement(provider.context, AllDayEventsPlacement.TOP_DAY)
        ApplicationPreferences.setEventsEnded(provider.context, EndedSomeTimeAgo.NONE)
        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.context, false)
        ApplicationPreferences.setEventRange(provider.context, 30)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 8, 1, 17, 0), 0)
        playAtOneTime(inputs, dateTime(2015, 8, 9, 23, 59), 0)
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 0).plusMillis(1), 2)
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 1), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 8, 17, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 8, 23, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 0, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 11, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 17, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 9, 23, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 0)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 0)
        ApplicationPreferences.setEventsEnded(provider.context, EndedSomeTimeAgo.ONE_HOUR)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0)
        ApplicationPreferences.setEventsEnded(provider.context, EndedSomeTimeAgo.TODAY)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0)
        ApplicationPreferences.setEventsEnded(provider.context, EndedSomeTimeAgo.FOUR_HOURS)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 3, 59), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 0)
        ApplicationPreferences.setEventsEnded(provider.context, EndedSomeTimeAgo.YESTERDAY)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 17, 0), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 10, 23, 30), 2)
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 0), 0)
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0)
        ApplicationPreferences.setShowPastEventsWithDefaultColor(provider.context, true)
        provider!!.savePreferences()
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0)
    }

    private fun playAtOneTime(inputs: QueryResultsStorage?, now: DateTime?, entriesWithoutLastExpected: Int) {
        inputs!!.executedAt = now
        provider!!.clear()
        provider!!.addResults(inputs)
        playResults(BaseWidgetTest.Companion.TAG)
        Assert.assertEquals((entriesWithoutLastExpected + 1).toLong(), getFactory().widgetEntries.size.toLong())
        if (entriesWithoutLastExpected > 0) {
            val birthday = getFactory().widgetEntries[1] as CalendarEntry
            Assert.assertEquals(9, birthday.entryDate.dayOfMonth().get().toLong())
            Assert.assertEquals(0, birthday.entryDate.hourOfDay().get().toLong())
            Assert.assertEquals(0, birthday.entryDate.minuteOfHour().get().toLong())
            Assert.assertEquals(0, birthday.entryDate.millisOfDay().get().toLong())
            Assert.assertEquals(true, birthday.allDay)
        }
        val lastEntry = getFactory().widgetEntries[getFactory().widgetEntries.size - 1] as LastEntry
        Assert.assertEquals(
            "Last entry: $lastEntry",
            if (entriesWithoutLastExpected == 0) LastEntry.LastEntryType.EMPTY else LastEntry.LastEntryType.LAST,
            lastEntry.type
        )
    }
}
