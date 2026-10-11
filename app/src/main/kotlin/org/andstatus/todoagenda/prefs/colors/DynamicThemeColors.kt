package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.TONAL_DARK
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.TONAL_LIGHT

/** TODO: Implement ThemeColors for dynamic sources. */
fun dynamicThemeColors(
    context: Context,
    colorSettingsSource: ColorSettingsSource,
    darkLightTheme: DarkLightTheme,
): ThemeColors =
    ThemeColors(
        colorThemeType =
            when (colorSettingsSource) {
                ColorSettingsSource.SINGLE_MANUAL,
                ColorSettingsSource.TWO_MANUAL,
                -> {
                    error("Incorrect ColorSettingsSource: $colorSettingsSource")
                }

                ColorSettingsSource.MATERIAL_YOU_TONAL -> {
                    when (darkLightTheme) {
                        DarkLightTheme.AUTO -> {
                            if (InstanceSettings.isDarkThemeOn(context)) {
                                TONAL_DARK
                            } else {
                                TONAL_LIGHT
                            }
                        }

                        DarkLightTheme.DARK -> {
                            TONAL_DARK
                        }

                        DarkLightTheme.LIGHT -> {
                            TONAL_LIGHT
                        }
                    }
                }

                ColorSettingsSource.MATERIAL_YOU_SEMANTIC -> {
                    when (darkLightTheme) {
                        DarkLightTheme.AUTO -> {
                            if (InstanceSettings.isDarkThemeOn(context)) {
                                ColorThemeType.SEMANTIC_DARK
                            } else {
                                ColorThemeType.SEMANTIC_LIGHT
                            }
                        }

                        DarkLightTheme.DARK -> {
                            ColorThemeType.SEMANTIC_DARK
                        }

                        DarkLightTheme.LIGHT -> {
                            ColorThemeType.SEMANTIC_LIGHT
                        }
                    }
                }
            },
    )
