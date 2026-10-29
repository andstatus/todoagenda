package org.andstatus.todoagenda.prefs.colors

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.colors.ColorTheme.Companion.PREF_COLOR_THEME_TYPE
import org.json.JSONArray
import org.json.JSONObject
import org.junit.runner.RunWith

/**
 * Verifies that each [ColorTheme] variant stores to JSON only the settings it owns.
 */
@Suppress("DEPRECATION")
@RunWith(KotestTestRunner::class)
class ColorThemeTest : AnnotationSpec() {
    val backgroundColor = 0xFF112233.toInt()
    val textColor = 0xFF445566.toInt()

    fun manualTheme(): ManualColorTheme =
        ManualColorTheme(
            colorThemeType = ColorThemeType.LIGHT,
            backgroundColors =
                BackgroundColorPref.entries.associateWith {
                    ShadingAndColor(
                        backgroundColor,
                    )
                },
            textColorSource = TextColorSource.COLORS,
            textShadings = TextColorPref.entries.associateWith { ShadingAndColor(it.defaultShading) },
            textColors = TextColorPref.entries.associateWith { ShadingAndColor(textColor) },
            textShadow = TextShadow.DARK_SHADOW,
            timeUntilBackgroundSource = TimeUntilBackgroundSource.YELLOW,
        )

    fun dynamicTheme(): DynamicColorTheme =
        DynamicColorTheme(
            colorThemeType = ColorThemeType.TONAL_DARK,
            backgroundColors =
                BackgroundColorPref.entries.associateWith {
                    ShadingAndColor(
                        backgroundColor,
                    )
                },
            textColors = TextColorPref.entries.associateWith { ShadingAndColor(textColor) },
            textShadow = TextShadow.LIGHT_SHADOW,
            timeUntilBackgroundSource = TimeUntilBackgroundSource.WHITE,
        )

    @Test
    fun manualThemeStoresAllItsSettings() {
        val theme = manualTheme()
        val json = theme.toJson(JSONObject())
        BackgroundColorPref.entries.forEach { pref ->
            json.has(pref.colorPreferenceName) shouldBe true
            json.getInt(pref.colorPreferenceName) shouldBe backgroundColor
        }
        json.getString(ColorTheme.PREF_TEXT_COLOR_SOURCE) shouldBe TextColorSource.COLORS.value
        TextColorPref.entries.forEach { pref ->
            json.getString(pref.shadingPreferenceName) shouldBe pref.defaultShading.themeName
            json.getInt(pref.colorPreferenceName) shouldBe textColor
        }
        json.getString(ColorTheme.PREF_TEXT_SHADOW) shouldBe TextShadow.DARK_SHADOW.value
        json.getString(ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE) shouldBe
            TimeUntilBackgroundSource.YELLOW.value
    }

    @Test
    fun manualThemeRoundTripsThroughJSON() {
        val theme = manualTheme()
        val restored = ColorTheme.fromJson(theme.colorThemeType, theme.toJson(JSONObject()))
        restored.shouldBeInstanceOf<ManualColorTheme>()
        restored shouldBe theme
        restored.toJson(JSONObject()).toString() shouldBe theme.toJson(JSONObject()).toString()
        restored.textShadow shouldBe TextShadow.DARK_SHADOW
        restored.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.YELLOW
    }

    @Test
    fun dynamicThemeStoresOnlyItsTypeAndSettings() {
        val theme = dynamicTheme()
        val json = theme.toJson(JSONObject())
        json.keys().asSequence().toSet() shouldBe
            setOf(
                ColorTheme.PREF_TEXT_SHADOW,
                ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                PREF_COLOR_THEME_TYPE,
            )
        json.getString(PREF_COLOR_THEME_TYPE) shouldBe ColorThemeType.TONAL_DARK.value
        json.getString(ColorTheme.PREF_TEXT_SHADOW) shouldBe TextShadow.LIGHT_SHADOW.value
        json.getString(ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE) shouldBe
            TimeUntilBackgroundSource.WHITE.value
        withClue("Colors are still used for drawing, even though they aren't stored") {
            theme.textColorSource shouldBe TextColorSource.COLORS
            theme.getBackgroundColor(BackgroundColorPref.WIDGET_HEADER) shouldBe backgroundColor
            theme.textColors.getValue(TextColorPref.WIDGET_HEADER).color shouldBe textColor
        }
    }

    @Test
    fun colorsOfADynamicThemeAreNotRestoredFromJson_SettingsAre() {
        // Couldn't ignore it conditionally.
        if (!ColorThemeType.TONAL_LIGHT.supportedByDevice) return

        val json =
            JSONObject().apply {
                put(BackgroundColorPref.WIDGET_HEADER.colorPreferenceName, backgroundColor)
                put(TextColorPref.WIDGET_HEADER.colorPreferenceName, textColor)
                put(ColorTheme.PREF_TEXT_COLOR_SOURCE, TextColorSource.COLORS.value)
                put(ColorTheme.PREF_TEXT_SHADOW, TextShadow.DARK_SHADOW.value)
                put(
                    ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                    TimeUntilBackgroundSource.BLACK.value,
                )
                put(PREF_COLOR_THEME_TYPE, ColorThemeType.SEMANTIC_LIGHT.value)
            }
        val restored = ColorTheme.fromJson(ColorThemeType.SEMANTIC_LIGHT, json)
        restored.shouldBeInstanceOf<DynamicColorTheme>()
        restored.getBackgroundColor(BackgroundColorPref.WIDGET_HEADER) shouldBe
            BackgroundColorPref.WIDGET_HEADER.defaultColor
        restored.textShadow shouldBe TextShadow.DARK_SHADOW
        restored.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.BLACK
        restored
            .toJson(JSONObject())
            .keys()
            .asSequence()
            .toSet() shouldBe
            setOf(
                ColorTheme.PREF_TEXT_SHADOW,
                ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                PREF_COLOR_THEME_TYPE,
            )
    }

