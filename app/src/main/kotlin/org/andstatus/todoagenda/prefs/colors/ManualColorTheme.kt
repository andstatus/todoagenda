package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.graphics.Color
import android.util.Log
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.json.JSONException
import org.json.JSONObject

/** Colors, which the user defined for the theme.
 * All of them are the user's choices, so all of them are stored to JSON.
 */
class ManualColorTheme(
    colorThemeType: ColorThemeType,
    override val backgroundColors: Map<BackgroundColorPref, ShadingAndColor> = emptyMap(),
    override val textColorSource: TextColorSource = TextColorSource.defaultEntry,
    override val textShadings: Map<TextColorPref, ShadingAndColor> = emptyMap(),
    override val textColors: Map<TextColorPref, ShadingAndColor> = emptyMap(),
    override val textShadow: TextShadow = TextShadow.NO_SHADOW,
    override val timeUntilBackgroundSource: TimeUntilBackgroundSource = TimeUntilBackgroundSource.defaultEntry,
    override val isEmpty: Boolean = false,
    override val widgetBackgroundColor: Int = Color.TRANSPARENT,
) : ColorTheme(colorThemeType) {
    /** Same settings, but for another theme, e.g. the Light theme settings are used as a
     * starting point for the Dark theme until the user customizes it */
    fun copy(colorThemeType: ColorThemeType = this.colorThemeType): ManualColorTheme =
        ManualColorTheme(
            colorThemeType = colorThemeType,
            backgroundColors = backgroundColors,
            textColorSource = textColorSource,
            textShadings = textShadings,
            textColors = textColors,
            textShadow = textShadow,
            timeUntilBackgroundSource = timeUntilBackgroundSource,
            isEmpty = isEmpty,
            widgetBackgroundColor = widgetBackgroundColor,
        )

    override fun toJson(json: JSONObject): JSONObject {
        super.toJson(json)
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
            throw RuntimeException("Saving manual color theme to JSON", e)
        }
        return json
    }

    companion object {
        private val TAG = ManualColorTheme::class.java.simpleName

        fun fromJson(
            colorThemeType: ColorThemeType,
            json: JSONObject,
        ): ManualColorTheme =
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
                ManualColorTheme(
                    colorThemeType = colorThemeType,
                    backgroundColors = backgroundColors,
                    textColorSource = textColorSource,
                    textShadings = textShadings,
                    textColors = textColors,
                    textShadow = parseTextShadow(json),
                    timeUntilBackgroundSource = parseTimeUntilBackgroundSource(json),
                    isEmpty = false,
                )
            } catch (_: JSONException) {
                Log.w(TAG, "fromJson failed, $colorThemeType\n$json")
                EMPTY
            }

        fun fromApplicationPreferences(
            colorThemeType: ColorThemeType,
            context: Context,
        ): ManualColorTheme {
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
            return ManualColorTheme(
                colorThemeType = colorThemeType,
                backgroundColors = backgroundColors,
                textColorSource = textColorSource,
                textShadings = textShadings,
                textColors = textColors,
                textShadow = ApplicationPreferences.getTextShadow(context),
                timeUntilBackgroundSource =
                    ApplicationPreferences.getTimeUntilBackgroundSource(
                        context,
                    ),
                isEmpty = false,
            )
        }
    }
}
