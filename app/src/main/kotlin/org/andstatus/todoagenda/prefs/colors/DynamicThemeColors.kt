package org.andstatus.todoagenda.prefs.colors

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorRes
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.SEMANTIC_DARK
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.SEMANTIC_LIGHT
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.TONAL_DARK
import org.andstatus.todoagenda.prefs.colors.ColorThemeType.TONAL_LIGHT

fun dynamicThemeColors(
    context: Context,
    colorSettingsSource: ColorSettingsSource,
    dark: Boolean,
): ColorTheme =
    when (colorSettingsSource) {
        ColorSettingsSource.SINGLE_MANUAL,
        ColorSettingsSource.TWO_MANUAL,
        -> {
            error("Incorrect ColorSettingsSource: $colorSettingsSource")
        }

        ColorSettingsSource.MATERIAL_YOU_TONAL -> {
            tonalPalette(dark).toThemeColors(
                context,
                if (dark) TONAL_DARK else TONAL_LIGHT,
            )
        }

        ColorSettingsSource.MATERIAL_YOU_SEMANTIC -> {
            semanticPalette(dark).toThemeColors(
                context,
                if (dark) SEMANTIC_DARK else SEMANTIC_LIGHT,
            )
        }
    }

/** The few Material You color roles that the widget is drawn with */
private class DynamicPalette(
    @param:ColorRes val surface: Int,
    @param:ColorRes val onSurface: Int,
    @param:ColorRes val onSurfaceVariant: Int,
    @param:ColorRes val primary: Int,
    @param:ColorRes val tertiary: Int,
) {
    /** The header and all entries share one surface color; only the Current time line has a color of its own */
    fun toThemeColors(
        context: Context,
        colorThemeType: ColorThemeType,
    ): ColorTheme {
        val backgroundColors =
            BackgroundColorPref.entries.associateWith { pref ->
                ShadingAndColor(
                    context.getColor(if (pref == BackgroundColorPref.CURRENT_TIME) primary else surface),
                )
            }
        val textColors =
            TextColorPref.entries.associateWith { pref ->
                ShadingAndColor(context.getColor(textColorRes(pref)))
            }
        return ColorTheme(
            colorThemeType = colorThemeType,
            backgroundColors = backgroundColors,
            textColorSource = TextColorSource.COLORS,
            textColors = textColors,
        )
    }

    /**
     * Entries that are entirely in the past get the reduced emphasis "On surface variant",
     * everything still to come gets "On surface", and today's day header gets the accent.
     */
    @ColorRes
    private fun textColorRes(pref: TextColorPref): Int =
        when (pref) {
            // The widget header is chrome rather than content, so it stays quieter than the entries
            TextColorPref.WIDGET_HEADER,
            TextColorPref.DAY_HEADER_PAST,
            TextColorPref.EVENT_PAST,
            -> onSurfaceVariant

            TextColorPref.DAY_HEADER_TODAY -> primary

            // An event happening right now is the most useful row in the widget, so it gets the
            // one accent not already in use: primary marks today and the Current time line
            TextColorPref.EVENT_ONGOING -> tertiary

            TextColorPref.EVENT_TODAY,
            TextColorPref.DAY_HEADER_FUTURE,
            TextColorPref.EVENT_FUTURE,
            -> onSurface
        }
}

/**
 * Tones of the wallpaper derived tonal palettes, available from Android 12 on.
 * The surface is tone 95 / 20 of the secondary accent: measured to be exactly what the
 * Google Calendar, Google Maps, Google Search and Breezy Weather widgets use, so that this widget
 * sits alongside them. Note that this is not one of the Material 3 surface roles, which are far less tinted.
 * Callers check [ColorSettingsSource.supportedByDevice] first.
 */
@SuppressLint("InlinedApi")
private fun tonalPalette(dark: Boolean): DynamicPalette =
    if (dark) {
        DynamicPalette(
            surface = android.R.color.system_accent2_800,
            onSurface = android.R.color.system_neutral1_100,
            onSurfaceVariant = android.R.color.system_neutral2_200,
            primary = android.R.color.system_accent1_200,
            tertiary = android.R.color.system_accent3_200,
        )
    } else {
        DynamicPalette(
            surface = android.R.color.system_accent2_50,
            onSurface = android.R.color.system_neutral1_900,
            onSurfaceVariant = android.R.color.system_neutral2_700,
            primary = android.R.color.system_accent1_600,
            tertiary = android.R.color.system_accent3_600,
        )
    }

/** Material 3 color roles, available from Android 14 on. Callers check [ColorSettingsSource.supportedByDevice] first. */
@SuppressLint("InlinedApi")
private fun semanticPalette(dark: Boolean): DynamicPalette =
    if (dark) {
        DynamicPalette(
            surface = android.R.color.system_surface_dark,
            onSurface = android.R.color.system_on_surface_dark,
            onSurfaceVariant = android.R.color.system_on_surface_variant_dark,
            primary = android.R.color.system_primary_dark,
            tertiary = android.R.color.system_tertiary_dark,
        )
    } else {
        DynamicPalette(
            surface = android.R.color.system_surface_light,
            onSurface = android.R.color.system_on_surface_light,
            onSurfaceVariant = android.R.color.system_on_surface_variant_light,
            primary = android.R.color.system_primary_light,
            tertiary = android.R.color.system_tertiary_light,
        )
    }
