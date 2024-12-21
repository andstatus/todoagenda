package org.andstatus.todoagenda.prefs

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import org.andstatus.todoagenda.MainActivity
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.prefs.dateformat.DateFormatter
import org.andstatus.todoagenda.provider.QueryResultsStorage
import org.andstatus.todoagenda.util.DateUtil
import org.joda.time.DateTime
import java.util.TimeZone

class OtherPreferencesFragment : MyPreferenceFragment(), OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_other)
    }

    override fun onResume() {
        super.onResume()
        showWidgetInstanceName()
        showSnapshotMode()
        setLockTimeZone()
        showLockTimeZone()
        showRefreshPeriod()
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setLockTimeZone() {
        val preference = findPreference<CheckBoxPreference>(InstanceSettings.PREF_LOCK_TIME_ZONE)
        if (preference != null) {
            val snapshotMode = ApplicationPreferences.getSnapshotMode(requireActivity())
            val isChecked = snapshotMode == SnapshotMode.SNAPSHOT_TIME ||
                ApplicationPreferences.isTimeZoneLocked(requireActivity())
            if (preference.isChecked != isChecked) {
                preference.isChecked = isChecked
            }
        }
    }

    private fun showLockTimeZone() {
        val preference =
            findPreference<CheckBoxPreference>(InstanceSettings.PREF_LOCK_TIME_ZONE)
                ?: return
        val snapshotMode = ApplicationPreferences.getSnapshotMode(requireActivity())
        preference.isEnabled = snapshotMode != SnapshotMode.SNAPSHOT_TIME
        val timeZone = settings.clock.zone
        preference.summary = String.format(
            getText(if (preference.isChecked) R.string.lock_time_zone_on_desc else R.string.lock_time_zone_off_desc).toString(),
            timeZone.getName(DateTime.now(timeZone).millis)
        )
    }

    private fun showSnapshotMode() {
        val preference = findPreference<ListPreference>(InstanceSettings.PREF_SNAPSHOT_MODE)
            ?: return
        val settings = settings
        val entries = arrayOf(
            getText(R.string.snapshot_mode_live_data),
            formatSnapshotModeSummary(settings, R.string.snapshot_mode_time),
            formatSnapshotModeSummary(settings, R.string.snapshot_mode_now)
        )
        preference.entries = entries
        val snapshotMode = ApplicationPreferences.getSnapshotMode(requireActivity())
        if (snapshotMode.isSnapshotMode) {
            preference.summary = formatSnapshotModeSummary(settings, snapshotMode.valueResId)
        } else {
            preference.setSummary(snapshotMode.valueResId)
        }
    }

    private fun formatSnapshotModeSummary(settings: InstanceSettings, valueResId: Int): String {
        val snapshotDateString: CharSequence = if (settings.hasResults()) {
            DateFormatter(
                settings.context, DateFormatType.DEFAULT_WEEKDAY.defaultValue,
                settings.clock.now()
            ).formatDate(settings.resultsStorage!!.executedAt.get()).toString() +
                " " + DateUtil.formatTime({ settings }, settings.resultsStorage!!.executedAt.get())
        } else {
            "..."
        }
        return String.format(getText(valueResId).toString(), snapshotDateString)
    }

    private fun showRefreshPeriod() {
        val preference =
            findPreference<Preference>(InstanceSettings.PREF_REFRESH_PERIOD_MINUTES) as EditTextPreference?
        if (preference != null) {
            val value = ApplicationPreferences.getRefreshPeriodMinutes(requireActivity())
            preference.summary = String.format(getText(R.string.refresh_period_minutes_desc).toString(), value)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            InstanceSettings.PREF_LOCK_TIME_ZONE -> if (preference is CheckBoxPreference) {
                val timeZone = if (preference.isChecked) TimeZone.getDefault().id else ""
                ApplicationPreferences.setLockedTimeZoneId(
                    activity,
                    timeZone
                )
                settings.copy(lockedTimeZoneIdIn = timeZone)
                    .let { AllSettings.addNew(TAG, requireActivity(), it) }
                showLockTimeZone()
            }

            else -> {}
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            InstanceSettings.PREF_WIDGET_INSTANCE_NAME -> {
                requireActivity().finish()
                startActivity(
                    MainActivity.intentToConfigure(
                        activity, ApplicationPreferences.getWidgetId(
                            activity
                        )
                    )
                )
            }

            InstanceSettings.PREF_REFRESH_PERIOD_MINUTES -> showRefreshPeriod()
            InstanceSettings.PREF_SNAPSHOT_MODE -> {
                val snapshotMode = ApplicationPreferences.getSnapshotMode(requireActivity())
                val settings = settings.copy(
                    snapshotModeIn = snapshotMode,
                    resultsStorage = if (snapshotMode.isSnapshotMode && !settings.hasResults()) {
                        QueryResultsStorage.getNewResults(requireActivity(), settings.widgetId)
                    } else {
                        settings.resultsStorage
                    }
                )
                settings.save(key, "newResultsForSnapshotMode")
                AllSettings.addNew(TAG, requireActivity(), settings)
                showSnapshotMode()
                setLockTimeZone()
                showLockTimeZone()
            }

            else -> {}
        }
    }

    private fun showWidgetInstanceName() {
        val preference = findPreference<Preference>(InstanceSettings.PREF_WIDGET_INSTANCE_NAME)
        if (preference != null) {
            preference.summary = ApplicationPreferences.getWidgetInstanceName(requireActivity()) +
                " (id:" + ApplicationPreferences.getWidgetId(activity) + ")"
        }
    }
}
