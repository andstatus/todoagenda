package org.andstatus.todoagenda.prefs.colors

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.InstanceSettings

class DynamicColorsPreferencesFragment : ColorsPreferencesFragment() {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        val context: FragmentActivity = requireActivity()
        setTitle(context)
        addPreferencesFromResource(R.xml.preferences_colors_dynamic)
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
                if (!restartIfThemeChanged(activity)) {
                    showDarkLightTheme()
                    setTitle(activity)
                }
            }

            else -> {
                saveSettings()
                showColorSettingsSources()
                showTextShadow()
                showTimeUntilBackgroundSource()
            }
        }
    }
}
