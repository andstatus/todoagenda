package org.andstatus.todoagenda.prefs

/**
 * Vertical spacing between widget entries.
 *
 * [verticalPaddingMultiplier] is applied to the `entry_top_bottom_padding` dimension.
 * [COMPACT] additionally removes the horizontal padding (this is the old "Compact layout").
 */
enum class EntrySpacing(
    val preferenceValue: String,
    val verticalPaddingMultiplier: Float,
) {
    COMPACT("compact", 0f),
    NORMAL("normal", 1f),
    RELAXED("relaxed", 3f),
    LOOSE("loose", 6f),
    ;

    val isCompact: Boolean
        get() = this == COMPACT

    companion object {
        fun fromPreferenceValue(preferenceValue: String?): EntrySpacing =
            entries.firstOrNull { it.preferenceValue == preferenceValue } ?: NORMAL

        /** Migrates the old "Compact layout" checkbox, which was stored as a separate boolean */
        fun fromLegacyCompactLayout(isCompactLayout: Boolean): EntrySpacing =
            if (isCompactLayout) COMPACT else NORMAL
    }
}
