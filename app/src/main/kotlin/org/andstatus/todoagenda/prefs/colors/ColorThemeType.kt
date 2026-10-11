package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import android.os.Build
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
) {
    UNSPECIFIED(value = "none", titleResId = R.string.no_title, manuallyDefinedColors = true),
    SINGLE(
        value = "single",
        titleResId = R.string.appearance_group_color_title,
        manuallyDefinedColors = true,
    ),
    DARK(value = "dark", titleResId = R.string.colors_dark, manuallyDefinedColors = true),
    LIGHT(value = "light", titleResId = R.string.colors_light, manuallyDefinedColors = true),
    TONAL_LIGHT(
        value = "tonalLight",
        titleResId = R.string.colors_light,
        manuallyDefinedColors = false,
    ),
    TONAL_DARK(
        value = "tonalDark",
        titleResId = R.string.colors_dark,
        manuallyDefinedColors = false,
    ),
    SEMANTIC_LIGHT(
        value = "semanticLight",
        titleResId = R.string.colors_light,
        manuallyDefinedColors = false,
    ),
    SEMANTIC_DARK(
        value = "semanticDark",
        titleResId = R.string.colors_dark,
        manuallyDefinedColors = false,
    ),
    ;

    val isValid: Boolean get() = this != UNSPECIFIED

    fun setTitle(activity: Activity?): ColorThemeType {
        activity?.setTitle(titleResId)
        return this
    }

    companion object {
        private val defaultValue = SINGLE

        fun fromValue(value: String?): ColorThemeType {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }

        /** See https://developer.android.com/guide/topics/ui/look-and-feel/darktheme  */
        val canHaveDifferentColorsForDark: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}
