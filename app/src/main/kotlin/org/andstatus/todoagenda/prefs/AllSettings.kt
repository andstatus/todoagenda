package org.andstatus.todoagenda.prefs

import android.app.Activity
import android.content.Context
import android.util.Log
import org.andstatus.todoagenda.AppWidgetProvider
import org.andstatus.todoagenda.EnvironmentChangedReceiver
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.MyLocale.setLocale
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.provider.WidgetData
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
object AllSettings {
    private val TAG = AllSettings::class.java.simpleName

    private val instancesLoaded = AtomicBoolean()
    private val instances: MutableMap<Int, InstanceSettings> = ConcurrentHashMap()
    fun instanceFromId(context: Context, widgetId: Int): InstanceSettings {
        ensureLoadedFromFiles(context)
        synchronized(instances) {
            val settings = instances[widgetId]
            return settings ?: newInstance(context, widgetId)
        }
    }

    private fun newInstance(context: Context, widgetId: Int): InstanceSettings {
        synchronized(instances) {
            var settings = instances[widgetId]
            if (settings == null) {
                settings = if (widgetId != 0 && ApplicationPreferences.getWidgetId(context) == widgetId) {
                    InstanceSettings.fromApplicationPreferences(context, widgetId, null)
                } else {
                    InstanceSettings(contextIn = context, widgetId = widgetId)
                }
                if (save(TAG, "newInstance", settings)) {
                    EventProviderType.initialize(context, true)
                    EnvironmentChangedReceiver.registerReceivers(instances)
                    EnvironmentChangedReceiver.updateWidget(context, widgetId)
                }
            }
            return settings
        }
    }

    fun reInitialize(context: Context) = ensureLoadedFromFiles(context, true)

    fun ensureLoadedFromFiles(context: Context, reInitialize: Boolean = false) {
        if (instancesLoaded.get() && !reInitialize) return
        synchronized(instances) {
            if (reInitialize) setLocale(context)
            if (instancesLoaded.get() && !reInitialize) return
            instances.clear()
            EventProviderType.initialize(context, reInitialize)
            for (widgetId in AppWidgetProvider.getWidgetIds(context)) {
                var settings: InstanceSettings
                try {
                    val json = SettingsStorage.loadJsonFromFile(context, getStorageKey(widgetId))
                    settings = InstanceSettings.fromJson(context, instances[widgetId], json)
                    if (settings.widgetId == 0) {
                        newInstance(context, widgetId)
                    } else {
                        settings.logMe(TAG, "ensureLoadedFromFiles put", widgetId)
                        instances[widgetId] = settings
                    }
                } catch (e: Exception) { // Starting from API21 android.system.ErrnoException may be thrown
                    Log.e("loadInstances", "widgetId:$widgetId", e)
                    newInstance(context, widgetId)
                }
            }
            instancesLoaded.set(true)
            EnvironmentChangedReceiver.registerReceivers(instances)
        }
    }

    fun addOrReplace(tag: String, context: Context, settings: InstanceSettings) {
        save(tag, "addNew", settings)
    }

    /** @return true if success */
    private fun save(tag: String, method: String, settings: InstanceSettings): Boolean {
        if (settings.isEmpty) {
            settings.logMe(tag, "Skipped saving empty from $method", settings.widgetId)
        } else if (settings.save(tag, method)) {
            instances[settings.widgetId] = settings
            return true
        }
        return false
    }

    fun saveFromApplicationPreferences(context: Context, widgetId: Int) {
        if (widgetId == 0) return
        val settingsStored = instanceFromId(context, widgetId)
        val settings: InstanceSettings =
            InstanceSettings.fromApplicationPreferences(context, widgetId, settingsStored)
        if (settings.widgetId == widgetId && settings != settingsStored) {
            save(TAG, "ApplicationPreferences", settings)
        }
        EnvironmentChangedReceiver.registerReceivers(instances)
    }

    fun getStorageKey(widgetId: Int): String {
        return "instanceSettings$widgetId"
    }

    fun delete(context: Context, widgetId: Int) {
        ensureLoadedFromFiles(context)
        synchronized(instances) {
            instances.remove(widgetId)
            SettingsStorage.delete(context, getStorageKey(widgetId))
            if (ApplicationPreferences.getWidgetId(context) == widgetId) {
                ApplicationPreferences.setWidgetId(context, 0)
            }
        }
    }

    fun uniqueInstanceName(context: Context?, widgetId: Int, proposedInstanceName: String?): String {
        if (proposedInstanceName != null && proposedInstanceName.trim { it <= ' ' }.length > 0 &&
            !existsInstanceName(widgetId, proposedInstanceName)
        ) {
            return proposedInstanceName
        }
        val nameByWidgetId = defaultInstanceName(context, widgetId)
        if (!existsInstanceName(widgetId, nameByWidgetId)) {
            return nameByWidgetId
        }
        var index = 1
        var name: String
        do {
            name = defaultInstanceName(context, index)
            index = index + 1
        } while (existsInstanceName(widgetId, name))
        return name
    }

    private fun defaultInstanceName(context: Context?, index: Int): String {
        return context!!.getText(R.string.app_name).toString() + " " + index
    }

    private fun existsInstanceName(widgetId: Int, name: String): Boolean {
        for (settings in instances.values) {
            if (settings.widgetId != widgetId && settings.widgetInstanceName == name) {
                return true
            }
        }
        return false
    }

    fun getInstances(context: Context): Map<Int, InstanceSettings> {
        ensureLoadedFromFiles(context)
        return instances
    }

    val loadedInstances: MutableMap<Int, InstanceSettings>
        get() = instances

    fun forget() {
        synchronized(instances) {
            instances.clear()
            instancesLoaded.set(false)
        }
    }

    fun restoreWidgetSettings(activity: Activity, json: JSONObject?, targetWidgetId: Int): InstanceSettings {
        val settings: InstanceSettings = WidgetData.fromJson(json)
            .getSettingsForWidget(activity, instances[targetWidgetId], targetWidgetId)
            .copy(snapshotModeIn = SnapshotMode.SNAPSHOT_TIME)
        save(TAG, "restoreWidgetSettings", settings)
        return settings
    }
}
