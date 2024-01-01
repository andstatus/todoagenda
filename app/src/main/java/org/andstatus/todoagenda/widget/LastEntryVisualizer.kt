package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.colors.TextColorPref;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import static org.andstatus.todoagenda.RemoteViewsFactory.ACTION_CONFIGURE;
import static org.andstatus.todoagenda.RemoteViewsFactory.getActionPendingIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.newOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

/** @author yvolk@yurivolkov.com */
public class LastEntryVisualizer extends WidgetEntryVisualizer<LastEntry> {
    private static final String TAG = LastEntryVisualizer.class.getSimpleName();

    public LastEntryVisualizer(Context context, int widgetId) {
        super(new EventProvider(EventProviderType.LAST_ENTRY, context, widgetId));
    }

    @Override
    @NonNull
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        LastEntry entry = (LastEntry) eventEntry;
        Log.d(TAG, "lastEntry: " + entry.type);
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), entry.type.layoutId);

        int viewId = R.id.event_entry;
        if (position < 0) {
            rv.setOnClickPendingIntent(R.id.event_entry, getActionPendingIntent(getSettings(), ACTION_CONFIGURE));
        }
        if (entry.type == LastEntry.LastEntryType.EMPTY && getSettings().noPastEvents()) {
            rv.setTextViewText(viewId, getContext().getText(R.string.no_upcoming_events));
        }
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColor(getSettings(), TextColorPref.forTitle(entry), rv, viewId, R.attr.eventEntryTitle);
        setBackgroundColor(rv, viewId, getSettings().colors().getEntryBackgroundColor(entry));
        return rv;
    }

    @Override
    public Intent newViewEntryIntent(WidgetEntry widgetEntry) {
        LastEntry entry = (LastEntry) widgetEntry;
        switch (entry.type) {
            case EMPTY:
            case NOT_LOADED:
                return newOpenCalendarAtDayIntent(new DateTime(getSettings().clock().getZone()));
            default:
                break;
        }
        return  MainActivity.intentToConfigure(getSettings().getContext(), getSettings().getWidgetId());
    }

    @Override
    public List<LastEntry> queryEventEntries() {
        return Collections.emptyList();
    }
}