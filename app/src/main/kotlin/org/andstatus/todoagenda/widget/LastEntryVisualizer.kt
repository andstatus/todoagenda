package org.andstatus.todoagenda.widget

import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.LastEntryAppearance
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.RemoteViewsUtil

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
        Log.d(TAG, "lastEntry: ${entry.type}, position: $position")
        val rv = RemoteViews(context.packageName, entry.type.widgetLayout.shadowed(settings.textShadow))
        val viewId = R.id.event_entry
        if (entry.appearance == LastEntryAppearance.WITH_MESSAGE) {
            rv.setTextViewText(viewId, context.getText(entry.type.valueResId))
        } else {
            rv.setTextViewText(viewId, context.getText(R.string.one_space))
        }
        RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_title)
        RemoteViewsUtil.setTextColor(settings, TextColorPref.forTitle(entry), rv, viewId, R.attr.eventEntryTitle)
        RemoteViewsUtil.setBackgroundColor(rv, viewId, settings.colors().getEntryBackgroundColor(entry))
        return rv
    }

    companion object {
        private val TAG = LastEntryVisualizer::class.java.simpleName
    }
}
