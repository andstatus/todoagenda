package org.andstatus.todoagenda.prefs.colors

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.widget.WidgetEntry
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Colors part of settings for one theme, of one Widget
 * @author yvolk@yurivolkov.com
 */
class ThemeColors(
    val contextIn: Context?,
    val colorThemeType: ColorThemeType,
) {
    val context: Context get() = contextIn ?: throw IllegalStateException("Context is null")
    val backgroundColors: ConcurrentMap<BackgroundColorPref?, ShadingAndColor> = ConcurrentHashMap()
    var textColorSource: TextColorSource = TextColorSource.defaultEntry
    val textShadings: ConcurrentMap<TextColorPref?, ShadingAndColor> = ConcurrentHashMap()
    val textColors: ConcurrentMap<TextColorPref?, ShadingAndColor> = ConcurrentHashMap()
    var useDynamicColors: Boolean = false

    fun copy(
        context: Context,
        colorThemeType: ColorThemeType,
    ): ThemeColors {
        val themeColors = ThemeColors(context, colorThemeType)
        return if (isEmpty) themeColors else themeColors.setFromJson(toJson(JSONObject()))
    }

    private fun setFromJson(json: JSONObject): ThemeColors {
        try {
            useDynamicColors = json.optBoolean(PREF_USE_DYNAMIC_COLORS, false)
            for (pref in BackgroundColorPref.entries) {
                val color =
                    if (json.has(pref.colorPreferenceName)) {
                        json.getInt(pref.colorPreferenceName)
                    } else {
                        pref.defaultColor
                    }
                backgroundColors[pref] = ShadingAndColor(color)
            }
            textColorSource =
                if (json.has(PREF_TEXT_COLOR_SOURCE)) {
                    TextColorSource.fromValue(json.getString(PREF_TEXT_COLOR_SOURCE))
                } else {
                    // This was default before v.4.4
                    TextColorSource.SHADING
                }
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
        } catch (e: JSONException) {
            Log.w(TAG, "setFromJson failed\n$json")
            return this
        }
        return this
    }

    fun setFromApplicationPreferences(): ThemeColors {
        useDynamicColors = ApplicationPreferences.getBoolean(context, PREF_USE_DYNAMIC_COLORS, false)
        for (pref in BackgroundColorPref.entries) {
            setBackgroundColor(pref, ApplicationPreferences.getBackgroundColor(pref, context))
        }
        textColorSource = ApplicationPreferences.getTextColorSource(context)
        for (pref in TextColorPref.entries) {
            val oldValue = getTextShadingStored(pref)
            val themeName = ApplicationPreferences.getString(context, pref.shadingPreferenceName, "")
            val shading: Shading = Shading.fromThemeName(themeName, oldValue.shading)
            textShadings[pref] = ShadingAndColor(shading)
        }
        for (pref in TextColorPref.entries) {
            val oldValue = getTextColorStored(pref)
            val color = ApplicationPreferences.getInt(context, pref.colorPreferenceName, oldValue.color)
            textColors[pref] = ShadingAndColor(color)
        }
        return this
    }

    /**
     * Note that this stores what the user picked, never what Material You currently derives:
     * the manually set colors have to survive switching Material You on and back off again.
     */
    fun toJson(json: JSONObject): JSONObject {
        try {
            json.put(PREF_USE_DYNAMIC_COLORS, useDynamicColors)
            for (pref in BackgroundColorPref.entries) {
                json.put(pref.colorPreferenceName, getBackground(pref).color)
            }
            json.put(PREF_TEXT_COLOR_SOURCE, textColorSource.value)
            for (pref in TextColorPref.entries) {
                json.put(pref.shadingPreferenceName, getTextShadingStored(pref).shading.themeName)
                json.put(pref.colorPreferenceName, getTextColorStored(pref).color)
            }
        } catch (e: JSONException) {
            throw RuntimeException("Saving settings to JSON", e)
        }
        return json
    }

    private fun setBackgroundColor(
        pref: BackgroundColorPref,
        backgroundColor: Int?,
    ) {
        val color = backgroundColor ?: pref.defaultColor
        backgroundColors[pref] = ShadingAndColor(color)
    }

    /**
     * The wallpaper derived tonal palettes exist from Android 12 on. This widget reads those
     * directly, rather than the Material 3 color roles added in Android 14, because that is what
     * the Google Calendar and Breezy Weather widgets do and the point is to sit alongside them.
     */
    private val useMaterialYou: Boolean
        get() = useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    /**
     * The color of the widget as a whole. Transparent unless Material You is on: without it
     * each entry paints its own background and the widget has no backdrop of its own.
     */
    val widgetBackgroundColor: Int
        get() = if (useMaterialYou) getMaterialYouSurfaceColor() else Color.TRANSPARENT

    /**
     * The color to actually draw with. When Material You is on this overrides, but never
     * overwrites, the stored value returned by [getBackground] - see [toJson].
     */
    fun getBackgroundColor(colorPref: BackgroundColorPref?): Int {
        if (colorPref != null && useMaterialYou) {
            return getMaterialYouBackgroundColor(colorPref)
        }
        return getBackground(colorPref).color
    }

    /** The color the user picked, which is what gets persisted */
    fun getBackground(colorPref: BackgroundColorPref?): ShadingAndColor =
        backgroundColors.computeIfAbsent(colorPref) { pref: BackgroundColorPref? ->
            ShadingAndColor(
                pref!!.defaultColor,
            )
        }

    val isEmpty: Boolean
        get() = contextIn == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val settings = other as ThemeColors
        return toJson(JSONObject()).toString() == settings.toJson(JSONObject()).toString()
    }

    override fun hashCode(): Int = toJson(JSONObject()).toString().hashCode()

    /**
     * The color to actually draw with. When Material You is on this overrides, but never
     * overwrites, [textColorSource] and the stored values - see [toJson].
     */
    fun getTextColor(
        textColorPref: TextColorPref,
        @AttrRes colorAttrId: Int,
    ): Int {
        if (useMaterialYou) {
            return getMaterialYouTextColor(textColorPref)
        }
        if (textColorSource == TextColorSource.COLORS) {
            return getTextColorStored(textColorPref).color
        } else if (textColorSource == TextColorSource.SHADING) {
            when (colorAttrId) {
                R.attr.header -> {
                    return getTextShadingStored(textColorPref).shading.widgetHeaderColor
                }

                R.attr.dayHeaderTitle -> {
                    return getTextShadingStored(textColorPref).shading.dayHeaderColor
                }

                R.attr.eventEntryTitle -> {
                    return getTextShadingStored(textColorPref).shading.titleColor
                }
            }
        }
        return RemoteViewsUtil.getColorValue(getThemeContext(textColorPref), colorAttrId)
    }

    /** The shading the user picked, which is what gets persisted */
    fun getTextShadingStored(colorPref: TextColorPref?): ShadingAndColor =
        textShadings.computeIfAbsent(colorPref) { pref: TextColorPref? ->
            ShadingAndColor(
                pref!!.defaultShading,
            )
        }

    /** The color the user picked, which is what gets persisted */
    fun getTextColorStored(colorPref: TextColorPref?): ShadingAndColor =
        textColors.computeIfAbsent(colorPref) { pref: TextColorPref? ->
            ShadingAndColor(
                pref!!.defaultColor,
            )
        }

    /**
     * Shading selects the icon and "Time until" tag variants, so with Material You on it follows
     * from the luminance of the role color rather than from the stored settings.
     */
    fun getShading(pref: TextColorPref): Shading {
        if (useMaterialYou) {
            return ShadingAndColor(getMaterialYouTextColor(pref)).shading
        }
        return when (textColorSource) {
            TextColorSource.SHADING -> getTextShadingStored(pref).shading
            TextColorSource.COLORS -> getTextColorStored(pref).shading
            TextColorSource.AUTO -> pref.getShadingForBackground(getBackground(pref.backgroundColorPref).shading)
        }
    }

    fun getEntryBackgroundColor(entry: WidgetEntry): Int = getBackgroundColor(BackgroundColorPref.forTimeSection(entry.timeSection))

    fun getThemeContext(pref: TextColorPref): ContextThemeWrapper = ContextThemeWrapper(context, getShading(pref).themeResId)

    /**
     * Which of the two Material You palettes to read. This follows the system theme rather than
     * [colorThemeType], because [InstanceSettings.colors] already picks the Dark instance only
     * while the system theme is dark, so the two always agree.
     */
    private fun isDarkMaterialYou(): Boolean = InstanceSettings.isDarkThemeOn(context)

    /**
     * The single color the whole widget is drawn on. Tone 95 and 20 of the secondary accent:
     * measured to be exactly what the Google Calendar, Google Maps, Google Search and
     * Breezy Weather widgets use, so that this widget sits alongside them unchanged.
     * Note that this is not one of the Material 3 surface roles, which are far less tinted.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun getMaterialYouSurfaceColor(): Int =
        context.getColor(
            if (isDarkMaterialYou()) android.R.color.system_accent2_800 else android.R.color.system_accent2_50,
        )

    /**
     * Only the Current time line has a color of its own; everything else is transparent,
     * so that the widget reads as one Material You surface.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun getMaterialYouBackgroundColor(pref: BackgroundColorPref): Int =
        if (pref == BackgroundColorPref.CURRENT_TIME) {
            context.getColor(materialYouAccent())
        } else {
            Color.TRANSPARENT
        }

    /** Material You "Primary": tone 40 of the primary accent, or tone 80 against a dark surface */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun materialYouAccent(): Int =
        if (isDarkMaterialYou()) android.R.color.system_accent1_200 else android.R.color.system_accent1_600

    /**
     * Entries that are entirely in the past get the reduced emphasis "On surface variant" tone,
     * everything still to come gets "On surface", and today's day header gets the accent.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun getMaterialYouTextColor(pref: TextColorPref): Int {
        val dark = isDarkMaterialYou()
        val resId =
            when (pref) {
                // The widget header is chrome rather than content, so it stays quieter than the entries
                TextColorPref.WIDGET_HEADER,
                TextColorPref.DAY_HEADER_PAST,
                TextColorPref.EVENT_PAST,
                -> if (dark) android.R.color.system_neutral2_200 else android.R.color.system_neutral2_700

                TextColorPref.DAY_HEADER_TODAY -> materialYouAccent()

                // An event happening right now is the most useful row in the widget, so it gets the
                // one accent not already in use: the primary accent marks today and the Current
                // time line, and the secondary accent has too little chroma to tell from the text.
                TextColorPref.EVENT_ONGOING ->
                    if (dark) android.R.color.system_accent3_200 else android.R.color.system_accent3_600

                TextColorPref.EVENT_TODAY,
                TextColorPref.DAY_HEADER_FUTURE,
                TextColorPref.EVENT_FUTURE,
                -> if (dark) android.R.color.system_neutral1_100 else android.R.color.system_neutral1_900
            }
        return context.getColor(resId)
    }

    companion object {
        private val TAG = ThemeColors::class.java.simpleName
        const val TRANSPARENT_BLACK = Color.TRANSPARENT
        const val TRANSPARENT_WHITE = 0x00FFFFFF
        val EMPTY = ThemeColors(null, ColorThemeType.SINGLE)
        const val PREF_TEXT_COLOR_SOURCE = "textColorSource"
        const val PREF_USE_DYNAMIC_COLORS = "useDynamicColors"

        fun fromJson(
            context: Context?,
            colorThemeType: ColorThemeType,
            json: JSONObject,
        ): ThemeColors = ThemeColors(context, colorThemeType).setFromJson(json)
    }
}
