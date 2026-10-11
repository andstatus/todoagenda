package org.andstatus.todoagenda.prefs

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.ApplicationPreferences.getSupportedColorSettingsSource
import org.andstatus.todoagenda.prefs.colors.DynamicColorsPreferencesFragment
import org.andstatus.todoagenda.prefs.colors.ManualColorsPreferencesFragment

class RootFragment : PreferenceFragmentCompat() {
    override fun onResume() {
        super.onResume()
        activity?.let { a: FragmentActivity ->
            a.title = ApplicationPreferences.getWidgetInstanceName(a)
        }
        setTitlesAndVisibility()
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.preferences_root)
        setTitlesAndVisibility()
    }

    private fun setTitlesAndVisibility() {
        val context = context ?: return
        val colorSettingsSource = getSupportedColorSettingsSource(context)
        val titleResId = ApplicationPreferences.colorsPreferencesTitleResId(context)
        if (colorSettingsSource.manuallyDefinedColors) {
            findPreference<Preference>(ManualColorsPreferencesFragment::class.simpleName!!)?.let { preference ->
                preference.setTitle(titleResId)
                preference.isVisible = true
            }
            findPreference<Preference>(DynamicColorsPreferencesFragment::class.simpleName!!)?.let { preference ->
                preference.isVisible = false
            }
        } else {
            findPreference<Preference>(ManualColorsPreferencesFragment::class.simpleName!!)?.let { preference ->
                preference.isVisible = false
            }
            findPreference<Preference>(DynamicColorsPreferencesFragment::class.simpleName!!)?.let { preference ->
                preference.setTitle(titleResId)
                preference.isVisible = true
            }
        }
    }
}
