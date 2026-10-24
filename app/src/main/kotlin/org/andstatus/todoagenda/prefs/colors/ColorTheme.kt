package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.WidgetEntry
import org.json.JSONException
import org.json.JSONObject

/** Colors and related settings for one theme, of one Widget.
 *
 * Colors are either defined by the user ([ManualColorTheme]) or are provided by the device
 * ([DynamicColorTheme]), so each variant stores to JSON only the settings it owns. Settings,
 * which are the same for all variants, are stored by [toJson] of this class.
 *
 * @author yvolk@yurivolkov.com
 */
sealed class ColorTheme(
    val colorThemeType: ColorThemeType,
) {
    /** This theme has no settings, e.g. because parsing of the stored settings failed */
    open val isEmpty: Boolean = false

    abstract val backgroundColors: Map<BackgroundColorPref, ShadingAndColor>
    abstract val textColorSource: TextColorSource
    abstract val textShadings: Map<TextColorPref, ShadingAndColor>
    abstract val textColors: Map<TextColorPref, ShadingAndColor>
    abstract val textShadow: TextShadow
    abstract val timeUntilBackgroundSource: TimeUntilBackgroundSource

    /** Stores this theme's settings to [json]. Each theme variant stores its own settings only */
    open fun toJson(json: JSONObject): JSONObject {
        try {
            json.put(PREF_COLOR_THEME_TYPE, colorThemeType.value)
            json.put(PREF_TEXT_SHADOW, textShadow.value)
            json.put(PREF_TIME_UNTIL_BACKGROUND_SOURCE, timeUntilBackgroundSource.value)
        } catch (e: JSONException) {
            throw RuntimeException("Saving color theme settings to JSON", e)
        }
        return json
    }

    fun getBackgroundColor(colorPref: BackgroundColorPref): Int = getBackground(colorPref).color

    fun getBackground(colorPref: BackgroundColorPref): ShadingAndColor =
        backgroundColors.getOrElse(colorPref) {
            ShadingAndColor(
                colorPref.defaultColor,
            )
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ColorTheme) return false
        return toJson(JSONObject()).toString() == other.toJson(JSONObject()).toString()
    }

    override fun hashCode(): Int = toJson(JSONObject()).toString().hashCode()

    fun getTextColor(
        context: Context,
        textColorPref: TextColorPref,
        @AttrRes colorAttrId: Int,
    ): Int {
        if (textColorSource == TextColorSource.COLORS) {
            return textColors.textColorStored(textColorPref).color
        } else if (textColorSource == TextColorSource.SHADING) {
            when (colorAttrId) {
                R.attr.header -> {
                    return textShadings.textShadingStored(textColorPref).shading.widgetHeaderColor
                }

                R.attr.dayHeaderTitle -> {
                    return textShadings.textShadingStored(textColorPref).shading.dayHeaderColor
                }

                R.attr.eventEntryTitle -> {
                    return textShadings.textShadingStored(textColorPref).shading.titleColor
                }
            }
        }
        return RemoteViewsUtil.getColorValue(getThemeContext(context, textColorPref), colorAttrId)
    }

    fun getShading(pref: TextColorPref): Shading =
        when (textColorSource) {
            TextColorSource.SHADING -> textShadings.textShadingStored(pref).shading
            TextColorSource.COLORS -> textColors.textColorStored(pref).shading
            TextColorSource.AUTO -> pref.getShadingForBackground(getBackground(pref.backgroundColorPref).shading)
        }

    fun timeUntilBackgroundSource(pref: TextColorPref): TimeUntilBackgroundSource =
        when (timeUntilBackgroundSource) {
            TimeUntilBackgroundSource.AUTO -> getShading(pref).timeUntilBackgroundSource
            else -> timeUntilBackgroundSource
        }

    fun getEntryBackgroundColor(entry: WidgetEntry): Int = getBackgroundColor(BackgroundColorPref.forTimeSection(entry.timeSection))

    fun getThemeContext(
        context: Context,
        pref: TextColorPref,
    ): ContextThemeWrapper = ContextThemeWrapper(context, getShading(pref).themeResId)

    companion object {
        const val TRANSPARENT_BLACK = Color.TRANSPARENT
        const val TRANSPARENT_WHITE = 0x00FFFFFF
        val EMPTY = ManualColorTheme(ColorThemeType.NONE, isEmpty = true)
        const val PREF_COLOR_THEME_TYPE = "colorThemeType"
        const val PREF_TEXT_COLOR_SOURCE = "textColorSource"
        const val PREF_TEXT_SHADOW = "textShadow"
        val PREF_TEXT_SHADOW_DEFAULT = TextShadow.NO_SHADOW.name
        const val PREF_TIME_UNTIL_BACKGROUND_SOURCE = "timeUntilBackgroundSource"

        fun fromJson(
            typeOverride: ColorThemeType?,
            json: JSONObject,
        ): ColorTheme {
            val colorThemeType =
                typeOverride ?: ColorThemeType.fromValue(json.optString(PREF_COLOR_THEME_TYPE))
            return if (colorThemeType.supportedByDevice) {
                if (colorThemeType.manuallyDefinedColors) {
                    ManualColorTheme.fromJson(colorThemeType, json)
                } else {
                    DynamicColorTheme.fromJson(colorThemeType, json)
                }
            } else {
                EMPTY
            }
        }

        fun fromApplicationPreferences(
            colorThemeType: ColorThemeType,
            context: Context,
        ): ColorTheme =
            if (colorThemeType.manuallyDefinedColors) {
                ManualColorTheme.fromApplicationPreferences(colorThemeType, context)
            } else {
                DynamicColorTheme.fromApplicationPreferences(colorThemeType, context)
            }

        fun parseTextShadow(json: JSONObject): TextShadow = TextShadow.fromValue(json.optString(PREF_TEXT_SHADOW, PREF_TEXT_SHADOW_DEFAULT))

        fun parseTimeUntilBackgroundSource(json: JSONObject): TimeUntilBackgroundSource =
            TimeUntilBackgroundSource.fromValue(
                json.optString(
                    PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                    TimeUntilBackgroundSource.defaultEntry.value,
                ),
            )

        fun Map<TextColorPref, ShadingAndColor>.textShadingStored(colorPref: TextColorPref): ShadingAndColor =
            this.getOrElse(colorPref) { ShadingAndColor(colorPref.defaultShading) }

        fun Map<TextColorPref, ShadingAndColor>.textColorStored(colorPref: TextColorPref): ShadingAndColor =
            this.getOrElse(colorPref) { ShadingAndColor(colorPref.defaultColor) }
    }
}
