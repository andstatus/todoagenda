package org.andstatus.todoagenda.widget

import android.widget.RemoteViews
import org.andstatus.todoagenda.layout.EventEntryLayoutApplier
import org.andstatus.todoagenda.provider.EventProvider

abstract class EventEntryVisualizer(
    eventProvider: EventProvider,
) : WidgetEntryVisualizer(eventProvider) {
    val layout: EventEntryLayoutApplier = settings.eventEntryLayout.applier(this)

    override fun getRemoteViews(
        entry: WidgetEntry,
        position: Int,
    ): RemoteViews =
        RemoteViews(context.packageName, settings.eventEntryLayout.widgetLayout.shadowed(settings.textShadow)).also {
            layout.apply(entry, it)
        }

    abstract fun setIcon(
        entry: WidgetEntry,
        rv: RemoteViews,
    )

    abstract fun setIndicators(
        entry: WidgetEntry?,
        rv: RemoteViews,
    )
}
