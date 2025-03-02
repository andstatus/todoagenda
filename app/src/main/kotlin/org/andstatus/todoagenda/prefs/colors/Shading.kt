package org.andstatus.todoagenda.prefs.colors

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class Shading(
    @field:ColorInt val widgetHeaderColor: Int,
    @field:ColorInt val dayHeaderColor: Int,
    @field:ColorInt val titleColor: Int,
    @field:StringRes val titleResId: Int,
    val themeName: String,
    @field:StyleRes val themeResId: Int,
    @field:DrawableRes val timeUntilBackgroundResId: Int,
    val timeUntilBackgroundSource: TimeUntilBackgroundSource,
) {
    // For historical reasons we store theme names for text shadings i.e. "BLACK" theme for WHITE text shading
    WHITE(
        -0x23000001,
        Color.WHITE,
        Color.WHITE,
        R.string.appearance_theme_black,
        "BLACK",
        R.style.Theme_ToDoAgenda_Black,
        R.drawable.time_until_black,
        TimeUntilBackgroundSource.BLACK,
    ),
    LIGHT(
        -0x65000001,
        -0x333334,
        -0x333334,
        R.string.appearance_theme_dark,
        "DARK",
        R.style.Theme_ToDoAgenda_Dark,
        R.drawable.time_until_black,
        TimeUntilBackgroundSource.BLACK,
    ),
    DARK(
        -0x67000000,
        -0x888889,
        -0xaaaaab,
        R.string.appearance_theme_light,
        "LIGHT",
        R.style.Theme_ToDoAgenda_Light,
        R.drawable.time_until_white,
        TimeUntilBackgroundSource.WHITE,
    ),
    BLACK(
        -0x34000000,
        Color.BLACK,
        Color.BLACK,
        R.string.appearance_theme_white,
        "WHITE",
        R.style.Theme_ToDoAgenda_White,
        R.drawable.time_until_white,
        TimeUntilBackgroundSource.WHITE,
    ),
    ;

    companion object {
        fun fromThemeName(
            themeName: String?,
            defaultShading: Shading,
        ): Shading {
            for (item in entries) {
                if (item.themeName == themeName) {
                    return item
                }
            }
            return defaultShading
        }
    }
}
