package org.andstatus.todoagenda.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.util.CalendarIntentUtil

abstract class WidgetEntryVisualizer(
    protected val eventProvider: EventProvider,
) {
    abstract fun getRemoteViews(
        entry: WidgetEntry,
        position: Int,
    ): RemoteViews

    val settings: InstanceSettings
        get() = eventProvider.settings
    val context: Context
        get() = eventProvider.context

    open fun queryEventEntries(): List<WidgetEntry> = emptyList()

    fun isFor(entry: WidgetEntry): Boolean = entry.source.source.providerType === eventProvider.type

    open fun newViewEntryIntent(entry: WidgetEntry): Intent = CalendarIntentUtil.newOpenCalendarAtDayIntent(entry.entryDate)
}
