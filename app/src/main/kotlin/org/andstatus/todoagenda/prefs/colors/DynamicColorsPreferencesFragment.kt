package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings

class DynamicColorsPreferencesFragment : ColorsPreferencesFragment() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        val context: FragmentActivity = requireActivity()
        setTitle(context)
        addPreferencesFromResource(R.xml.preferences_colors_dynamic)
        showSupportedColorSettingsSources()
    }

    override fun onResume() {
        super.onResume()
        showColorSettingsSources()
        showDarkLightTheme()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?,
    ) {
        val activity = activity ?: return
        when (key) {
            InstanceSettings.PREF_COLOR_SETTINGS_SOURCE -> {
                onColorSettingSourceChanged(activity)
            }

            InstanceSettings.PREF_DARK_LIGHT_THEME -> {
                showDarkLightTheme()
                setTitle(activity)
            }

            else -> {
                saveSettings()
                showColorSettingsSources()
            }
        }
    }

    fun showDarkLightTheme() {
        val context: Context = activity ?: return
        val darkLightTheme: DarkLightTheme =
            ApplicationPreferences.getDarkLightTheme(context)
        val preference = findPreference<Preference>(InstanceSettings.PREF_DARK_LIGHT_THEME)
        if (preference != null) {
            preference.summary = context.getString(darkLightTheme.titleResId)
        }
    }
}
