package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.ApplicationPreferences.isAskForPermissions
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.util.PermissionsUtil

/**
 * @author yvolk@yurivolkov.com
 */
class MainActivity : MyActivity(), OnRequestPermissionsResultCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle(R.string.app_name)
        AllSettings.reInitialize(this)
        if (isOpenThisActivity()) {
            updateScreen()
        }
    }

    private fun isOpenThisActivity(): Boolean {
        var widgetIdToConfigure = 0
        if (!PermissionsUtil.mustRequestPermissions(this)) {
            widgetIdToConfigure = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
            if (widgetIdToConfigure == 0 && AllSettings.getInstances(this).size == 1) {
                widgetIdToConfigure = AllSettings.getInstances(this).keys.iterator().next()
            }
            if (widgetIdToConfigure != 0) {
                val intent: Intent = WidgetConfigurationActivity.intentToStartMe(this, widgetIdToConfigure)
                IntentUtil.copyStringExtra(
                    getIntent(),
                    intent,
                    WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION
                )
                startActivity(intent)
                finish()
            }
        }
        return widgetIdToConfigure == 0
    }

    private fun updateScreen() {
        val needToRequestPermission = PermissionsUtil.mustRequestPermissions(this)
        val messageResourceId = if (needToRequestPermission) {
            R.string.permissions_justification
        } else {
            if (AllSettings.getInstances(this).isEmpty()) {
                R.string.no_widgets_found
            } else {
                R.string.select_a_widget_to_configure
            }
        }
        var text = this.getText(messageResourceId)
        if (needToRequestPermission) {
            text = text.toString() + EventProviderType.neededPermissions
                .fold("\n") { acc, it -> "$acc\n$it" }
        }
        findViewById<TextView>(R.id.message)?.setText(text)

        findViewById<ListView>(R.id.instancesList)?.visibility =
            if (!needToRequestPermission && AllSettings.getInstances(this).isNotEmpty()) {
                fillWidgetList()
                View.VISIBLE
            } else {
                View.GONE
            }
        findViewById<Button>(R.id.grant_permissions)?.visibility =
            if (needToRequestPermission) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.dont_ask_for_permissions_button)?.visibility =
            if (needToRequestPermission && isAskForPermissions(this) &&
                EventProviderType.availableSources.isNotEmpty()
            ) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.go_to_home_screen_button)?.visibility =
            if (!needToRequestPermission && AllSettings.getInstances(this).isEmpty()) View.VISIBLE else View.GONE

        EnvironmentChangedReceiver.updateAllWidgets(this)
    }

    private fun fillWidgetList() {
        val data: MutableList<Map<String, String>> = ArrayList()
        for (settings in AllSettings.getInstances(this).values) {
            val map: MutableMap<String, String> = HashMap()
            map[KEY_VISIBLE_NAME] = settings.widgetInstanceName
            map[KEY_ID] = Integer.toString(settings.widgetId)
            data.add(map)
        }
        findViewById<ListView>(R.id.instancesList)?.let { listView ->
            listView.adapter = SimpleAdapter(
                this,
                data,
                R.layout.widget_list_item,
                arrayOf(KEY_VISIBLE_NAME),
                intArrayOf(R.id.widget_name)
            )
            listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                val stringStringMap = data[position]
                val widgetId: String? = stringStringMap[KEY_ID]
                if (widgetId.isNullOrBlank()) {
                    Log.w("fillWidgetList", "No $KEY_ID in $stringStringMap")
                } else {
                    val intent: Intent = WidgetConfigurationActivity.intentToStartMe(
                        this@MainActivity, Integer.valueOf(widgetId)
                    )
                    startActivity(intent)
                }
                finish()
            }
        }
    }

    fun onGrantPermissionsButtonClick(view: View?) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun onDontAskForPermissionsButtonClick(view: View?) {
        ApplicationPreferences.setAskForPermissions(this, false)
        onHomeButtonClick(view)
    }

    fun onHomeButtonClick(view: View?) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    companion object {
        protected const val KEY_VISIBLE_NAME = "visible_name"
        protected const val KEY_ID = "id"
        fun intentToConfigure(context: Context?, widgetId: Int): Intent {
            return intentToStartMe(context)
                .setData(Uri.parse("intent:configureMain$widgetId"))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }

        fun intentToStartMe(context: Context?): Intent {
            return Intent(context!!.applicationContext, MainActivity::class.java)
                .setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
        }
    }
}
