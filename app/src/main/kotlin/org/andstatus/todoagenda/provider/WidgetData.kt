package org.andstatus.todoagenda.provider

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author yvolk@yurivolkov.com
 */
class WidgetData private constructor(private val jsonData: JSONObject) {
    val results: List<QueryResult> = CopyOnWriteArrayList()

    fun toJsonString(): String {
        return try {
            toJson().toString(2)
        } catch (e: JSONException) {
            TAG + " Error while formatting data " + e
        }
    }

    fun toJson(): JSONObject {
        return jsonData
    }

    override fun toString(): String = "$TAG:$jsonData"

    fun getSettingsForWidget(
        context: Context,
        storedSettings: InstanceSettings?,
        targetWidgetId: Int
    ): InstanceSettings {
        val jsonSettings = jsonData.optJSONObject(KEY_SETTINGS)
            ?: return InstanceSettings.EMPTY
        val originalSettings: InstanceSettings =
            InstanceSettings.fromJson(context, storedSettings, jsonSettings)
        val results: QueryResultsStorage = QueryResultsStorage.fromJson(targetWidgetId, jsonData)
        return originalSettings.copy(
            widgetId = targetWidgetId,
            proposedInstanceName = originalSettings.widgetInstanceName,
            resultsStorage = results.takeIf { results.results.isNotEmpty() }
        )
    }

    companion object {
        val EMPTY = WidgetData(JSONObject())
        private val TAG = WidgetData::class.java.simpleName
        private const val KEY_SETTINGS = "settings"
        private const val KEY_APP_VERSION_NAME = "versionName"
        private const val KEY_APP_VERSION_CODE = "versionCode"
        private const val KEY_APP_INFO = "applicationInfo"
        private const val KEY_DEVICE_INFO = "deviceInfo"
        private const val KEY_ANDROID_VERSION_CODE = "versionCode"
        private const val KEY_ANDROID_VERSION_RELEASE = "versionRelease"
        private const val KEY_ANDROID_VERSION_CODENAME = "versionCodename"
        private const val KEY_ANDROID_MANUFACTURE = "buildManufacturer"
        private const val KEY_ANDROID_BRAND = "buildBrand"
        private const val KEY_ANDROID_MODEL = "buildModel"
        fun fromJson(jso: JSONObject?): WidgetData {
            return jso?.let { WidgetData(it) } ?: EMPTY
        }

        fun fromSettings(context: Context, settings: InstanceSettings?): WidgetData {
            val json = JSONObject()
            try {
                json.put(KEY_DEVICE_INFO, deviceInfo)
                json.put(KEY_APP_INFO, getAppInfo(context))
                if (settings != null) {
                    json.put(KEY_SETTINGS, settings.toJson())
                }
            } catch (e: JSONException) {
                Log.w(TAG, "fromSettings failed; $settings", e)
            }
            return WidgetData(json)
        }

        @Throws(JSONException::class)
        private fun getAppInfo(context: Context): JSONObject {
            val json = JSONObject()
            try {
                val pm = context.packageManager
                val applicationContext = context.applicationContext
                val pi = pm.getPackageInfo(
                    (applicationContext ?: context).packageName, 0
                )
                json.put(KEY_APP_VERSION_NAME, pi.versionName)
                json.put(KEY_APP_VERSION_CODE, pi.versionCode)
            } catch (e: PackageManager.NameNotFoundException) {
                json.put(KEY_APP_VERSION_NAME, "Unable to obtain package information $e")
                json.put(KEY_APP_VERSION_CODE, -1)
            }
            return json
        }

        private val deviceInfo: JSONObject
            get() {
                val json = JSONObject()
                try {
                    json.put(KEY_ANDROID_VERSION_CODE, Build.VERSION.SDK_INT)
                    json.put(KEY_ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE)
                    json.put(KEY_ANDROID_VERSION_CODENAME, Build.VERSION.CODENAME)
                    json.put(KEY_ANDROID_MANUFACTURE, Build.MANUFACTURER)
                    json.put(KEY_ANDROID_BRAND, Build.BRAND)
                    json.put(KEY_ANDROID_MODEL, Build.MODEL)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                return json
            }
    }
}
