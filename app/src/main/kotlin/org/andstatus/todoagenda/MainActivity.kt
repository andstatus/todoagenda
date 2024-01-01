package org.andstatus.todoagenda

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.util.PermissionsUtil

/**
 * @author yvolk@yurivolkov.com
 */
class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    var permissionsGranted = false
    var listView: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Log.w("onCreate", "Failed to find layout", e)
            finish()
            return
        }
        listView = findViewById(android.R.id.list)
        checkPermissions()
        if (openThisActivity()) {
            updateScreen()
        }
    }

    private fun checkPermissionsAndRequestThem() {
        checkPermissions()
        if (!permissionsGranted) {
            val neededPermissions: List<String> = EventProviderType.neededPermissions.toList()
            Log.d(this.localClassName, "Requesting permissions: $neededPermissions")
            val arr: Array<String> = Array(neededPermissions.size) {i -> neededPermissions[i] }
            ActivityCompat.requestPermissions(this, arr, 1)
        }
    }

    private fun checkPermissions() {
        permissionsGranted = PermissionsUtil.arePermissionsGranted(this)
    }

    private fun openThisActivity(): Boolean {
        var widgetIdToConfigure = 0
        if (permissionsGranted) {
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
        var messageResourceId = R.string.permissions_justification
        if (permissionsGranted) {
            messageResourceId = if (AllSettings.getInstances(this).isEmpty()) {
                R.string.no_widgets_found
            } else {
                R.string.select_a_widget_to_configure
            }
        }
        val message = findViewById<TextView>(R.id.message)
        message?.setText(messageResourceId)
        if (!AllSettings.getInstances(this).isEmpty() && permissionsGranted) {
            fillWidgetList()
            listView!!.visibility = View.VISIBLE
        } else {
            listView!!.visibility = View.GONE
        }
        val goToHomeScreenButton = findViewById<Button>(R.id.go_to_home_screen_button)
        if (goToHomeScreenButton != null) {
            goToHomeScreenButton.visibility = if (permissionsGranted &&
                AllSettings.getInstances(this).isEmpty()
            ) View.VISIBLE else View.GONE
        }
        val grantPermissionsButton = findViewById<Button>(R.id.grant_permissions)
        if (grantPermissionsButton != null) {
            grantPermissionsButton.visibility = if (permissionsGranted) View.GONE else View.VISIBLE
        }
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
        listView!!.adapter = SimpleAdapter(
            this,
            data,
            R.layout.widget_list_item,
            arrayOf(KEY_VISIBLE_NAME),
            intArrayOf(R.id.widget_name)
        )
        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
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

    fun grantPermissions(view: View?) {
        checkPermissionsAndRequestThem()
        updateScreen()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        AllSettings.ensureLoadedFromFiles(this, true)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissions()
        updateScreen()
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
