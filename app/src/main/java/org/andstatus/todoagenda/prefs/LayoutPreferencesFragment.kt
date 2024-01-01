package org.andstatus.todoagenda.prefs

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.prefs.dateformat.DateFormatDialog
import org.andstatus.todoagenda.prefs.dateformat.DateFormatPreference

class LayoutPreferencesFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_layout)
    }

    override fun onResume() {
        super.onResume()
        showEventEntryLayout()
        showWidgetHeaderLayout()
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    private fun showEventEntryLayout() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_EVENT_ENTRY_LAYOUT)
        preference?.setSummary(ApplicationPreferences.getEventEntryLayout(requireActivity()).summaryResId)
    }

    private fun showWidgetHeaderLayout() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_WIDGET_HEADER_LAYOUT)
        preference?.setSummary(ApplicationPreferences.getWidgetHeaderLayout(requireActivity()).summaryResId)
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is DateFormatPreference) {
            dialogFragment = DateFormatDialog(preference)
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(requireFragmentManager(), WidgetConfigurationActivity.FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            InstanceSettings.PREF_EVENT_ENTRY_LAYOUT -> showEventEntryLayout()
            InstanceSettings.PREF_WIDGET_HEADER_LAYOUT -> showWidgetHeaderLayout()
            else -> {}
        }
    }
}
