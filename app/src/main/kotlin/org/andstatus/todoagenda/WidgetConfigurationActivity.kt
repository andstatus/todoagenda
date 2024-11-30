package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.RootFragment
import org.andstatus.todoagenda.prefs.colors.ColorsPreferencesFragment
import org.andstatus.todoagenda.provider.WidgetData
import org.andstatus.todoagenda.util.PermissionsUtil
import org.json.JSONException
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.StandardCharsets

class WidgetConfigurationActivity : MyActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private var widgetId = 0
    private var saveOnPause = true
    override fun onPause() {
        super.onPause()
        if (saveOnPause) {
            ApplicationPreferences.save(this, widgetId)
            EnvironmentChangedReceiver.updateWidget(this, widgetId)
        }
    }

    override fun onResume() {
        super.onResume()
        restartIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!openThisActivity(intent)) return
        setContentView(R.layout.activity_settings)
        super.onCreate(savedInstanceState)
        title = ApplicationPreferences.getWidgetInstanceName(this)
        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            var fragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
            if (fragment == null) {
                fragment = RootFragment()
            }
            var ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.settings_container, fragment, FRAGMENT_TAG)
            ft.commit()
            val gotoSection = intent.getStringExtra(EXTRA_GOTO_PREFERENCES_SECTION)
            if (EXTRA_GOTO_SECTION_COLORS == gotoSection) {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.settings_container, ColorsPreferencesFragment(), FRAGMENT_TAG)
                ft.commit()
                ft.addToBackStack(null)
            }
        }
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        title = pref.title.toString() + " - " + ApplicationPreferences.getWidgetInstanceName(this)
        return false
    }

    private fun openThisActivity(newIntent: Intent): Boolean {
        var newWidgetId = newIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        if (newWidgetId == 0) {
            newWidgetId = ApplicationPreferences.getWidgetId(this)
        }
        var restartIntent: Intent? = null
        if (newWidgetId == 0 || PermissionsUtil.mustRequestPermissions(this)) {
            restartIntent = MainActivity.intentToStartMe(this)
        } else if (widgetId != 0 && widgetId != newWidgetId) {
            restartIntent = MainActivity.intentToConfigure(this, newWidgetId)
        } else if (widgetId == 0) {
            widgetId = newWidgetId
            ApplicationPreferences.fromInstanceSettings(this, widgetId)
        }
        if (restartIntent != null) {
            widgetId = 0
            startActivity(restartIntent)
            finish()
        }
        return restartIntent == null
    }

    private fun restartIfNeeded() {
        if (widgetId != ApplicationPreferences.getWidgetId(this) ||
            PermissionsUtil.mustRequestPermissions(this)
        ) {
            widgetId = 0
            startActivity(MainActivity.intentToStartMe(this))
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_ID_BACKUP_SETTINGS -> if (resultCode == RESULT_OK && data != null) {
                backupSettings(data.data)
            }

            REQUEST_ID_RESTORE_SETTINGS -> if (resultCode == RESULT_OK && data != null) {
                restoreSettings(data.data)
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun backupSettings(uri: Uri?) {
        if (uri == null) return
        val settings = AllSettings.instanceFromId(this, widgetId)
        val jsonSettings: String = WidgetData.fromSettings(this, settings).toJsonString()
        var pfd: ParcelFileDescriptor? = null
        var out: FileOutputStream? = null
        try {
            pfd = this.contentResolver.openFileDescriptor(uri, "w")
            out = FileOutputStream(pfd!!.fileDescriptor)
            out.write(jsonSettings.toByteArray())
        } catch (e: Exception) {
            val msg = """Error while writing ${getText(R.string.app_name)} settings to $uri
${e.message}"""
            Log.w(TAG, msg, e)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (e2: IOException) {
                    Log.w(TAG, "Error while closing stream", e2)
                }
            }
            if (pfd != null) {
                try {
                    pfd.close()
                } catch (e2: IOException) {
                    Log.w(TAG, "Error while closing file descriptor", e2)
                }
            }
        }
        Toast.makeText(this, getText(R.string.backup_settings_title), Toast.LENGTH_LONG).show()
    }

    private fun restoreSettings(uri: Uri?) {
        if (uri == null) return
        val jsonObject = readJson(uri)
        if (jsonObject.length() == 0) return
        if (!AllSettings.restoreWidgetSettings(this, jsonObject, widgetId).isEmpty) {
            saveOnPause = false
            val duration = 3000
            val context = this@WidgetConfigurationActivity
            Toast.makeText(context, context.getText(R.string.restore_settings_title), Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                startActivity(intentToStartMe(context, widgetId))
                context.finish()
            }, duration.toLong())
        }
    }

    private fun readJson(uri: Uri): JSONObject {
        val BUFFER_LENGTH = 10000
        var `in`: InputStream? = null
        var reader: Reader? = null
        try {
            `in` = contentResolver.openInputStream(uri)
            val buffer = CharArray(BUFFER_LENGTH)
            val builder = StringBuilder()
            var count: Int
            reader = InputStreamReader(`in`, StandardCharsets.UTF_8)
            while (reader.read(buffer).also { count = it } != -1) {
                builder.append(buffer, 0, count)
            }
            return JSONObject(builder.toString())
        } catch (e: IOException) {
            val msg = """Error while reading ${getText(R.string.app_name)} settings from $uri
${e.message}"""
            Log.w(TAG, msg, e)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } catch (e: JSONException) {
            val msg = """Error while reading ${getText(R.string.app_name)} settings from $uri
${e.message}"""
            Log.w(TAG, msg, e)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Error while closing stream", e)
                }
            }
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    Log.w(TAG, "Error while closing reader", e)
                }
            }
        }
        return JSONObject()
    }

    companion object {
        val EXTRA_GOTO_PREFERENCES_SECTION: String =
            RemoteViewsFactory.PACKAGE + ".extra.GOTO_COLORS_PREFERENCES"
        const val EXTRA_GOTO_SECTION_COLORS = "colors"
        private val TAG = WidgetConfigurationActivity::class.java.simpleName
        const val FRAGMENT_TAG = "settings_fragment"
        const val REQUEST_ID_RESTORE_SETTINGS = 1
        const val REQUEST_ID_BACKUP_SETTINGS = 2
        fun intentToStartMe(context: Context?, widgetId: Int): Intent {
            return Intent(context, WidgetConfigurationActivity::class.java)
                .setData(Uri.parse("intent:configure$widgetId"))
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
    }
}
