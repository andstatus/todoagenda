package org.andstatus.todoagenda.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.util.MyStringBuilder
import org.andstatus.todoagenda.util.RemoteViewsUtil

abstract class WidgetEntryVisualizer<T : WidgetEntry<T>>(protected val eventProvider: EventProvider) {
    open fun getRemoteViews(entry: WidgetEntry<*>, position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, settings.eventEntryLayout.widgetLayout.shadowed(settings.textShadow))
        setTitle(entry, rv)
        setDetails(entry, rv)
        setDate(entry, rv)
        setTime(entry, rv)
        setTextStrikethrough(entry, rv)
        setIndicators(entry, rv)
        if (settings.isCompactLayout) {
            RemoteViewsUtil.setPadding(
                settings,
                rv,
                R.id.event_entry,
                R.dimen.zero,
                R.dimen.zero,
                R.dimen.zero,
                R.dimen.zero
            )
        } else {
            RemoteViewsUtil.setPadding(
                settings,
                rv,
                R.id.event_entry,
                R.dimen.calender_padding,
                R.dimen.zero,
                R.dimen.calender_padding,
                R.dimen.entry_bottom_padding
            )
        }
        RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry, settings.colors().getEntryBackgroundColor(entry))
        return rv
    }

    protected open fun setIndicators(entry: WidgetEntry<*>?, rv: RemoteViews) {
        for (indicator in AlarmIndicatorScaled.entries) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE)
        }
        for (indicator in RecurringIndicatorScaled.entries) {
            rv.setViewVisibility(indicator.indicatorResId, View.GONE)
        }
    }

    protected val settings: InstanceSettings
        get() = eventProvider.settings
    val context: Context
        get() = eventProvider.context

    abstract fun queryEventEntries(): List<T>
    protected fun setTitle(entry: WidgetEntry<*>, rv: RemoteViews) {
        val viewId = R.id.event_entry_title
        rv.setTextViewText(viewId, getTitleString(entry))
        RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.event_entry_title)
        RemoteViewsUtil.setTextColor(
            settings,
            TextColorPref.forTitle(entry),
            rv,
            viewId,
            R.attr.eventEntryTitle
        )
        RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineTitle)
        if (settings.isMultilineTitle) {
            RemoteViewsUtil.setMaxLines(rv, viewId, settings.maxLinesTitle)
        }
    }

    protected fun getTitleString(event: WidgetEntry<*>): CharSequence {
        return if (settings.eventEntryLayout == EventEntryLayout.DEFAULT) {
            event.title
        } else {
            MyStringBuilder.of(event.title)
                .withSeparator(event.locationShown, EventEntryLayout.SPACE_PIPE_SPACE)
                .withSeparator(event.descriptionShown, EventEntryLayout.SPACE_PIPE_SPACE)
        }
    }

    protected fun setDetails(entry: WidgetEntry<*>, rv: RemoteViews) {
        if (settings.eventEntryLayout == EventEntryLayout.ONE_LINE) return
        val eventDetails: MyStringBuilder = MyStringBuilder.of(entry.formatEntryDate())
            .withSpace(entry.eventTimeString)
            .withSeparator(entry.locationShown, EventEntryLayout.SPACE_PIPE_SPACE)
            .withSeparator(entry.descriptionShown, EventEntryLayout.SPACE_PIPE_SPACE)
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
                R.attr.dayHeaderTitle
            )
            RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineDetails)
            if (settings.isMultilineDetails) {
                RemoteViewsUtil.setMaxLines(rv, viewId, settings.maxLinesDetails)
            }
        }
    }

    protected fun setTextStrikethrough(entry: WidgetEntry<*>, rv: RemoteViews) {
        val viewId = R.id.event_entry_title
        RemoteViewsUtil.setTextStrikethrough(rv, viewId, entry.status == EventStatus.CANCELED)
    }

    protected fun setDate(entry: WidgetEntry<*>, rv: RemoteViews) {
        if (settings.eventEntryLayout == EventEntryLayout.DEFAULT) return
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
                if (daysAsText) R.dimen.days_to_event_width else R.dimen.days_to_event_right_width
            )
            RemoteViewsUtil.setTextSize(settings, rv, viewToShow, R.dimen.event_entry_details)
            RemoteViewsUtil.setTextColor(
                settings,
                TextColorPref.forDetails(entry),
                rv,
                viewToShow,
                R.attr.dayHeaderTitle
            )
        }
    }

    protected fun setTime(entry: WidgetEntry<*>, rv: RemoteViews) {
        if (settings.eventEntryLayout == EventEntryLayout.DEFAULT) return
        val viewId = R.id.event_entry_time
        RemoteViewsUtil.setMultiline(rv, viewId, settings.isMultilineDetails)
        rv.setTextViewText(viewId, entry.eventTimeString.replace(CalendarEntry.SPACE_DASH_SPACE, "\n"))
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
            R.attr.dayHeaderTitle
        )
    }

    fun isFor(entry: WidgetEntry<*>): Boolean {
        return entry.source.source.providerType === eventProvider.type
    }

    open fun newViewEntryIntent(entry: WidgetEntry<*>): Intent? {
        return null
    }
}
