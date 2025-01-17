package org.andstatus.todoagenda.task

import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.widget.TaskEntry
import org.andstatus.todoagenda.widget.WidgetEntry
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer

class TaskVisualizer(eventProvider: EventProvider) : WidgetEntryVisualizer<TaskEntry>(eventProvider) {
    private val taskProvider: AbstractTaskProvider
        private get() = super.eventProvider as AbstractTaskProvider

    override fun getRemoteViews(eventEntry: WidgetEntry<*>, position: Int): RemoteViews {
        val rv = super.getRemoteViews(eventEntry, position)
        val entry = eventEntry as TaskEntry
        setIcon(entry, rv)
        return rv
    }

    override fun newViewEntryIntent(eventEntry: WidgetEntry<*>): Intent {
        val entry = eventEntry as TaskEntry
        return taskProvider!!.newViewEventIntent(entry.event)
    }

    private fun setIcon(entry: TaskEntry, rv: RemoteViews) {
        if (settings.showEventIcon) {
            rv.setViewVisibility(R.id.event_entry_icon, View.VISIBLE)
            rv.setTextColor(R.id.event_entry_icon, entry.event.color)
        } else {
            rv.setViewVisibility(R.id.event_entry_icon, View.GONE)
        }
        rv.setViewVisibility(R.id.event_entry_color, View.GONE)
    }

    override fun queryEventEntries(): List<TaskEntry> {
        return toTaskEntryList(taskProvider!!.queryEvents())
    }

    private fun toTaskEntryList(events: List<TaskEvent>?): List<TaskEntry> {
        val entries: MutableList<TaskEntry> = ArrayList()
        for (event in events!!) {
            entries.add(TaskEntry.Companion.fromEvent(settings, event))
        }
        return entries
    }
}
