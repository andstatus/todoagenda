package org.andstatus.todoagenda.prefs

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.util.DateUtil

class FeedbackPreferencesFragment : PreferenceFragmentCompat() {
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val widgetId = ApplicationPreferences.getWidgetId(activity)
        ApplicationPreferences.save(activity, widgetId)
        when (preference.key) {
            KEY_SHARE_EVENTS_FOR_DEBUGGING -> QueryResultsStorage.shareEventsForDebugging(
                requireActivity(), widgetId
            )

            KEY_BACKUP_SETTINGS -> {
                val settings = AllSettings.instanceFromId(requireActivity(), widgetId)
                val fileName = (settings.widgetInstanceName + "-" + getText(R.string.app_name))
                    .replace("\\W+".toRegex(), "-") +
                    "-backup-" + DateUtil.formatLogDateTime(System.currentTimeMillis()) +
                    ".json"
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                intent.setType("application/json")
                intent.putExtra(Intent.EXTRA_TITLE, fileName)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                requireActivity().startActivityForResult(
                    intent,
                    WidgetConfigurationActivity.REQUEST_ID_BACKUP_SETTINGS
                )
            }

            KEY_RESTORE_SETTINGS -> {
                val intent2 = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                val withChooser = Intent.createChooser(
                    intent2,
                    requireActivity().getText(R.string.restore_settings_title)
                )
                requireActivity().startActivityForResult(
                    withChooser,
                    WidgetConfigurationActivity.REQUEST_ID_RESTORE_SETTINGS
                )
            }

            else -> {}
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_feedback)
    }

    companion object {
        private val TAG = FeedbackPreferencesFragment::class.java.simpleName
        private const val KEY_SHARE_EVENTS_FOR_DEBUGGING = "shareEventsForDebugging"
        private const val KEY_BACKUP_SETTINGS = "backupSettings"
        private const val KEY_RESTORE_SETTINGS = "restoreSettings"
    }
}
