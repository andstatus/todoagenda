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

    fun copy(
        context: Context,
        colorThemeType: ColorThemeType,
    ): ThemeColors {
        val themeColors = ThemeColors(context, colorThemeType)
        return if (isEmpty) themeColors else themeColors.setFromJson(toJson(JSONObject()))
    }

    private fun setFromJson(json: JSONObject): ThemeColors {
        try {
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

    fun toJson(json: JSONObject): JSONObject {
        try {
            for (pref in BackgroundColorPref.entries) {
                json.put(pref.colorPreferenceName, getBackgroundColor(pref))
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

    fun getBackgroundColor(colorPref: BackgroundColorPref?): Int = getBackground(colorPref).color

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

    fun getTextColor(
        textColorPref: TextColorPref,
        @AttrRes colorAttrId: Int,
    ): Int {
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

    fun getTextShadingStored(colorPref: TextColorPref?): ShadingAndColor =
        textShadings.computeIfAbsent(colorPref) { pref: TextColorPref? ->
            ShadingAndColor(
                pref!!.defaultShading,
            )
        }

    fun getTextColorStored(colorPref: TextColorPref?): ShadingAndColor =
        textColors.computeIfAbsent(colorPref) { pref: TextColorPref? ->
            ShadingAndColor(
                pref!!.defaultColor,
            )
        }

    fun getShading(pref: TextColorPref): Shading =
        when (textColorSource) {
            TextColorSource.SHADING -> getTextShadingStored(pref).shading
            TextColorSource.COLORS -> getTextColorStored(pref).shading
            TextColorSource.AUTO -> pref.getShadingForBackground(getBackground(pref.backgroundColorPref).shading)
        }

    fun getEntryBackgroundColor(entry: WidgetEntry): Int = getBackgroundColor(BackgroundColorPref.forTimeSection(entry.timeSection))

    fun getThemeContext(pref: TextColorPref): ContextThemeWrapper = ContextThemeWrapper(context, getShading(pref).themeResId)

    companion object {
        private val TAG = ThemeColors::class.java.simpleName
        const val TRANSPARENT_BLACK = Color.TRANSPARENT
        const val TRANSPARENT_WHITE = 0x00FFFFFF
        val EMPTY = ThemeColors(null, ColorThemeType.SINGLE)
        const val PREF_TEXT_COLOR_SOURCE = "textColorSource"

        fun fromJson(
            context: Context?,
            colorThemeType: ColorThemeType,
            json: JSONObject,
        ): ThemeColors = ThemeColors(context, colorThemeType).setFromJson(json)
    }
}
