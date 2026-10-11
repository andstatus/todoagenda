package org.andstatus.todoagenda.prefs.colors

import android.os.Build
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.Companion.canHaveDifferentColorsForDark

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
        value = "two",
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
        supportedByDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    ),
    ;

    fun toSupportedByDevice(): ColorSettingsSource = if (this.supportedByDevice) this else SINGLE_MANUAL

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
