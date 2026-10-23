package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * See https://github.com/andstatus/todoagenda/issues/28
 * @author yvolk@yurivolkov.com
 */
enum class ColorThemeType(
    val value: String,
    @field:StringRes val titleResId: Int,
    val manuallyDefinedColors: Boolean,
    val supportedByDevice: Boolean,
) {
    NONE(
        value = "none",
        titleResId = R.string.no_title,
        manuallyDefinedColors = true,
        supportedByDevice = false,
    ),
    LIGHT(
        value = "light",
        titleResId = R.string.colors_light,
        manuallyDefinedColors = true,
        supportedByDevice = true,
    ),
    DARK(
        value = "dark",
        titleResId = R.string.colors_dark,
        manuallyDefinedColors = true,
        supportedByDevice = ColorSettingsSource.TWO_MANUAL.supportedByDevice,
    ),
    TONAL_LIGHT(
        value = "tonalLight",
        titleResId = R.string.colors_light,
        manuallyDefinedColors = false,
        supportedByDevice = ColorSettingsSource.MATERIAL_YOU_TONAL.supportedByDevice,
    ),
    TONAL_DARK(
        value = "tonalDark",
        titleResId = R.string.colors_dark,
        manuallyDefinedColors = false,
        supportedByDevice = ColorSettingsSource.MATERIAL_YOU_TONAL.supportedByDevice,
    ),
    SEMANTIC_LIGHT(
        value = "semanticLight",
        titleResId = R.string.colors_light,
        manuallyDefinedColors = false,
        supportedByDevice = ColorSettingsSource.MATERIAL_YOU_SEMANTIC.supportedByDevice,
    ),
    SEMANTIC_DARK(
        value = "semanticDark",
        titleResId = R.string.colors_dark,
        manuallyDefinedColors = false,
        supportedByDevice = ColorSettingsSource.MATERIAL_YOU_SEMANTIC.supportedByDevice,
    ),
    ;

    val isValid: Boolean get() = this != NONE

    fun setTitle(activity: Activity?): ColorThemeType {
        activity?.setTitle(titleResId)
        return this
    }

    companion object {
        private val defaultValue = LIGHT

        fun fromValue(value: String?): ColorThemeType {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
