package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.WidgetEntry
import org.json.JSONException
import org.json.JSONObject

/** Colors part of settings for one theme, of one Widget
 * @author yvolk@yurivolkov.com */
class ThemeColors(
    val colorThemeType: ColorThemeType,
    val backgroundColors: Map<BackgroundColorPref, ShadingAndColor> = emptyMap(),
    val textColorSource: TextColorSource = TextColorSource.defaultEntry,
    val textShadings: Map<TextColorPref, ShadingAndColor> = emptyMap(),
    val textColors: Map<TextColorPref, ShadingAndColor> = emptyMap(),
    val isEmpty: Boolean = false,
) {
    fun jsonCopy(colorThemeType: ColorThemeType): ThemeColors = fromJson(colorThemeType, toJson(JSONObject()))

    fun toJson(json: JSONObject): JSONObject {
        try {
            for (pref in BackgroundColorPref.entries) {
                json.put(pref.colorPreferenceName, getBackgroundColor(pref))
            }
            json.put(PREF_TEXT_COLOR_SOURCE, textColorSource.value)
            for (pref in TextColorPref.entries) {
                json.put(
                    pref.shadingPreferenceName,
                    textShadings.textShadingStored(pref).shading.themeName,
                )
                json.put(pref.colorPreferenceName, textColors.textColorStored(pref).color)
            }
        } catch (e: JSONException) {
            throw RuntimeException("Saving settings to JSON", e)
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
        if (other == null || other !is ThemeColors) return false
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

    fun getEntryBackgroundColor(entry: WidgetEntry): Int = getBackgroundColor(BackgroundColorPref.forTimeSection(entry.timeSection))

    fun getThemeContext(
        context: Context,
        pref: TextColorPref,
    ): ContextThemeWrapper = ContextThemeWrapper(context, getShading(pref).themeResId)

    companion object {
        private val TAG = ThemeColors::class.java.simpleName
        const val TRANSPARENT_BLACK = Color.TRANSPARENT
        const val TRANSPARENT_WHITE = 0x00FFFFFF
        val EMPTY = ThemeColors(ColorThemeType.SINGLE, isEmpty = true)
        const val PREF_TEXT_COLOR_SOURCE = "textColorSource"

        fun fromJson(
            colorThemeType: ColorThemeType,
            json: JSONObject,
        ): ThemeColors =
            try {
                val backgroundColors: MutableMap<BackgroundColorPref, ShadingAndColor> =
                    mutableMapOf()
                for (pref in BackgroundColorPref.entries) {
                    val color =
                        if (json.has(pref.colorPreferenceName)) {
                            json.getInt(pref.colorPreferenceName)
                        } else {
                            pref.defaultColor
                        }
                    backgroundColors[pref] = ShadingAndColor(color)
                }
                val textColorSource: TextColorSource =
                    if (json.has(PREF_TEXT_COLOR_SOURCE)) {
                        TextColorSource.fromValue(json.getString(PREF_TEXT_COLOR_SOURCE))
                    } else {
                        // This was default before v.4.4
                        TextColorSource.SHADING
                    }
                val textShadings: MutableMap<TextColorPref, ShadingAndColor> = mutableMapOf()
                val textColors: MutableMap<TextColorPref, ShadingAndColor> = mutableMapOf()
                for (pref in TextColorPref.entries) {
                    val shading =
                        if (json.has(pref.shadingPreferenceName)) {
                            Shading.fromThemeName(
                                json.getString(pref.shadingPreferenceName),
                                pref.defaultShading,
                            )
                        } else {
                            pref.defaultShading
                        }
                    textShadings[pref] = ShadingAndColor(shading)
                    val color =
                        if (json.has(pref.colorPreferenceName)) json.getInt(pref.colorPreferenceName) else pref.defaultColor
                    textColors[pref] = ShadingAndColor(color)
                }
                ThemeColors(
                    colorThemeType = colorThemeType,
                    backgroundColors = backgroundColors,
                    textColorSource = textColorSource,
                    textShadings = textShadings,
                    textColors = textColors,
                    isEmpty = false,
                )
            } catch (_: JSONException) {
                Log.w(TAG, "setFromJson failed\n$json")
                EMPTY
            }

        fun fromApplicationPreferences(
            colorThemeType: ColorThemeType,
            context: Context,
        ): ThemeColors {
            val backgroundColors: MutableMap<BackgroundColorPref, ShadingAndColor> = mutableMapOf()
            for (pref in BackgroundColorPref.entries) {
                val color = ApplicationPreferences.getBackgroundColor(pref, context)
                backgroundColors[pref] = ShadingAndColor(color)
            }
            val textColorSource = ApplicationPreferences.getTextColorSource(context)
            val textShadings: MutableMap<TextColorPref, ShadingAndColor> = mutableMapOf()
            val textColors: MutableMap<TextColorPref, ShadingAndColor> = mutableMapOf()
            for (pref in TextColorPref.entries) {
                val oldValue = textShadings.textShadingStored(pref)
                val themeName =
                    ApplicationPreferences.getString(context, pref.shadingPreferenceName, "")
                val shading: Shading = Shading.fromThemeName(themeName, oldValue.shading)
                textShadings[pref] = ShadingAndColor(shading)
            }
            for (pref in TextColorPref.entries) {
                val oldValue = textColors.textColorStored(pref)
                val color =
                    ApplicationPreferences.getInt(context, pref.colorPreferenceName, oldValue.color)
                textColors[pref] = ShadingAndColor(color)
            }
            return ThemeColors(
                colorThemeType = colorThemeType,
                backgroundColors = backgroundColors,
                textColorSource = textColorSource,
                textShadings = textShadings,
                textColors = textColors,
                isEmpty = false,
            )
        }

        fun Map<TextColorPref, ShadingAndColor>.textShadingStored(colorPref: TextColorPref): ShadingAndColor =
            this.getOrElse(colorPref) { ShadingAndColor(colorPref.defaultShading) }

        fun Map<TextColorPref, ShadingAndColor>.textColorStored(colorPref: TextColorPref): ShadingAndColor =
            this.getOrElse(colorPref) { ShadingAndColor(colorPref.defaultColor) }
    }
}
