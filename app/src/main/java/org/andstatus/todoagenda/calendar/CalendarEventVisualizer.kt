package org.andstatus.todoagenda.calendar

import android.content.Intent
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

class CalendarEventVisualizer(eventProvider: EventProvider) : WidgetEntryVisualizer<CalendarEntry>(eventProvider) {
    private val calendarEventProvider: CalendarEventProvider
        get() = eventProvider as CalendarEventProvider

    override fun getRemoteViews(eventEntry: WidgetEntry<*>, position: Int): RemoteViews {
        val rv = super.getRemoteViews(eventEntry, position)
        val entry = eventEntry as CalendarEntry
        setIcon(entry, rv)
        return rv
    }

    override fun newViewEntryIntent(entry: WidgetEntry<*>): Intent {
        val calendarEntry = entry as CalendarEntry
        return calendarEventProvider.newViewEventIntent(calendarEntry.event)
    }

    override fun setIndicators(entry: WidgetEntry<*>?, rv: RemoteViews) {
        val calendarEntry = entry as CalendarEntry
        setAlarmActive(calendarEntry, rv)
        setRecurring(calendarEntry, rv)
    }

    private fun setAlarmActive(entry: CalendarEntry, rv: RemoteViews) {
        val showIndicator = entry.isAlarmActive && settings.indicateAlerts
        for (indicator in AlarmIndicatorScaled.entries) {
            setIndicator(
                entry, rv,
                showIndicator && indicator == settings.textSizeScale.alarmIndicator,
                indicator.indicatorResId, R.attr.eventEntryAlarm
            )
        }
    }

    private fun setRecurring(entry: CalendarEntry, rv: RemoteViews) {
        val showIndicator = entry.isRecurring && settings.indicateRecurring
        for (indicator in RecurringIndicatorScaled.entries) {
            setIndicator(
                entry, rv,
                showIndicator && indicator == settings.textSizeScale.recurringIndicator,
                indicator.indicatorResId, R.attr.eventEntryRecurring
            )
        }
    }

    private fun setIndicator(
        entry: CalendarEntry,
        rv: RemoteViews,
        showIndication: Boolean,
        viewId: Int,
        imageAttrId: Int
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

    private fun setIcon(entry: CalendarEntry, rv: RemoteViews) {
        if (settings.showEventIcon) {
            rv.setViewVisibility(R.id.event_entry_color, View.VISIBLE)
            RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry_color, entry.color)
        } else {
            rv.setViewVisibility(R.id.event_entry_color, View.GONE)
        }
        rv.setViewVisibility(R.id.event_entry_icon, View.GONE)
    }

    override fun queryEventEntries(): List<CalendarEntry> {
        return toCalendarEntryList(calendarEventProvider.queryEvents())
    }

    private fun toCalendarEntryList(eventList: List<CalendarEvent>?): List<CalendarEntry> {
        val fillAllDayEvents = settings.fillAllDayEvents
        val entryList: MutableList<CalendarEntry> = ArrayList()
        for (event in eventList!!) {
            val dayOneEntry = getDayOneEntry(event)
            entryList.add(dayOneEntry)
            if (fillAllDayEvents) {
                addEntriesToFillAllDayEvents(entryList, dayOneEntry)
            }
        }
        return entryList
    }

    private fun getDayOneEntry(event: CalendarEvent): CalendarEntry {
        var firstDate = event.startDate
        val dayOfStartOfTimeRange = calendarEventProvider.startOfTimeRange
            .withTimeAtStartOfDay()
        if (!event.hasDefaultCalendarColor() // ??? TODO: fix logic
            && firstDate.isBefore(calendarEventProvider.startOfTimeRange)
            && event.endDate.isAfter(calendarEventProvider.startOfTimeRange)
        ) {
            if (event.isAllDay || firstDate.isBefore(dayOfStartOfTimeRange)) {
                firstDate = dayOfStartOfTimeRange
            }
        }
        val today = settings.clock().now(event.startDate.zone).withTimeAtStartOfDay()
        if (event.isActive && firstDate.isBefore(today)) {
            firstDate = today
        }
        return CalendarEntry.fromEvent(settings, event, firstDate)
    }

    private fun addEntriesToFillAllDayEvents(entryList: MutableList<CalendarEntry>, dayOneEntry: CalendarEntry) {
        var endDate = dayOneEntry.event.endDate
        if (endDate.isAfter(calendarEventProvider.endOfTimeRange)) {
            endDate = calendarEventProvider.endOfTimeRange
        }
        var thisDay = dayOneEntry.entryDay.plusDays(1).withTimeAtStartOfDay()
        while (thisDay.isBefore(endDate)) {
            val nextEntry: CalendarEntry = CalendarEntry.fromEvent(settings, dayOneEntry.event, thisDay)
            entryList.add(nextEntry)
            thisDay = thisDay.plusDays(1)
        }
    }
}
