package org.andstatus.todoagenda.prefs.colors

import android.os.Build
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/** See https://github.com/andstatus/todoagenda/issues/175#issuecomment-6098933074 */
enum class ColorSettingsSource(
    val value: String,
    @field:StringRes val titleResId: Int,
    val manuallyDefinedColors: Boolean,
    val supportedByDevice: Boolean,
) {
    SINGLE_MANUAL(
        value = "single",
        titleResId = R.string.color_settings_source_single_manual,
        manuallyDefinedColors = true,
        supportedByDevice = true,
    ),
    TWO_MANUAL(
        value = "manual",
        titleResId = R.string.color_settings_source_two_manual,
        manuallyDefinedColors = true,
        supportedByDevice = canHaveDifferentColorsForDark,
    ),
    MATERIAL_YOU_TONAL(
        value = "tonal",
        titleResId = R.string.color_settings_source_tonal,
        manuallyDefinedColors = false,
        supportedByDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    ),
    MATERIAL_YOU_SEMANTIC(
        value = "semantic",
        titleResId = R.string.color_settings_source_semantic,
        manuallyDefinedColors = false,
        supportedByDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
    ),
    ;

    fun toSupportedByDevice(): ColorSettingsSource = if (this.supportedByDevice) this else SINGLE_MANUAL

    /** The theme, which is displayed (and edited) for this source */
    fun themeType(
        darkLightTheme: DarkLightTheme,
        isDarkThemeOn: Boolean,
    ): ColorThemeType =
        when (this) {
            SINGLE_MANUAL -> ColorThemeType.LIGHT
            // Two manually defined themes always follow the device's dark theme
            TWO_MANUAL -> if (isDarkThemeOn) ColorThemeType.DARK else ColorThemeType.LIGHT
            MATERIAL_YOU_TONAL ->
                if (darkLightTheme.isDark(isDarkThemeOn)) {
                    ColorThemeType.TONAL_DARK
                } else {
                    ColorThemeType.TONAL_LIGHT
                }
            MATERIAL_YOU_SEMANTIC ->
                if (darkLightTheme.isDark(isDarkThemeOn)) {
                    ColorThemeType.SEMANTIC_DARK
                } else {
                    ColorThemeType.SEMANTIC_LIGHT
                }
        }

    companion object {
        fun fromValue(value: String?): ColorSettingsSource {
            for (item in ColorSettingsSource.entries) {
                if (item.value == value) {
                    return item
                }
            }
            return SINGLE_MANUAL
        }
    }
}
