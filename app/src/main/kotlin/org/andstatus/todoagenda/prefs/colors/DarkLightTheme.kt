package org.andstatus.todoagenda.prefs.colors

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

enum class DarkLightTheme(
    val value: String,
    @field:StringRes val titleResId: Int,
) {
    AUTO("auto", R.string.color_theme_auto),
    DARK("dark", R.string.appearance_theme_light),
    LIGHT("light", R.string.appearance_theme_dark),
    ;

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
