package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

enum class LastEntryAppearance(
    val value: String,
    @field:StringRes val valueResId: Int,
) {
    WITH_MESSAGE("with_message", R.string.last_entry_with_message),
    EMPTY_ENTRY("empty", R.string.last_entry_empty),
    HIDDEN("hidden", R.string.last_entry_hidden),
    ;

    companion object {
        val defaultValue = WITH_MESSAGE

        fun fromValue(value: String?): LastEntryAppearance {
            for (item in LastEntryAppearance.entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
