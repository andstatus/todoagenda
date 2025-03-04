package org.andstatus.todoagenda.prefs.colors

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * See [#143](https://github.com/andstatus/todoagenda/issues/143)
 * @author yvolk@yurivolkov.com
 */
enum class TimeUntilBackgroundSource(
    val value: String,
    @field:StringRes val titleResId: Int,
    @field:DrawableRes val drawableResId: Int?,
    @ColorInt val textColor: Int?,
) {
    AUTO("auto", R.string.auto_color, null, null),
    BLACK("black", R.string.appearance_theme_white, R.drawable.time_until_black, Color.WHITE),
    WHITE("white", R.string.appearance_theme_black, R.drawable.time_until_white, Color.BLACK),
    YELLOW("yellow", R.string.color_yellow, R.drawable.time_until_yellow, Color.BLACK),
    ;

    companion object {
        val defaultEntry = AUTO

        fun fromValue(value: String?): TimeUntilBackgroundSource = entries.firstOrNull { it.value == value } ?: defaultEntry
    }
}
