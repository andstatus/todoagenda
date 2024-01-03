package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.util.Log
import org.andstatus.todoagenda.prefs.AllSettings

class RemoteViewsService : android.widget.RemoteViewsService() {
    override fun onCreate() {
        Log.d(TAG, "onCreate")
        AllSettings.ensureLoadedFromFiles(this)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        Log.d(TAG, "$widgetId onGetViewFactory, intent:$intent")
        val factory = RemoteViewsFactory(this, widgetId, true)
        org.andstatus.todoagenda.RemoteViewsFactory.Companion.factories.put(widgetId, factory)
        return factory
    }

    companion object {
        private val TAG = RemoteViewsService::class.java.simpleName
    }
}
