package org.andstatus.todoagenda.prefs

/**
 * Horizontal scale of widget texts (see [android.widget.TextView.setTextScaleX]).
 *
 * Values below 1.0 make the text narrower ("condensed"), values above 1.0 widen it.
 */
enum class FontWidth(
    val preferenceValue: String,
    val scaleX: Float,
) {
    CONDENSED("0.8", 0.8f),
    NARROW("0.9", 0.9f),
    NORMAL("1.0", 1.0f),
    WIDE("1.1", 1.1f),
    EXTRA_WIDE("1.2", 1.2f),
    ;

    companion object {
        fun fromPreferenceValue(preferenceValue: String?): FontWidth =
            entries.firstOrNull { it.preferenceValue == preferenceValue } ?: NORMAL
    }
}
