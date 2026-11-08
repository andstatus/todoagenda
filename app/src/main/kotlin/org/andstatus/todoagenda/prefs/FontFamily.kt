package org.andstatus.todoagenda.prefs

/**
 * Font family, used for all texts of a widget.
 *
 * Only font families, available on the device, can be used, because widget views are inflated
 * and drawn by the launcher process. See [org.andstatus.todoagenda.util.RemoteViewsUtil.setText]
 * for how the family is applied to a [android.widget.RemoteViews] text.
 */
enum class FontFamily(
    val preferenceValue: String,
    /** Family name, understood by [android.graphics.Typeface.create].
     * Null means the typeface, defined by the layout (the device default one) */
    val family: String?,
) {
    DEFAULT("default", null),
    SANS_SERIF("sans_serif", "sans-serif"),
    SANS_SERIF_CONDENSED("sans_serif_condensed", "sans-serif-condensed"),
    SANS_SERIF_LIGHT("sans_serif_light", "sans-serif-light"),
    SANS_SERIF_MEDIUM("sans_serif_medium", "sans-serif-medium"),
    SERIF("serif", "serif"),
    MONOSPACE("monospace", "monospace"),
    CASUAL("casual", "casual"),
    CURSIVE("cursive", "cursive"),
    ;

    val isDefault: Boolean
        get() = family == null

    companion object {
        fun fromPreferenceValue(preferenceValue: String?): FontFamily =
            entries.firstOrNull { it.preferenceValue == preferenceValue } ?: DEFAULT
    }
}
