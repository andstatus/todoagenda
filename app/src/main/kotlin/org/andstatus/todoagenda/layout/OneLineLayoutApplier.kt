package org.andstatus.todoagenda.layout

import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.util.MyStringBuilder
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.EventEntryVisualizer
import org.andstatus.todoagenda.widget.WidgetEntry

/**
 * @author yvolk@yurivolkov.com
 */
class OneLineLayoutApplier(
    visualizer: EventEntryVisualizer,
) : EventEntryLayoutApplier(visualizer) {
    override fun getTitleString(entry: WidgetEntry): CharSequence =
        MyStringBuilder
            .of(entry.title)
            .withSpace(dayXY(entry))
            .withSeparator(entry.locationShown, SPACE_PIPE_SPACE)
            .withSeparator(entry.descriptionShown, SPACE_PIPE_SPACE)

    override fun setDetails(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
    }

    override fun setDate(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        if (settings.entryDateFormat.type == DateFormatType.HIDDEN) {
            rv.setViewVisibility(R.id.event_entry_days, View.GONE)
            rv.setViewVisibility(R.id.event_entry_days_right, View.GONE)
        } else {
            val days = settings.clock.getNumberOfDaysTo(entry.entryDate)
            val daysAsText = settings.entryDateFormat.type != DateFormatType.NUMBER_OF_DAYS || days > -2 && days < 2
            val viewToShow = if (daysAsText) R.id.event_entry_days else R.id.event_entry_days_right
            val viewToHide = if (daysAsText) R.id.event_entry_days_right else R.id.event_entry_days
            rv.setViewVisibility(viewToHide, View.GONE)
            rv.setViewVisibility(viewToShow, View.VISIBLE)
            rv.setTextViewText(viewToShow, entry.formatEntryDate())
            RemoteViewsUtil.setViewWidth(
                settings,
                rv,
                viewToShow,
                if (daysAsText) R.dimen.days_to_event_width else R.dimen.days_to_event_right_width,
            )
            RemoteViewsUtil.setTextSize(settings, rv, viewToShow, R.dimen.event_entry_details)
            RemoteViewsUtil.setTextColor(
                settings,
                TextColorPref.forDetails(entry),
                rv,
                viewToShow,
                R.attr.dayHeaderTitle,
            )
        }
    }

    override fun setTime(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        val viewId = R.id.event_entry_time
        RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineDetails)
        rv.setTextViewText(viewId, entry.eventTimeString.replace(CalendarEntry.Companion.SPACE_DASH_SPACE, "\n"))
        if (settings.isMultilineDetails) {
            RemoteViewsUtil.setViewWidth(settings, rv, viewId, R.dimen.event_time_width)
            RemoteViewsUtil.setMaxLines(rv, viewId, settings.maxLinesDetails)
        } else {
            RemoteViewsUtil.setViewMinWidth(settings, rv, viewId, R.dimen.event_time_width)
        }
        RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_details)
        RemoteViewsUtil.setTextColor(
            settings,
            TextColorPref.forDetails(entry),
            rv,
            viewId,
            R.attr.dayHeaderTitle,
        )
    }
}
