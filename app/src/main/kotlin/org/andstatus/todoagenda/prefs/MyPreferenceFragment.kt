package org.andstatus.todoagenda.prefs

import androidx.preference.PreferenceFragmentCompat

abstract class MyPreferenceFragment : PreferenceFragmentCompat() {
    val TAG = this::class.java.simpleName
    val settings: InstanceSettings
        get() = AllSettings.instanceFromId(requireActivity(), widgetId)
    val widgetId: Int
        get() = ApplicationPreferences.getWidgetId(activity)

    fun saveSettings() {
        ApplicationPreferences.save(activity, widgetId)
    }
}
