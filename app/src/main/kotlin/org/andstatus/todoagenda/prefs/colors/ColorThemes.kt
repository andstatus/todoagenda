package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_DARK_THEME
import org.json.JSONException
import org.json.JSONObject

class ColorThemes(
    private val themesMap: Map<ColorThemeType, ColorTheme> = emptyMap(),
) {
    fun hasTheme(colorThemeType: ColorThemeType): Boolean = themesMap.containsKey(colorThemeType)

    operator fun plus(colorTheme: ColorTheme?): ColorThemes =
        colorTheme?.let {
            ColorThemes(themesMap + (it.colorThemeType to it))
        } ?: this

    fun theme(
        context: Context?,
        colorSettingsSource: ColorSettingsSource,
        darkLightTheme: DarkLightTheme,
    ): ColorTheme {
        if (!colorSettingsSource.supportedByDevice) {
            error("$colorSettingsSource is not supported by the device")
        }
        if (context == null) {
            return theme(ColorThemeType.LIGHT)
        }
        val dark =
            when (darkLightTheme) {
                DarkLightTheme.AUTO -> InstanceSettings.isDarkThemeOn(context)
                DarkLightTheme.DARK -> true
                DarkLightTheme.LIGHT -> false
            }
        return when (colorSettingsSource) {
            ColorSettingsSource.SINGLE_MANUAL -> theme(ColorThemeType.LIGHT)
            ColorSettingsSource.TWO_MANUAL -> theme(if (dark) ColorThemeType.DARK else ColorThemeType.LIGHT)
            else -> dynamicThemeColors(context, colorSettingsSource, dark)
        }
    }

    fun theme(colorThemeType: ColorThemeType): ColorTheme =
        themesMap.getOrElse(colorThemeType) {
            when (colorThemeType) {
                ColorThemeType.DARK -> themesMap.getOrElse(ColorThemeType.LIGHT) { null }
                ColorThemeType.LIGHT -> themesMap.getOrElse(ColorThemeType.DARK) { null }
                else -> null
            } ?: ColorTheme(colorThemeType)
        }

    fun toJson(json: JSONObject): JSONObject {
        try {
            val nestedJson = JSONObject()
            themesMap.values
                .filter { it.colorThemeType.supportedByDevice }
                .forEach { theme ->
                    nestedJson.put(theme.colorThemeType.value, theme.toJson(JSONObject()))
                }
            json.put(PREF_COLOR_THEMES, nestedJson)
        } catch (e: JSONException) {
            throw RuntimeException("Saving ColorThemes to JSON", e)
        }
        return json
    }

    companion object {
        const val PREF_COLOR_THEMES = "colorThemes"

        fun fromJson(rootJson: JSONObject): ColorThemes {
            val themesJson: JSONObject =
                rootJson.optJSONObject(PREF_COLOR_THEMES)
                    ?: return fromJsonV4p14(rootJson)

            return ColorThemeType.entries
                .filter { it.supportedByDevice }
                .fold(emptyMap<ColorThemeType, ColorTheme>()) { themesMap, themeType ->
                    themesJson.optJSONObject(themeType.value)?.let { themeJson ->
                        ColorTheme
                            .fromJson(themeType, themeJson)
                            .takeIf { !it.isEmpty }
                            ?.let {
                                themesMap + (themeType to it)
                            }
                    } ?: themesMap
                }.let {
                    ColorThemes(it)
                }
        }

        /** For app versions up to 4.14 */
        private fun fromJsonV4p14(json: JSONObject): ColorThemes {
            val colorThemeMap: MutableMap<ColorThemeType, ColorTheme> = mutableMapOf()
            ColorTheme
                .fromJson(ColorThemeType.LIGHT, json)
                .also {
                    colorThemeMap[it.colorThemeType] = it
                }
            if (ColorThemeType.DARK.supportedByDevice && json.has(PREF_DARK_THEME)) {
                ColorTheme
                    .fromJson(ColorThemeType.DARK, json.getJSONObject(PREF_DARK_THEME))
                    .also {
                        colorThemeMap[it.colorThemeType] = it
                    }
            }
            return ColorThemes(colorThemeMap.toMap())
        }
    }
}
