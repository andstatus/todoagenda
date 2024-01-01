package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.colors.ColorThemeType;

import java.util.Optional;

public class RootFragment extends PreferenceFragmentCompat {

    @Override
    public void onResume() {
        super.onResume();
        Optional.ofNullable(getActivity())
                .ifPresent(a -> a.setTitle(ApplicationPreferences.getWidgetInstanceName(a)));
        setTitles();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_root);
        setTitles();
    }

    private void setTitles() {
        Context context = getContext();
        Preference preference = findPreference("ColorsPreferencesFragment");
        if (context != null && preference != null) {
            ColorThemeType themeType = ApplicationPreferences.getEditingColorThemeType(context);
            preference.setTitle(themeType.titleResId);
            preference.setVisible(themeType.isValid());
        }
    }
}