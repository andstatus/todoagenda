package org.andstatus.todoagenda.prefs;

import androidx.preference.PreferenceFragmentCompat;

public abstract class MyPreferenceFragment extends PreferenceFragmentCompat {

    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(getActivity(), getWidgetId());
    }

    public int getWidgetId() {
        return ApplicationPreferences.getWidgetId(getActivity());
    }

    public void saveSettings() {
        ApplicationPreferences.save(getActivity(), getWidgetId());
    }
}
