package org.andstatus.todoagenda.layout

import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.util.MyStringBuilder
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.EventEntryVisualizer
import org.andstatus.todoagenda.widget.WidgetEntry

/**
 * @author yvolk@yurivolkov.com
 */
class TimeBelowTitleLayoutApplier(
    visualizer: EventEntryVisualizer,
) : EventEntryLayoutApplier(visualizer) {
    override fun getTitleString(event: WidgetEntry): CharSequence = event.title

    override fun setDetails(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        val eventDetails: MyStringBuilder =
            MyStringBuilder
                .of(entry.formatEntryDate())
                .withSpace(dayXY(entry))
                .withSpace(entry.eventTimeString)
                .withSeparator(entry.locationShown, SPACE_PIPE_SPACE)
                .withSeparator(entry.descriptionShown, SPACE_PIPE_SPACE)
        val viewId = R.id.event_entry_details
        if (eventDetails.isEmpty()) {
            rv.setViewVisibility(viewId, View.GONE)
        } else {
            rv.setViewVisibility(viewId, View.VISIBLE)
            rv.setTextViewText(viewId, eventDetails)
            RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_details)
            RemoteViewsUtil.setTextColor(
                settings,
                TextColorPref.forDetails(entry),
                rv,
                viewId,
                R.attr.dayHeaderTitle,
            )
            RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineDetails)
            if (settings.isMultilineDetails) {
                RemoteViewsUtil.setMaxLines(rv, viewId, settings.maxLinesDetails)
            }
        }
    }

    override fun setDate(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
    }

    override fun setTime(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
    }
}
