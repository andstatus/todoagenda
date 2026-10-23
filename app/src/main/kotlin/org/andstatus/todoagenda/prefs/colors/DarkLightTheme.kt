package org.andstatus.todoagenda.prefs.colors

import android.os.Build
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/** See https://developer.android.com/guide/topics/ui/look-and-feel/darktheme  */
val canHaveDifferentColorsForDark: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

enum class DarkLightTheme(
    val value: String,
    @field:StringRes val titleResId: Int,
    val supportedByDevice: Boolean,
) {
    AUTO("auto", R.string.color_theme_auto, canHaveDifferentColorsForDark),
    DARK("dark", R.string.appearance_theme_light, canHaveDifferentColorsForDark),
    LIGHT("light", R.string.appearance_theme_dark, true),
    ;

    fun toSupportedByDevice(): DarkLightTheme = if (supportedByDevice) this else LIGHT

    companion object {
        val DEFAULT = AUTO

        fun fromValue(value: String?): DarkLightTheme {
            for (item in DarkLightTheme.entries) {
                if (item.value == value) {
                    return item
                }
            }
            return DEFAULT
        }
    }
}
