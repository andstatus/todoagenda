package org.andstatus.todoagenda.prefs

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

abstract class MyPreferenceFragment : PreferenceFragmentCompat() {
    val TAG = this::class.simpleName!!
    val settings: InstanceSettings
        get() = AllSettings.instanceFromId(requireActivity(), widgetId)
    val widgetId: Int
        get() = ApplicationPreferences.getWidgetId(activity)

    fun saveSettings() {
        ApplicationPreferences.save(activity, widgetId)
    }

    fun removePreferenceImproved(preferenceName: String?) {
        val preference = findPreference<Preference>(preferenceName!!)
        val screen = preferenceScreen
        if (screen != null && preference != null) {
            val group = preference.parent
            group?.removePreference(preference) ?: screen.removePreference(preference)
        }
    }
}
