package org.andstatus.todoagenda.calendar

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.colors.Shading
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.AlarmIndicatorScaled
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.RecurringIndicatorScaled
import org.andstatus.todoagenda.widget.WidgetEntry
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer

class CalendarEventVisualizer(
    eventProvider: EventProvider,
) : WidgetEntryVisualizer(eventProvider) {
    private val calendarEventProvider: CalendarEventProvider
        get() = eventProvider as CalendarEventProvider

    override fun getRemoteViews(
        eventEntry: WidgetEntry,
        position: Int,
    ): RemoteViews {
        val rv = super.getRemoteViews(eventEntry, position)
        val entry = eventEntry as CalendarEntry
        setIcon(entry, rv)
        return rv
    }

    override fun newViewEntryIntent(entry: WidgetEntry): Intent {
        val calendarEntry = entry as CalendarEntry
        return calendarEventProvider.newViewEventIntent(calendarEntry.event)
    }

    override fun setIndicators(
        entry: WidgetEntry?,
        rv: RemoteViews,
    ) {
        val calendarEntry = entry as CalendarEntry
        setAlarmActive(calendarEntry, rv)
        setRecurring(calendarEntry, rv)
    }

    private fun setAlarmActive(
        entry: CalendarEntry,
        rv: RemoteViews,
    ) {
        val showIndicator = entry.isAlarmActive && settings.indicateAlerts
        for (indicator in AlarmIndicatorScaled.entries) {
            setIndicator(
                entry,
                rv,
                showIndicator && indicator == settings.textSizeScale.alarmIndicator,
                indicator.indicatorResId,
                R.attr.eventEntryAlarm,
            )
        }
    }

    private fun setRecurring(
        entry: CalendarEntry,
        rv: RemoteViews,
    ) {
        val showIndicator = entry.isRecurring && settings.indicateRecurring
        for (indicator in RecurringIndicatorScaled.entries) {
            setIndicator(
                entry,
                rv,
                showIndicator && indicator == settings.textSizeScale.recurringIndicator,
                indicator.indicatorResId,
                R.attr.eventEntryRecurring,
            )
        }
    }

    private fun setIndicator(
        entry: CalendarEntry,
        rv: RemoteViews,
        showIndication: Boolean,
        viewId: Int,
        imageAttrId: Int,
    ) {
        if (showIndication) {
            rv.setViewVisibility(viewId, View.VISIBLE)
            val pref: TextColorPref = TextColorPref.forTitle(entry)
            RemoteViewsUtil.setImageFromAttr(settings.colors().getThemeContext(pref), rv, viewId, imageAttrId)
            val shading = settings.colors().getShading(pref)
            var alpha = 255
            if (shading == Shading.DARK || shading == Shading.LIGHT) {
                alpha = 128
            }
            RemoteViewsUtil.setAlpha(rv, viewId, alpha)
        } else {
            rv.setViewVisibility(viewId, View.GONE)
        }
    }

    private fun setIcon(
        entry: CalendarEntry,
        rv: RemoteViews,
    ) {
        if (settings.showEventIcon) {
            rv.setViewVisibility(R.id.event_entry_color, View.VISIBLE)
            RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry_color, entry.color)
        } else {
            rv.setViewVisibility(R.id.event_entry_color, View.GONE)
        }
        rv.setViewVisibility(R.id.event_entry_icon, View.GONE)
    }

    override fun queryEventEntries(): List<CalendarEntry> = toCalendarEntryList(calendarEventProvider.queryEvents())

    private fun toCalendarEntryList(eventList: List<CalendarEvent>?): List<CalendarEntry> {
        val entryList: MutableList<CalendarEntry> = ArrayList()
        for (event in eventList!!) {
            val firstDayEntry = getFirstDayEntry(event)
            if (firstDayEntry.entryDay.isBefore(settings.clock.thisDay()) || settings.fillAllDayEvents) {
                val oneEventEntries = allEventEntries(firstDayEntry)
                if (settings.fillAllDayEvents || oneEventEntries.size < 2) {
                    entryList.addAll(oneEventEntries)
                } else {
                    // Decide, which one entry to add
                    val best: CalendarEntry =
                        oneEventEntries.drop(1).fold(oneEventEntries.first()) { acc, entry ->
                            if (Math.abs(settings.clock.getNumberOfMinutesTo(entry.entryClosestTime)) <
                                Math.abs(settings.clock.getNumberOfMinutesTo(acc.entryClosestTime))
                            ) {
                                entry
                            } else {
                                acc
                            }
                        }
                    entryList.add(best)
                }
            } else {
                entryList.add(firstDayEntry)
                if (settings.logEvents) {
                    Log.i("toCalendarEntryList", "Only one entry for event: $event\nentry: $firstDayEntry")
                }
            }
        }
        if (settings.logEvents) {
            entryList.forEachIndexed { index, entry ->
                Log.i("toCalendarEntryLAll", "${index + 1}. $entry")
            }
        }
        return entryList
    }

    private fun getFirstDayEntry(event: CalendarEvent): CalendarEntry {
        var firstDate = event.startDate
        val today = settings.clock.startOfToday()
        if (event.isActive && firstDate.isBefore(today)) {
            firstDate = today
        } else {
            val timeRangeStartDay = settings.clock.dayOf(calendarEventProvider.startOfTimeRange)
            if (firstDate.isBefore(timeRangeStartDay) && event.endDate.isAfter(timeRangeStartDay)) {
                firstDate = timeRangeStartDay
            }
        }
        return CalendarEntry.fromEvent(settings, event, firstDate)
    }

    private fun allEventEntries(firstDayEntry: CalendarEntry): List<CalendarEntry> {
        val entries = mutableListOf(firstDayEntry)
        var endDay =
            if (firstDayEntry.event.endDate.isAfter(calendarEventProvider.endOfTimeRange)) {
                calendarEventProvider.endOfTimeRange
            } else {
                firstDayEntry.event.endDate
            }.let {
                firstDayEntry.calcEntryDay(it.minusSeconds(1))
            }
        var nextDay = firstDayEntry.entryDay.plusDays(1)
        var i = 1
        if (settings.logEvents) {
            Log.i("addEntriesToFillAllEventDays", "$i. endDay: $endDay, thisDay: $nextDay, $firstDayEntry")
        }
        while (!nextDay.isAfter(endDay)) {
            val nextEntry: CalendarEntry =
                CalendarEntry.fromEvent(
                    settings,
                    firstDayEntry.event,
                    entryDate = settings.clock.withTimeAtStartHourOfDay(nextDay),
                )
            if (settings.logEvents) {
                i++
                Log.i("addEntriesToFillAllEventDays", "$i. thisDay: $nextDay, $nextEntry")
            }
            entries.add(nextEntry)
            nextDay = nextDay.plusDays(1)
        }
        return entries
    }
}
