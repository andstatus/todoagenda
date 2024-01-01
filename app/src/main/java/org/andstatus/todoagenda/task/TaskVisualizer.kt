package org.andstatus.todoagenda.task;

import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.List;

public class TaskVisualizer extends WidgetEntryVisualizer<TaskEntry> {

    public TaskVisualizer(EventProvider eventProvider) {
        super(eventProvider);
    }

    private AbstractTaskProvider getTaskProvider() {
        return (AbstractTaskProvider) super.eventProvider;
    }

    @Override
    @NonNull
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        RemoteViews rv = super.getRemoteViews(eventEntry, position);

        TaskEntry entry = (TaskEntry) eventEntry;
        setIcon(entry, rv);
        return rv;
    }

    @Override
    public Intent newViewEntryIntent(WidgetEntry eventEntry) {
        TaskEntry entry = (TaskEntry) eventEntry;
        return getTaskProvider().newViewEventIntent(entry.getEvent());
    }

    private void setIcon(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getShowEventIcon()) {
            rv.setViewVisibility(R.id.event_entry_icon, View.VISIBLE);
            rv.setTextColor(R.id.event_entry_icon, entry.getEvent().getColor());
        } else {
            rv.setViewVisibility(R.id.event_entry_icon, View.GONE);
        }
        rv.setViewVisibility(R.id.event_entry_color, View.GONE);
    }

    @Override
    public List<TaskEntry> queryEventEntries() {
        return toTaskEntryList(getTaskProvider().queryEvents());
    }

    private List<TaskEntry> toTaskEntryList(List<TaskEvent> events) {
        List<TaskEntry> entries = new ArrayList<>();
        for (TaskEvent event : events) {
            entries.add(TaskEntry.fromEvent(getSettings(), event));
        }
        return entries;
    }

}
