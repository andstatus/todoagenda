package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_DARK_THEME
import org.json.JSONArray
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
            return manualColorTheme(ColorThemeType.LIGHT)
        }
        val isDarkThemeOn = InstanceSettings.isDarkThemeOn(context)
        val colorThemeType = colorSettingsSource.themeType(darkLightTheme, isDarkThemeOn)
        return if (colorSettingsSource.manuallyDefinedColors) {
            manualColorTheme(colorThemeType)
        } else {
            // Only colors are provided by the device, settings are stored in this theme
            dynamicColorTheme(
                context,
                colorSettingsSource,
                darkLightTheme.isDark(isDarkThemeOn),
                themesMap[colorThemeType],
            )
        }
    }

    fun manualColorTheme(colorThemeType: ColorThemeType): ColorTheme {
        themesMap[colorThemeType]?.let { return it }
        val otherTheme =
            when (colorThemeType) {
                ColorThemeType.DARK -> themesMap[ColorThemeType.LIGHT]
                ColorThemeType.LIGHT -> themesMap[ColorThemeType.DARK]
                else -> null
            }
        // Settings of the other theme are a starting point for the not yet customized one
        return (otherTheme as? ManualColorTheme)?.copy(colorThemeType = colorThemeType)
            ?: if (colorThemeType.manuallyDefinedColors) {
                ManualColorTheme(colorThemeType)
            } else {
                error("Unexpected colorThemeType $colorThemeType")
            }
    }

    fun toJson(json: JSONObject): JSONObject {
        try {
            val themesArray = JSONArray()
            themesMap.values
                .filter { it.colorThemeType.supportedByDevice }
                .forEach { theme ->
                    themesArray.put(theme.toJson(JSONObject()))
                }
            json.put(PREF_COLOR_THEMES, themesArray)
        } catch (e: JSONException) {
            throw RuntimeException("Saving ColorThemes to JSON", e)
        }
        return json
    }

    companion object {
        const val PREF_COLOR_THEMES = "colorThemes"

        fun fromJson(rootJson: JSONObject): ColorThemes {
            val themesArray: JSONArray =
                rootJson.optJSONArray(PREF_COLOR_THEMES)
                    ?: return fromJsonV4p14(rootJson)

            return (0 until themesArray.length())
                .mapNotNull { index ->
                    ColorTheme
                        .fromJson(null, themesArray.getJSONObject(index))
                        .takeIf { it.colorThemeType.supportedByDevice }
                }.associateBy { it.colorThemeType }
                .let {
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
                val legacyDarkThemeJson =
                    JSONObject(json.getJSONObject(PREF_DARK_THEME).toString()).apply {
                        // Add settings that were stored at the Widget's level, not inside a theme
                        listOf(
                            ColorTheme.PREF_TEXT_SHADOW,
                            ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                        ).filter { !has(it) && json.has(it) }
                            .forEach { put(it, json.get(it)) }
                    }
                ColorTheme
                    .fromJson(ColorThemeType.DARK, legacyDarkThemeJson)
                    .also {
                        colorThemeMap[it.colorThemeType] = it
                    }
            }
            return ColorThemes(colorThemeMap.toMap())
        }
    }
}
