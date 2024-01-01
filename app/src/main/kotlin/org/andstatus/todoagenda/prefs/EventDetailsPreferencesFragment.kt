package org.andstatus.todoagenda.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R

class EventDetailsPreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_event_details)
    }
}
