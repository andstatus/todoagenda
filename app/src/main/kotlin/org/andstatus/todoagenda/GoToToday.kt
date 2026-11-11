package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import org.andstatus.todoagenda.layout.WidgetRootLayout
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings

private val TAG = "GoToToday"

fun clearBootCompleted(context: Context) {
    Log.d(TAG, "clearBootCompleted")
    ApplicationPreferences.clearBootCompleted(context)
}

fun delayedGotoToday(settings: InstanceSettings) {
    val context = settings.context
    val widgetId = settings.widgetId
    if (!settings.isScrollableLayout ||
        ApplicationPreferences.hasBootCompleted(context, widgetId)
    ) {
        return
    }
    Log.d(TAG, "Posting gotoToday for widgetId:$widgetId")
    // Execute scrolling with delay, waiting for the list to complete layout
    Handler(Looper.getMainLooper()).postDelayed(
        {
            ApplicationPreferences.addBootCompleted(context, widgetId)
            gotoToday(context, widgetId)
        },
        500,
    )
}

fun gotoToday(
    context: Context,
    widgetId: Int,
) {
    val settings = AllSettings.instanceFromId(context, widgetId)
    if (!settings.isScrollableLayout) return

    Log.d(TAG, "gotoToday for widgetId:$widgetId")
    val factory: RemoteViewsFactory? = RemoteViewsFactory.factories[widgetId]
    val position1 = factory?.tomorrowsPosition ?: 0
    val position2 = factory?.todaysPosition ?: 0
    gotoPosition(context, widgetId, position1)
    if (position1 >= 0 && position2 >= 0 && position1 != position2) {
        EnvironmentChangedReceiver.sleep(1000)
    }
    gotoPosition(context, widgetId, position2)
}

private fun gotoPosition(
    context: Context,
    widgetId: Int,
    position: Int,
) {
    if (widgetId == 0 || position < 0) return
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val settings = AllSettings.instanceFromId(context, widgetId)
    val rv =
        RemoteViews(
            context.packageName,
            WidgetRootLayout.forWidget(true, settings.widgetHeaderLayout).layoutId,
        )
    Log.d(TAG, "gotoPosition, Scrolling widget $widgetId to position $position")
    rv.setScrollPosition(R.id.event_list, position)
    appWidgetManager.partiallyUpdateAppWidget(widgetId, rv)
}
