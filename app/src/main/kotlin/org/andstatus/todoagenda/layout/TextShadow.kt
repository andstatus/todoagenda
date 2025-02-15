package org.andstatus.todoagenda.layout

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

enum class TextShadow(val value: String,
                      @field:StringRes val titleResId: Int,
    ) {
    NO_SHADOW("no", R.string.text_shadow_no_shadow),
    DARK_SHADOW("dark", R.string.text_shadow_dark),
    LIGHT_SHADOW("light", R.string.text_shadow_light);

    companion object {
        fun fromValue(value: String?): TextShadow {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return NO_SHADOW
        }
    }
}
