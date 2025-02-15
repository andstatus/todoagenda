package org.andstatus.todoagenda.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import org.andstatus.todoagenda.MainActivity
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.RemoteViewsFactory
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.CalendarIntentUtil
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.LastEntry.LastEntryType
import org.joda.time.DateTime

/** @author yvolk@yurivolkov.com
 */
class LastEntryVisualizer(
    context: Context,
    widgetId: Int,
) : WidgetEntryVisualizer(EventProvider(EventProviderType.LAST_ENTRY, context, widgetId)) {
    override fun getRemoteViews(
        eventEntry: WidgetEntry,
        position: Int,
    ): RemoteViews {
        val entry = eventEntry as LastEntry
        Log.d(TAG, "lastEntry: " + entry.type)
        val rv = RemoteViews(context.packageName, entry.type.widgetLayout.shadowed(settings.textShadow))
        val viewId = R.id.event_entry
        if (position < 0) {
            rv.setOnClickPendingIntent(
                R.id.event_entry,
                RemoteViewsFactory.getActionPendingIntent(
                    settings,
                    RemoteViewsFactory.ACTION_CONFIGURE,
                ),
            )
        }
        if (entry.type == LastEntryType.EMPTY && settings.noPastEvents()) {
            rv.setTextViewText(viewId, context.getText(R.string.no_upcoming_events))
        }
        RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_title)
        RemoteViewsUtil.setTextColor(
            settings,
            TextColorPref.forTitle(entry),
            rv,
            viewId,
            R.attr.eventEntryTitle,
        )
        RemoteViewsUtil.setBackgroundColor(rv, viewId, settings.colors().getEntryBackgroundColor(entry))
        return rv
    }

    override fun newViewEntryIntent(widgetEntry: WidgetEntry): Intent {
        val entry = widgetEntry as LastEntry
        when (entry.type) {
            LastEntryType.EMPTY, LastEntryType.NOT_LOADED -> return CalendarIntentUtil.newOpenCalendarAtDayIntent(
                DateTime(
                    settings.timeZone,
                ),
            )

            else -> {}
        }
        return MainActivity.intentToConfigure(settings.context, settings.widgetId)
    }

    override fun queryEventEntries(): List<LastEntry> = emptyList()

    companion object {
        private val TAG = LastEntryVisualizer::class.java.simpleName
    }
}
