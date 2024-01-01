package org.andstatus.todoagenda.prefs.colors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.rarepebble.colorpicker.ColorPreference;
import com.rarepebble.colorpicker.ColorPreferenceDialog;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.WidgetConfigurationActivity;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.MyPreferenceFragment;
import org.andstatus.todoagenda.widget.TimeSection;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.andstatus.todoagenda.WidgetConfigurationActivity.EXTRA_GOTO_SECTION_COLORS;
import static org.andstatus.todoagenda.WidgetConfigurationActivity.FRAGMENT_TAG;
import static org.andstatus.todoagenda.prefs.ApplicationPreferences.PREF_DIFFERENT_COLORS_FOR_DARK;
import static org.andstatus.todoagenda.prefs.colors.ThemeColors.PREF_TEXT_COLOR_SOURCE;

/** AndroidX version created by yvolk@yurivolkov.com
 *   based on this answer: https://stackoverflow.com/a/53290775/297710
 *   and on the code of https://github.com/koji-1009/ChronoDialogPreference
 */
public class ColorsPreferencesFragment extends MyPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setTitle();
        addPreferencesFromResource(R.xml.preferences_colors);
        removeUnavailablePreferences();
    }

    private void setTitle() {
        ApplicationPreferences.getEditingColorThemeType(getActivity()).setTitle(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        removeUnavailablePreferences();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        showTextSources();
    }

    private void showTextSources() {
        Context context = getActivity();
        if (context != null) {
            TextColorSource textColorSource = ApplicationPreferences.getTextColorSource(context);
            Preference preference = findPreference(PREF_TEXT_COLOR_SOURCE);
            if (preference != null) {
                preference.setSummary(context.getString(textColorSource.titleResId) + "\n" +
                        context.getString(textColorSource.summaryResId));
            }
            if (textColorSource == TextColorSource.SHADING) {
                showShadings();
            }
            previewTextOnBackground();
        }
    }

    private void previewTextOnBackground() {
        ThemeColors colors = getSettings().colors();
        for(BackgroundColorPref backgroundColorPref: BackgroundColorPref.values()) {
            ColorPreference colorPreference = findPreference(backgroundColorPref.colorPreferenceName);
            if (colorPreference != null) {
                List<TextColorPref> toPreview = Arrays.stream(TextColorPref.values())
                        .filter(pref -> pref.backgroundColorPref == backgroundColorPref).collect(Collectors.toList());
                if (toPreview.size() > 0) {
                    TextColorPref pref = toPreview.get(0);
                    colorPreference.setSampleTextColor1(colors.getTextColor(pref, pref.colorAttrId));
                }
                if(toPreview.size() > 1) {
                    TextColorPref pref = toPreview.get(1);
                    colorPreference.setSampleTextColor2(colors.getTextColor(pref, pref.colorAttrId));
                }
            }
        }
    }

    private void removeUnavailablePreferences() {
        Context context = getActivity();
        if (context == null) return;

        ColorThemeType colorThemeType = ApplicationPreferences.getColorThemeType(context);
        if (!ColorThemeType.canHaveDifferentColorsForDark() ||
                colorThemeType == ColorThemeType.LIGHT ||
                colorThemeType == ColorThemeType.SINGLE && !InstanceSettings.isDarkThemeOn(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(PREF_DIFFERENT_COLORS_FOR_DARK);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        if (ApplicationPreferences.noPastEvents(context)) {
            PreferenceScreen screen = getPreferenceScreen();
            Preference preference = findPreference(TimeSection.PAST.preferenceCategoryKey);
            if (screen != null && preference != null) {
                screen.removePreference(preference);
            }
        }
        switch (ApplicationPreferences.getTextColorSource(context)) {
            case AUTO:
                removeShadings();
                removeTextColors();
                break;
            case SHADING:
                removeTextColors();
                break;
            case COLORS:
                removeShadings();
                break;
        }
    }

    private void removeShadings() {
        for (TextColorPref pref : TextColorPref.values()) {
            removePreferenceImproved(pref.shadingPreferenceName);
        }
    }

    private void removePreferenceImproved(String preferenceName) {
        Preference preference = findPreference(preferenceName);
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null && preference != null) {
            PreferenceGroup group = preference.getParent();
            if (group != null) {
                group.removePreference(preference);
            } else {
                screen.removePreference(preference);
            }
        }
    }

    private void removeTextColors() {
        for (TextColorPref pref : TextColorPref.values()) {
            removePreferenceImproved(pref.colorPreferenceName);
        }
    }

    private void showShadings() {
        for (TextColorPref shadingPref : TextColorPref.values()) {
            ListPreference preference = findPreference(shadingPref.shadingPreferenceName);
            if (preference != null) {
                Shading shading = Shading.fromThemeName(preference.getValue(), shadingPref.defaultShading);
                preference.setSummary(getActivity().getString(shading.titleResId));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        FragmentActivity activity = getActivity();
        switch (key) {
            case PREF_DIFFERENT_COLORS_FOR_DARK:
                if (activity != null) {
                    if (ApplicationPreferences.getEditingColorThemeType(activity) == ColorThemeType.NONE) {
                        activity.startActivity(MainActivity.intentToConfigure(activity, ApplicationPreferences.getWidgetId(activity)));
                        activity.finish();
                        return;
                    };
                    setTitle();
                }
                break;
            case PREF_TEXT_COLOR_SOURCE:
                if (activity != null) {
                    Intent intent = MainActivity.intentToConfigure(activity, ApplicationPreferences.getWidgetId(activity));
                    intent.putExtra(WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION, EXTRA_GOTO_SECTION_COLORS);
                    activity.startActivity(intent);
                    activity.finish();
                    return;
                }
                break;
            default:
                saveSettings();
                showTextSources();
                break;
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment dialogFragment = null;
        if (preference instanceof ColorPreference) {
            dialogFragment = new ColorPreferenceDialog((ColorPreference) preference);
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getFragmentManager(), FRAGMENT_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}