    @Test
    fun widgetLevelSettingsAreAppliedToLegacyThemes() {
        val legacyJson =
            JSONObject().apply {
                put(ColorTheme.PREF_TEXT_SHADOW, TextShadow.LIGHT_SHADOW.value)
                put(
                    ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                    TimeUntilBackgroundSource.BLACK.value,
                )
                put(InstanceSettings.PREF_DARK_THEME, JSONObject())
            }
        val themes = ColorThemes.fromJson(legacyJson)
        themes.manualColorTheme(ColorThemeType.LIGHT).textShadow shouldBe TextShadow.LIGHT_SHADOW
        themes.manualColorTheme(ColorThemeType.LIGHT).timeUntilBackgroundSource shouldBe
            TimeUntilBackgroundSource.BLACK
        if (ColorThemeType.DARK.supportedByDevice) {
            themes.manualColorTheme(ColorThemeType.DARK).textShadow shouldBe TextShadow.LIGHT_SHADOW
            themes.manualColorTheme(ColorThemeType.DARK).timeUntilBackgroundSource shouldBe
                TimeUntilBackgroundSource.BLACK
        }
    }

    @Test
    fun widgetLevelSettingsArentAppliedToTheNewFormat() {
        val json =
            JSONObject().apply {
                put(ColorTheme.PREF_TEXT_SHADOW, TextShadow.LIGHT_SHADOW.value)
                put(
                    ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                    TimeUntilBackgroundSource.BLACK.value,
                )
                put(
                    ColorThemes.PREF_COLOR_THEMES,
                    JSONArray().apply {
                        val json = JSONObject()
                        json.put(PREF_COLOR_THEME_TYPE, ColorThemeType.LIGHT.value)
                        put(ColorThemeType.LIGHT.value, json)
                    },
                )
            }
        val theme = ColorThemes.fromJson(json).manualColorTheme(ColorThemeType.LIGHT)
        theme.textShadow shouldBe TextShadow.NO_SHADOW
        theme.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.AUTO
    }

    @Test
    fun eachThemeStoresItsOwnSettings() {
        val lightTheme =
            ManualColorTheme(
                colorThemeType = ColorThemeType.LIGHT,
                textShadow = TextShadow.DARK_SHADOW,
                timeUntilBackgroundSource = TimeUntilBackgroundSource.YELLOW,
            )
        val darkTheme =
            ManualColorTheme(
                colorThemeType = ColorThemeType.DARK,
                textShadow = TextShadow.LIGHT_SHADOW,
                timeUntilBackgroundSource = TimeUntilBackgroundSource.WHITE,
            )
        val restored =
            ColorThemes.fromJson(
                (ColorThemes() + lightTheme + darkTheme).toJson(JSONObject()),
            )
        restored.manualColorTheme(ColorThemeType.LIGHT).textShadow shouldBe TextShadow.DARK_SHADOW
        restored.manualColorTheme(ColorThemeType.LIGHT).timeUntilBackgroundSource shouldBe
            TimeUntilBackgroundSource.YELLOW
        if (ColorThemeType.DARK.supportedByDevice) {
            restored.manualColorTheme(ColorThemeType.DARK).textShadow shouldBe TextShadow.LIGHT_SHADOW
            restored.manualColorTheme(ColorThemeType.DARK).timeUntilBackgroundSource shouldBe
                TimeUntilBackgroundSource.WHITE
        }
    }

    @Test
    fun themeOfAnotherTypeIsCopiedToTheRequestedType() {
        val lightTheme = ManualColorTheme(colorThemeType = ColorThemeType.LIGHT)
        val themes = ColorThemes() + lightTheme

        val darkTheme = themes.manualColorTheme(ColorThemeType.DARK)
        darkTheme.colorThemeType shouldBe ColorThemeType.DARK
        darkTheme shouldBe lightTheme.copy(colorThemeType = ColorThemeType.DARK)

        val darkOnly = ColorThemes() + ManualColorTheme(colorThemeType = ColorThemeType.DARK)
        darkOnly.manualColorTheme(ColorThemeType.LIGHT).colorThemeType shouldBe ColorThemeType.LIGHT
    }

    @Test
    fun jsonVariantDependsOnThemeType() {
        ColorTheme
            .fromJson(ColorThemeType.LIGHT, JSONObject())
            .shouldBeInstanceOf<ManualColorTheme>()
        ColorTheme
            .fromJson(ColorThemeType.DARK, JSONObject())
            .shouldBeInstanceOf<ManualColorTheme>()
        if (ColorThemeType.TONAL_LIGHT.supportedByDevice) {
            ColorTheme
                .fromJson(ColorThemeType.TONAL_LIGHT, JSONObject())
                .shouldBeInstanceOf<DynamicColorTheme>()
            ColorTheme
                .fromJson(ColorThemeType.TONAL_DARK, JSONObject())
                .shouldBeInstanceOf<DynamicColorTheme>()
        }
        if (ColorThemeType.SEMANTIC_LIGHT.supportedByDevice) {
            ColorTheme
                .fromJson(ColorThemeType.SEMANTIC_LIGHT, JSONObject())
                .shouldBeInstanceOf<DynamicColorTheme>()
            ColorTheme
                .fromJson(ColorThemeType.SEMANTIC_DARK, JSONObject())
                .shouldBeInstanceOf<DynamicColorTheme>()
        }
    }
}
