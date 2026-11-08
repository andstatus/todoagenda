package org.andstatus.todoagenda.prefs

import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class FontSettingsTest : AnnotationSpec() {
    @Test
    fun fontFamilyIsParsedAndHasAFallback() {
        FontFamily.fromPreferenceValue("sans_serif_condensed") shouldBe FontFamily.SANS_SERIF_CONDENSED
        FontFamily.fromPreferenceValue("serif") shouldBe FontFamily.SERIF
        FontFamily.fromPreferenceValue("") shouldBe FontFamily.DEFAULT
        FontFamily.fromPreferenceValue(null) shouldBe FontFamily.DEFAULT
        FontFamily.fromPreferenceValue("unknown") shouldBe FontFamily.DEFAULT
    }

    @Test
    fun defaultFontFamilyKeepsTheLayoutTypeface() {
        FontFamily.DEFAULT.isDefault shouldBe true
        FontFamily.DEFAULT.family shouldBe null
        FontFamily.SANS_SERIF_CONDENSED.family shouldBe "sans-serif-condensed"
        FontFamily.MONOSPACE.family shouldBe "monospace"
    }

    @Test
    fun fontWidthIsParsedAndHasAFallback() {
        FontWidth.fromPreferenceValue("0.8") shouldBe FontWidth.CONDENSED
        FontWidth.fromPreferenceValue("1.2") shouldBe FontWidth.EXTRA_WIDE
        FontWidth.fromPreferenceValue("") shouldBe FontWidth.NORMAL
        FontWidth.fromPreferenceValue(null) shouldBe FontWidth.NORMAL
        FontWidth.fromPreferenceValue("2.0") shouldBe FontWidth.NORMAL
    }

    @Test
    fun entrySpacingIsParsedAndHasAFallback() {
        EntrySpacing.fromPreferenceValue("compact") shouldBe EntrySpacing.COMPACT
        EntrySpacing.fromPreferenceValue("relaxed") shouldBe EntrySpacing.RELAXED
        EntrySpacing.fromPreferenceValue("loose") shouldBe EntrySpacing.LOOSE
        EntrySpacing.fromPreferenceValue("unknown") shouldBe EntrySpacing.NORMAL
        EntrySpacing.COMPACT.isCompact shouldBe true
        EntrySpacing.NORMAL.isCompact shouldBe false
    }

    @Test
    fun legacyCompactLayoutIsMigrated() {
        EntrySpacing.fromLegacyCompactLayout(true) shouldBe EntrySpacing.COMPACT
        EntrySpacing.fromLegacyCompactLayout(false) shouldBe EntrySpacing.NORMAL
    }

    @Test
    fun widerSpacingAddsMoreVerticalPadding() {
        EntrySpacing.entries
            .sortedBy { it.verticalPaddingMultiplier }
            .map { it.preferenceValue } shouldBe
            listOf("compact", "normal", "relaxed", "loose")
    }
}
