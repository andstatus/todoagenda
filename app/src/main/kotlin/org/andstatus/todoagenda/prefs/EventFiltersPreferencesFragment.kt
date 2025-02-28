package org.andstatus.todoagenda.prefs

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.ApplicationPreferences.limitToString

class EventFiltersPreferencesFragment :
    PreferenceFragmentCompat(),
    OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.preferences_event_filters)
    }

    override fun onResume() {
        super.onResume()
        showStatus()
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    private fun showStatus() {
        showEventsEnded()
        showEvenRange()
        showHideBasedOnKeywords()
        showShowBasedOnKeywords()
        showMaximumNumberOfEvents()
        showAllDayEventsPlacement()
        showTaskScheduling()
        showTasksWithoutDates()
        showFilterMode()
    }

    private fun showEventsEnded() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_EVENTS_ENDED) as ListPreference?
        preference!!.summary = preference.entry
    }

    private fun showEvenRange() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_EVENT_RANGE) as ListPreference?
        preference!!.summary = preference.entry
    }

    private fun showHideBasedOnKeywords() {
        val preference =
            findPreference<Preference>(InstanceSettings.PREF_HIDE_BASED_ON_KEYWORDS) as EditTextPreference?
        val filter = KeywordsFilter(false, preference!!.text)
        if (filter.isEmpty) {
            preference.setSummary(R.string.this_option_is_turned_off)
        } else {
            preference.summary = filter.toString()
        }
    }

    private fun showShowBasedOnKeywords() {
        val preference =
            findPreference<Preference>(InstanceSettings.PREF_SHOW_BASED_ON_KEYWORDS) as EditTextPreference?
        val filter = KeywordsFilter(true, preference!!.text)
        if (filter.isEmpty) {
            preference.setSummary(R.string.this_option_is_turned_off)
        } else {
            preference.summary = filter.toString()
        }
    }

    private fun showMaximumNumberOfEvents() =
        findPreference<Preference>(InstanceSettings.PREF_MAX_NUMBER_OF_EVENTS)?.setSummary(
            ApplicationPreferences.getMaxNumberOfEvents(requireActivity()).limitToString(),
        )

    private fun showAllDayEventsPlacement() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_ALL_DAY_EVENTS_PLACEMENT)
        preference?.setSummary(ApplicationPreferences.getAllDayEventsPlacement(requireActivity()).valueResId)
    }

    private fun showTaskScheduling() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_TASK_SCHEDULING)
        preference?.setSummary(ApplicationPreferences.getTaskScheduling(requireActivity()).valueResId)
    }

    private fun showTasksWithoutDates() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_TASK_WITHOUT_DATES)
        preference?.setSummary(ApplicationPreferences.getTasksWithoutDates(requireActivity()).valueResId)
    }

    private fun showFilterMode() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_FILTER_MODE)
        preference?.setSummary(ApplicationPreferences.getFilterMode(requireActivity()).valueResId)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?,
    ) {
        showStatus()
    }
}
