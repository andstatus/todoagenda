package org.andstatus.todoagenda.prefs

import android.graphics.LightingColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.provider.EventProviderType

class EventSourcesPreferencesFragment : MyPreferenceFragment() {
    var savedActiveSources: List<OrderedEventSource>? = emptyList<OrderedEventSource>()
    var clickedSources: MutableList<EventSource> = ArrayList()
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_event_sources)
    }

    override fun onResume() {
        super.onResume()
        loadActiveSources()
    }

    private fun loadActiveSources() {
        val activeSourcesNew = ApplicationPreferences.getActiveEventSources(requireActivity())
        if (savedActiveSources != activeSourcesNew) {
            savedActiveSources = activeSourcesNew
            Log.i(
                TAG, """
     $this
     Loaded ${savedActiveSources!!.size}
     """.trimIndent()
            )
            showAllSources(activeSourcesNew)
        }
    }

    private fun showAllSources(activeSources: List<OrderedEventSource>?) {
        preferenceScreen.removeAll()
        val added: MutableList<EventSource> = ArrayList()
        for (saved in activeSources!!) {
            added.add(saved.source)
            addAsPreference(saved.source, true)
        }
        for (clicked in clickedSources) {
            if (!added.contains(clicked)) {
                added.add(clicked)
                addAsPreference(clicked, false)
            }
        }
        if (settings.isLiveMode) {
            for (available in EventProviderType.availableSources) {
                if (!added.contains(available.source)) {
                    added.add(available.source)
                    addAsPreference(available.source, false)
                }
            }
        }
    }

    private fun addAsPreference(source: EventSource?, isChecked: Boolean) {
        val checkboxPref = CheckBoxPreference(requireActivity())
        checkboxPref.title =
            (if (source!!.isAvailable) "" else getText(R.string.not_found).toString() + ": ") + source.title
        checkboxPref.summary = source.summary
        checkboxPref.icon = getDrawable(source.providerType.isCalendar, source.color)
        checkboxPref.extras.putString(SOURCE_ID, source.toStoredString())
        preferenceScreen.addPreference(checkboxPref)
        checkboxPref.isChecked = isChecked
    }

    private fun getDrawable(isCalendar: Boolean, color: Int): Drawable {
        val drawable = resources.getDrawable(
            if (isCalendar) R.drawable.prefs_calendar_entry else R.drawable.task_icon
        )
        drawable.colorFilter = LightingColorFilter(0x0, color)
        return drawable
    }

    override fun onPause() {
        if (selectedSources != savedActiveSources) {
            saveSelectedSources()
        }
        super.onPause()
    }

    fun saveSelectedSources() {
        synchronized(setLock) {
            val selectedSources = selectedSources
            Log.i(
                TAG, """
     $this
     Saving ${selectedSources.size}
     """.trimIndent()
            )
            ApplicationPreferences.setActiveEventSources(activity, selectedSources)
            savedActiveSources = selectedSources
        }
        loadSelectedInOtherInstances()
    }

    private val selectedSources: List<OrderedEventSource>
        get() {
            val preferenceScreen = preferenceScreen
            val prefCount = preferenceScreen.preferenceCount
            val checkedSources = getCheckedSources(preferenceScreen, prefCount)
            val clickedSelectedSources: MutableList<EventSource> = ArrayList()
            for (clicked in clickedSources) {
                if (checkedSources.contains(clicked)) {
                    checkedSources.remove(clicked)
                    clickedSelectedSources.add(clicked)
                }
            }
            // Previously selected sources are first
            val selectedSources: MutableList<OrderedEventSource> =
                OrderedEventSource.fromSources(checkedSources)
            // Then recently selected sources go
            return OrderedEventSource.addAll(selectedSources, clickedSelectedSources)
        }

    private fun getCheckedSources(preferenceScreen: PreferenceScreen, prefCount: Int): MutableList<EventSource> {
        val checkedSources: MutableList<EventSource> = ArrayList()
        for (i in 0 until prefCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference is CheckBoxPreference) {
                val checkBox = preference
                if (checkBox.isChecked) {
                    val eventSource: EventSource = EventSource.fromStoredString(
                        checkBox.extras.getString(
                            SOURCE_ID
                        )
                    )
                    if (eventSource !== EventSource.EMPTY) {
                        checkedSources.add(eventSource)
                    }
                }
            }
        }
        return checkedSources
    }

    private fun loadSelectedInOtherInstances() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (fragment in requireActivity().supportFragmentManager.fragments) {
                if (fragment is EventSourcesPreferencesFragment && fragment !== this) {
                    Log.i(TAG, "$this\nFound loaded $fragment")
                    fragment.loadActiveSources()
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference is CheckBoxPreference) {
            val source: EventSource =
                EventSource.fromStoredString(preference.getExtras().getString(SOURCE_ID))
            clickedSources.remove(source)
            clickedSources.add(source) // last clicked is the last in the list
            showAllSources(selectedSources)
        }
        return super.onPreferenceTreeClick(preference)
    }

    companion object {
        private val TAG = EventSourcesPreferencesFragment::class.java.simpleName
        private const val SOURCE_ID = "sourceId"
        private val setLock = Any()
    }
}
