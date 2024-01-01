package org.andstatus.todoagenda.prefs

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R
import java.util.Optional

class RootFragment : PreferenceFragmentCompat() {
    override fun onResume() {
        super.onResume()
        Optional.ofNullable(activity)
            .ifPresent { a: FragmentActivity -> a.title = ApplicationPreferences.getWidgetInstanceName(a) }
        setTitles()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_root)
        setTitles()
    }

    private fun setTitles() {
        val context = context
        val preference = findPreference<Preference>("ColorsPreferencesFragment")
        if (context != null && preference != null) {
            val themeType = ApplicationPreferences.getEditingColorThemeType(context)
            preference.setTitle(themeType!!.titleResId)
            preference.isVisible = themeType.isValid
        }
    }
}
