package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.rarepebble.colorpicker.ColorPreference
import com.rarepebble.colorpicker.ColorPreferenceDialog
import org.andstatus.todoagenda.MainActivity
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_TEXT_SHADOW
import org.andstatus.todoagenda.prefs.MyPreferenceFragment
import org.andstatus.todoagenda.layout.TimeSection
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_TIME_UNTIL_BACKGROUND_SOURCE
import java.util.Arrays
import java.util.stream.Collectors

/** AndroidX version created by yvolk@yurivolkov.com
 * based on this answer: https://stackoverflow.com/a/53290775/297710
 * and on the code of https://github.com/koji-1009/ChronoDialogPreference
 */
class ColorsPreferencesFragment :
    MyPreferenceFragment(),
    OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        setTitle()
        addPreferencesFromResource(R.xml.preferences_colors)
        removeUnavailablePreferences()
    }

    private fun setTitle() {
        ApplicationPreferences.getEditingColorThemeType(requireActivity()).setTitle(activity)
    }

    override fun onResume() {
        super.onResume()
        setTitle()
        removeUnavailablePreferences()
        preferenceManager.sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
        showTextSources()
        showTextShadow()
        showTimeUntilBackgroundSource()
    }

    private fun showTextSources() {
        val context: Context? = activity
        if (context != null) {
            val textColorSource = ApplicationPreferences.getTextColorSource(context)
            val preference = findPreference<Preference>(ThemeColors.PREF_TEXT_COLOR_SOURCE)
            if (preference != null) {
                preference.summary =
                    """
                    ${context.getString(textColorSource.titleResId)}
                    ${context.getString(textColorSource.summaryResId)}
                    """.trimIndent()
            }
            if (textColorSource == TextColorSource.SHADING) {
                showShadings()
            }
            previewTextOnBackground()
        }
    }

    private fun previewTextOnBackground() {
        val colors = settings.colors()
        for (backgroundColorPref in BackgroundColorPref.entries) {
            val colorPreference = findPreference<ColorPreference>(backgroundColorPref.colorPreferenceName)
            if (colorPreference != null) {
                val toPreview =
                    Arrays
                        .stream(TextColorPref.entries.toTypedArray())
                        .filter { pref: TextColorPref -> pref.backgroundColorPref == backgroundColorPref }
                        .collect(Collectors.toList())
                if (toPreview.size > 0) {
                    val pref = toPreview[0]
                    colorPreference.setSampleTextColor1(colors.getTextColor(pref, pref.colorAttrId))
                }
                if (toPreview.size > 1) {
                    val pref = toPreview[1]
                    colorPreference.setSampleTextColor2(colors.getTextColor(pref, pref.colorAttrId))
                }
            }
        }
    }

    private fun removeUnavailablePreferences() {
        val context = activity ?: return
        val colorThemeType = ApplicationPreferences.getColorThemeType(context)
        if (!ColorThemeType.canHaveDifferentColorsForDark() ||
            colorThemeType == ColorThemeType.LIGHT ||
            colorThemeType == ColorThemeType.SINGLE &&
            !InstanceSettings.isDarkThemeOn(
                context,
            )
        ) {
            val screen = preferenceScreen
            val preference = findPreference<Preference>(ApplicationPreferences.PREF_DIFFERENT_COLORS_FOR_DARK)
            if (screen != null && preference != null) {
                screen.removePreference(preference)
            }
        }
        if (ApplicationPreferences.noPastEvents(context)) {
            val screen = preferenceScreen
            val preference = findPreference<Preference>(TimeSection.PAST.preferenceCategoryKey)
            if (screen != null && preference != null) {
                screen.removePreference(preference)
            }
        }
        when (ApplicationPreferences.getTextColorSource(context)) {
            TextColorSource.AUTO -> {
                removeShadings()
                removeTextColors()
            }

            TextColorSource.SHADING -> removeTextColors()
            TextColorSource.COLORS -> removeShadings()
        }
    }

    private fun removeShadings() {
        for (pref in TextColorPref.entries) {
            removePreferenceImproved(pref.shadingPreferenceName)
        }
    }

    private fun removePreferenceImproved(preferenceName: String?) {
        val preference = findPreference<Preference>(preferenceName!!)
        val screen = preferenceScreen
        if (screen != null && preference != null) {
            val group = preference.parent
            group?.removePreference(preference) ?: screen.removePreference(preference)
        }
    }

    private fun removeTextColors() {
        for (pref in TextColorPref.entries) {
            removePreferenceImproved(pref.colorPreferenceName)
        }
    }

    private fun showTextShadow() {
        findPreference<ListPreference>(PREF_TEXT_SHADOW)?.let { preference ->
            TextShadow.fromValue(preference.value).let { textShadow ->
                preference.summary = requireActivity().getString(textShadow.titleResId)
            }
        }
    }

    private fun showTimeUntilBackgroundSource() {
        findPreference<ListPreference>(PREF_TIME_UNTIL_BACKGROUND_SOURCE)?.let { preference ->
            TimeUntilBackgroundSource.fromValue(preference.value).let { source ->
                preference.summary = requireActivity().getString(source.titleResId)
            }
        }
    }

    private fun showShadings() {
        for (shadingPref in TextColorPref.entries) {
            findPreference<ListPreference>(shadingPref.shadingPreferenceName)?.let { preference ->
                val shading: Shading = Shading.fromThemeName(preference.value, shadingPref.defaultShading)
                preference.summary = requireActivity().getString(shading.titleResId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?,
    ) {
        val activity = activity
        when (key) {
            ApplicationPreferences.PREF_DIFFERENT_COLORS_FOR_DARK ->
                if (activity != null) {
                    if (ApplicationPreferences.getEditingColorThemeType(activity) == ColorThemeType.NONE) {
                        activity.startActivity(
                            MainActivity.intentToConfigure(
                                activity,
                                ApplicationPreferences.getWidgetId(activity),
                            ),
                        )
                        activity.finish()
                        return
                    }
                    setTitle()
                }

            ThemeColors.PREF_TEXT_COLOR_SOURCE ->
                if (activity != null) {
                    val intent: Intent =
                        MainActivity.intentToConfigure(activity, ApplicationPreferences.getWidgetId(activity))
                    intent.putExtra(
                        WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION,
                        WidgetConfigurationActivity.EXTRA_GOTO_SECTION_COLORS,
                    )
                    activity.startActivity(intent)
                    activity.finish()
                    return
                }

            else -> {
                saveSettings()
                showTextSources()
                showTextShadow()
                showTimeUntilBackgroundSource()
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is ColorPreference) {
            dialogFragment = ColorPreferenceDialog(preference)
        }
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(requireFragmentManager(), WidgetConfigurationActivity.FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}
