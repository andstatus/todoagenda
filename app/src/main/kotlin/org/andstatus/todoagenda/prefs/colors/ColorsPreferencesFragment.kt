package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import org.andstatus.todoagenda.MainActivity
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
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    fun showColorSettingsSources() {
        val context: Context = activity ?: return
        val colorSettingsSource: ColorSettingsSource =
            ApplicationPreferences.getSupportedColorSettingsSource(context)
        val preference = findPreference<Preference>(PREF_COLOR_SETTINGS_SOURCE)
        if (preference != null) {
            preference.summary = context.getString(colorSettingsSource.titleResId)
        }
    }

    fun setTitle(context: FragmentActivity) {
        context.setTitle(ApplicationPreferences.colorsPreferencesTitleResId(context))
    }

    fun onColorSettingSourceChanged(activity: FragmentActivity) {
        if (ApplicationPreferences.getBeingEditedColorThemeType(activity) == ColorThemeType.UNSPECIFIED) {
            activity.startActivity(
                MainActivity.intentToConfigure(
                    activity,
                    ApplicationPreferences.getWidgetId(activity),
                ),
            )
            activity.finish()
            return
        }
        setTitle(activity)
        showColorSettingsSources()
    }
}
