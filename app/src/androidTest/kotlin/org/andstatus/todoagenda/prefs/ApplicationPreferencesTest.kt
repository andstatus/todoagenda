package org.andstatus.todoagenda.prefs

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.prefs.colors.ColorSettingsSource
import org.andstatus.todoagenda.prefs.colors.ColorThemeType
import org.andstatus.todoagenda.prefs.colors.DarkLightTheme
import org.junit.runner.RunWith

/**
 * Verifies that only one of the light/dark parts of a theme is edited at a time.
 */
@RunWith(KotestTestRunner::class)
class ApplicationPreferencesTest : AnnotationSpec() {
    @Test
    fun dynamicThemePartFollowsColorThemeSetting() {
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_LIGHT,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
            darkLightTheme = DarkLightTheme.DARK,
        ) shouldBe ColorThemeType.NONE
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_DARK,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
            darkLightTheme = DarkLightTheme.DARK,
        ) shouldBe ColorThemeType.TONAL_DARK
        beingEdited(
            colorThemeTypeIn = ColorThemeType.SEMANTIC_DARK,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_SEMANTIC,
            darkLightTheme = DarkLightTheme.LIGHT,
        ) shouldBe ColorThemeType.NONE
        beingEdited(
            colorThemeTypeIn = ColorThemeType.SEMANTIC_LIGHT,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_SEMANTIC,
            darkLightTheme = DarkLightTheme.LIGHT,
        ) shouldBe ColorThemeType.SEMANTIC_LIGHT
    }

    @Test
    fun dynamicThemePartFollowsSystemDarkMode() {
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_LIGHT,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
            darkLightTheme = DarkLightTheme.AUTO,
            isDarkThemeOn = true,
        ) shouldBe ColorThemeType.NONE
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_DARK,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
            darkLightTheme = DarkLightTheme.AUTO,
            isDarkThemeOn = true,
        ) shouldBe ColorThemeType.TONAL_DARK
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_LIGHT,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
            darkLightTheme = DarkLightTheme.AUTO,
            isDarkThemeOn = false,
        ) shouldBe ColorThemeType.TONAL_LIGHT
    }

    @Test
    fun twoManualThemesFollowSystemDarkMode() {
        beingEdited(
            colorThemeTypeIn = ColorThemeType.LIGHT,
            colorSettingsSource = ColorSettingsSource.TWO_MANUAL,
            isDarkThemeOn = false,
        ) shouldBe ColorThemeType.LIGHT
        beingEdited(
            colorThemeTypeIn = ColorThemeType.DARK,
            colorSettingsSource = ColorSettingsSource.TWO_MANUAL,
            isDarkThemeOn = true,
        ) shouldBe ColorThemeType.DARK
        beingEdited(
            colorThemeTypeIn = ColorThemeType.LIGHT,
            colorSettingsSource = ColorSettingsSource.TWO_MANUAL,
            isDarkThemeOn = true,
        ) shouldBe ColorThemeType.NONE
    }

    @Test
    fun twoManualThemeTypeFollowsSystemDarkModeOnly() {
        ColorSettingsSource.TWO_MANUAL
            .themeType(DarkLightTheme.LIGHT, isDarkThemeOn = true) shouldBe ColorThemeType.DARK
        ColorSettingsSource.TWO_MANUAL
            .themeType(DarkLightTheme.DARK, isDarkThemeOn = false) shouldBe ColorThemeType.LIGHT
    }

    @Test
    fun singleManualThemeIsEditedInAnyMode() {
        beingEdited(
            colorThemeTypeIn = ColorThemeType.LIGHT,
            colorSettingsSource = ColorSettingsSource.SINGLE_MANUAL,
            isDarkThemeOn = true,
        ) shouldBe ColorThemeType.LIGHT
    }

    @Test
    fun themeOfAnotherColorSourceIsNotEdited() {
        beingEdited(
            colorThemeTypeIn = ColorThemeType.LIGHT,
            colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
        ) shouldBe ColorThemeType.NONE
        beingEdited(
            colorThemeTypeIn = ColorThemeType.TONAL_LIGHT,
            colorSettingsSource = ColorSettingsSource.SINGLE_MANUAL,
        ) shouldBe ColorThemeType.NONE
    }

    private fun beingEdited(
        colorThemeTypeIn: ColorThemeType,
        colorSettingsSource: ColorSettingsSource,
        darkLightTheme: DarkLightTheme = DarkLightTheme.LIGHT,
        isDarkThemeOn: Boolean = false,
    ): ColorThemeType =
        ApplicationPreferences.beingEditedColorThemeType(
            colorThemeTypeIn = colorThemeTypeIn,
            colorSettingsSource = colorSettingsSource,
            darkLightTheme = darkLightTheme,
            isDarkThemeOn = isDarkThemeOn,
        )
}
