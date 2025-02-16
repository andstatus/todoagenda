package org.andstatus.todoagenda.layout

import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.CalendarEntry
import org.andstatus.todoagenda.widget.EventEntryVisualizer
import org.andstatus.todoagenda.widget.EventStatus
import org.andstatus.todoagenda.widget.WidgetEntry

/**
 * @author yvolk@yurivolkov.com
 */
abstract class EventEntryLayoutApplier(
    val visualizer: EventEntryVisualizer,
) {
    open fun apply(
        entry: WidgetEntry,
        rv: RemoteViews,
    ): RemoteViews {
        visualizer.setIcon(entry, rv)
        setTitle(entry, rv)
        setDetails(entry, rv)
        setDate(entry, rv)
        setTime(entry, rv)
        setTextStrikethrough(entry, rv)
        visualizer.setIndicators(entry, rv)
        RemoteViewsUtil.setCompact(settings, rv)
        setBackground(rv, entry)
        return rv
    }

    open fun setTitle(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        val viewId = R.id.event_entry_title
        rv.setTextViewText(viewId, getTitleString(entry))
        RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_title)
        RemoteViewsUtil.setTextColor(
            settings,
            TextColorPref.forTitle(entry),
            rv,
            viewId,
            R.attr.eventEntryTitle,
        )
        RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineTitle)
        if (settings.isMultilineTitle) {
            RemoteViewsUtil.setMaxLines(rv, viewId, settings.maxLinesTitle)
        }
    }

    abstract fun getTitleString(event: WidgetEntry): CharSequence

    abstract fun setDetails(
        entry: WidgetEntry,
        rv: RemoteViews,
    )

    abstract fun setDate(
        entry: WidgetEntry,
        rv: RemoteViews,
    )

    abstract fun setTime(
        entry: WidgetEntry,
        rv: RemoteViews,
    )

    open fun setTextStrikethrough(
        entry: WidgetEntry,
        rv: RemoteViews,
    ) {
        val viewId = R.id.event_entry_title
        RemoteViewsUtil.setTextStrikethrough(rv, viewId, entry.status == EventStatus.CANCELED)
    }

    open fun setBackground(
        rv: RemoteViews,
        entry: WidgetEntry,
    ) {
        RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry, settings.colors().getEntryBackgroundColor(entry))
    }

    fun dayXY(entry: WidgetEntry): String? =
        if (settings.showDayXY && entry is CalendarEntry && entry.isPartOfMultiDayEvent) {
            val day = entry.context.resources.getString(R.string.day)
            val dayOfEvent = entry.event.dayOfEvent(entry.entryDay)
            val daysOfEvent = entry.event.daysOfEvent
            "($day $dayOfEvent/$daysOfEvent)"
        } else {
            null
        }

    val settings: InstanceSettings
        get() = visualizer.settings

    companion object {
        const val SPACE_PIPE_SPACE = "  |  "
    }
}
