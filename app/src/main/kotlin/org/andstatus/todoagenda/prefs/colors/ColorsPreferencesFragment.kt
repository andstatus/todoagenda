package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.fragment.app.FragmentActivity
import androidx.preference.ListPreference
import org.andstatus.todoagenda.MainActivity
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_COLOR_SETTINGS_SOURCE
import org.andstatus.todoagenda.prefs.MyPreferenceFragment

abstract class ColorsPreferencesFragment :
    MyPreferenceFragment(),
    OnSharedPreferenceChangeListener {
    override fun onResume() {
        super.onResume()
        val context: FragmentActivity = requireActivity()
        setTitle(context)
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
        showColorSettingsSources()
        showTextShadow()
        showTimeUntilBackgroundSource()
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun showColorSettingsSources() {
        val context: Context = activity ?: return
        val colorSettingsSource: ColorSettingsSource =
            ApplicationPreferences.getSupportedColorSettingsSource(context)
        val preference = findPreference<ListPreference>(PREF_COLOR_SETTINGS_SOURCE)
        if (preference != null) {
            preference.summary = context.getString(colorSettingsSource.titleResId)
            val supported = ColorSettingsSource.entries.filter { it.supportedByDevice }
            if (supported.size > 1) {
                preference.entries =
                    supported.map { context.getString(it.titleResId) }.toTypedArray()
                preference.entryValues = supported.map { it.value }.toTypedArray()
            } else {
                removePreferenceImproved(preference)
            }
        }
    }

    fun setTitle(context: FragmentActivity) {
        context.setTitle(ApplicationPreferences.colorsPreferencesTitleResId(context))
    }

    fun showTextShadow() {
        findPreference<ListPreference>(ColorTheme.PREF_TEXT_SHADOW)?.let { preference ->
            TextShadow.fromValue(preference.value).let { textShadow ->
                preference.summary = requireActivity().getString(textShadow.titleResId)
            }
        }
    }

    fun showTimeUntilBackgroundSource() {
        findPreference<ListPreference>(ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE)?.let { preference ->
            TimeUntilBackgroundSource.fromValue(preference.value).let { source ->
                preference.summary = requireActivity().getString(source.titleResId)
            }
        }
    }

    fun onColorSettingSourceChanged(activity: FragmentActivity) {
        if (restartIfThemeChanged(activity)) return
        setTitle(activity)
        showColorSettingsSources()
    }

    /** When the settings, stored in the preferences, belong to another theme, restarts the
     * preferences screen in order to edit only the theme, which is actually selected now.
     * @return true if the screen was restarted
     */
    fun restartIfThemeChanged(activity: FragmentActivity): Boolean {
        if (ApplicationPreferences.getBeingEditedColorThemeType(activity) != ColorThemeType.NONE) {
            return false
        }
        val intent = MainActivity.intentToConfigure(activity, widgetId)
        intent.putExtra(
            WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION,
            WidgetConfigurationActivity.EXTRA_GOTO_SECTION_COLORS,
        )
        activity.startActivity(intent)
        activity.finish()
        return true
    }
}
