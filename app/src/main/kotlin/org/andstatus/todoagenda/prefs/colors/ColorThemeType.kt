package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.InstanceSettings

/**
 * See https://github.com/andstatus/todoagenda/issues/48
 * @author yvolk@yurivolkov.com
 */
enum class ColorThemeType(val value: String, @field:StringRes val titleResId: Int) {
    SINGLE("single", R.string.appearance_group_color_title),
    DARK("dark", R.string.colors_dark),
    LIGHT("light", R.string.colors_light),
    NONE("none", R.string.no_title);

    val isValid: Boolean
        get() = this != NONE

    fun fromEditor(context: Context?, differentColorsForDark: Boolean): ColorThemeType {
        return if (differentColorsForDark && canHaveDifferentColorsForDark()) {
            if (this == DARK || this == SINGLE && InstanceSettings.Companion.isDarkThemeOn(
                    context
                )
            ) {
                DARK
            } else {
                LIGHT
            }
        } else {
            if (this == LIGHT || this == SINGLE) {
                SINGLE
            } else {
                NONE
            }
        }
    }

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
        fun canHaveDifferentColorsForDark(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }
}
