package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.andstatus.todoagenda.prefs.AllSettings
import java.util.AbstractList

class AppWidgetProvider : android.appwidget.AppWidgetProvider() {
    init {
        Log.d(TAG, "init")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        newOptions: Bundle
    ) {
        Log.d(TAG, "$widgetId onOptionsChanged")
        super.onAppWidgetOptionsChanged(context, appWidgetManager, widgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive, intent:$intent")
        AllSettings.ensureLoadedFromFiles(context)
        val action = intent.action
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE == action) {
            val extras = intent.extras
            var widgetIds = extras?.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (widgetIds == null || widgetIds.size == 0) {
                widgetIds = getWidgetIds(context)
                Log.d(
                    TAG, "onUpdate, input: no widgetIds, discovered here:" +
                        asList(widgetIds) + ", context:" + context
                )
            }
            if (widgetIds.isNotEmpty()) {
                onUpdate(context, AppWidgetManager.getInstance(context), widgetIds)
            }
        } else {
            super.onReceive(context, intent)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled, context:$context")
        super.onEnabled(context)
    }

    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        Log.d(
            TAG, "onRestored, oldWidgetIds:" + asList(oldWidgetIds) +
                ", newWidgetIds:" + asList(newWidgetIds)
        )
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted, widgetIds:" + asList(appWidgetIds))
        super.onDeleted(context, appWidgetIds)
        for (widgetId in appWidgetIds) {
            AllSettings.delete(context, widgetId)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate, widgetIds:" + asList(appWidgetIds) + ", context:" + context)
        for (widgetId in appWidgetIds) {
            RemoteViewsFactory.updateWidget(context, widgetId)
            InstanceState.updated(widgetId)
            appWidgetManager.notifyAppWidgetViewDataChanged(intArrayOf(widgetId), R.id.event_list)
        }
    }

    companion object {
        private val TAG = AppWidgetProvider::class.java.simpleName
        fun asList(ints: IntArray): List<Int> {
            return object : AbstractList<Int>() {
                override fun get(index: Int): Int = ints[index]

                override val size: Int get() = ints.size
            }
        }

        fun getWidgetIds(context: Context?): IntArray {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return if (appWidgetManager == null) intArrayOf() else appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context!!, AppWidgetProvider::class.java
                )
            )
        }
    }
}
