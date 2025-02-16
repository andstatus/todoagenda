package org.andstatus.todoagenda.task

import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.widget.AlarmIndicatorScaled
import org.andstatus.todoagenda.widget.EventEntryVisualizer
import org.andstatus.todoagenda.widget.RecurringIndicatorScaled
import org.andstatus.todoagenda.widget.TaskEntry
import org.andstatus.todoagenda.widget.TaskEntry.Companion
import org.andstatus.todoagenda.widget.WidgetEntry

class TaskVisualizer(
    eventProvider: EventProvider,
) : EventEntryVisualizer(eventProvider) {
    private val taskProvider: AbstractTaskProvider
        get() = super.eventProvider as AbstractTaskProvider

    override fun newViewEntryIntent(entry: WidgetEntry): Intent =
        entry.event?.let { taskProvider.newViewEventIntent(it) } ?: super.newViewEntryIntent(entry)

    override fun setIndicators(
        entry: WidgetEntry?,
        rv: RemoteViews,
    ) {
        for (indicator in AlarmIndicatorScaled.entries) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE)
        }
        for (indicator in RecurringIndicatorScaled.entries) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE)
        }
    }

    override fun setIcon(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        if (settings.showEventIcon) {
            rv.setViewVisibility(R.id.event_entry_icon, View.VISIBLE)
            rv.setTextColor(R.id.event_entry_icon, (entry as TaskEntry).event.color)
        } else {
            rv.setViewVisibility(R.id.event_entry_icon, View.GONE)
        }
        rv.setViewVisibility(R.id.event_entry_color, View.GONE)
    }

    override fun queryEventEntries(): List<WidgetEntry> = taskProvider.queryEvents().map { Companion.fromEvent(settings, it) }
}
