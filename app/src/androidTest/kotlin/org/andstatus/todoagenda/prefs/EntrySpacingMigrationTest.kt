package org.andstatus.todoagenda.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_COMPACT_LAYOUT
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_ENTRY_SPACING
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_ENTRY_SPACING_MIGRATED
import org.junit.runner.RunWith

/**
 * Verifies migration of the old "Compact layout" checkbox to the "Spacing between entries" list,
 * including the case when a list preference has already written its default value.
 */
@RunWith(KotestTestRunner::class)
class EntrySpacingMigrationTest : AnnotationSpec() {
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    @Before
    fun clearPreferences() {
        prefs.edit().clear().commit()
    }

    @Test
    fun legacyCompactLayoutIsMigratedOnce() {
        prefs.edit().putBoolean(PREF_COMPACT_LAYOUT, true).commit()

        ApplicationPreferences.getEntrySpacing(context) shouldBe EntrySpacing.COMPACT

        prefs.getString(PREF_ENTRY_SPACING, null) shouldBe EntrySpacing.COMPACT.preferenceValue
        prefs.getBoolean(PREF_ENTRY_SPACING_MIGRATED, false) shouldBe true
    }

    @Test
    fun defaultListValueDoesNotOverrideLegacyCompactLayout() {
        prefs
            .edit()
            .putBoolean(PREF_COMPACT_LAYOUT, true)
            .putString(PREF_ENTRY_SPACING, EntrySpacing.NORMAL.preferenceValue)
            .commit()

        ApplicationPreferences.getEntrySpacing(context) shouldBe EntrySpacing.COMPACT
    }

    @Test
    fun userSelectionIsKeptAfterMigration() {
        ApplicationPreferences.getEntrySpacing(context)

        prefs.edit().putString(PREF_ENTRY_SPACING, EntrySpacing.LOOSE.preferenceValue).commit()

        ApplicationPreferences.getEntrySpacing(context) shouldBe EntrySpacing.LOOSE
    }

    @Test
    fun normalSpacingIsUsedWithoutLegacyValue() {
        ApplicationPreferences.getEntrySpacing(context) shouldBe EntrySpacing.NORMAL
        prefs.getBoolean(PREF_ENTRY_SPACING_MIGRATED, false) shouldBe true
    }
}
