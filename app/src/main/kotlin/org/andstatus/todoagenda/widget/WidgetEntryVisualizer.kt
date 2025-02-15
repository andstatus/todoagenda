package org.andstatus.todoagenda.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.EventProvider

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

    abstract fun queryEventEntries(): List<WidgetEntry>

    fun isFor(entry: WidgetEntry): Boolean = entry.source.source.providerType === eventProvider.type

    open fun newViewEntryIntent(entry: WidgetEntry): Intent? = null
}
