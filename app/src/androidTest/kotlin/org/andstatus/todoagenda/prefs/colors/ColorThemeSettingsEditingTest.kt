package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Edits colors of a test widget the same way the user does: opens the Widget's settings, changes
 * the "Color theme", then lets the app itself close the settings (saving them) and reopen them
 * for the other theme.
 */
@RunWith(KotestTestRunner::class)
class ColorThemeSettingsEditingTest : AnnotationSpec() {
    @Test
    fun switchingColorThemeKeepsSettingsOfBothThemes() {
        if (!ColorSettingsSource.MATERIAL_YOU_TONAL.supportedByDevice) return

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context: Context = instrumentation.targetContext
        val provider = FakeCalendarContentProvider.contentProvider
        val activities = CopyOnWriteArrayList<Activity>()
        val lifecycleCallbacks =
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {
                    activities.add(activity)
                }

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityResumed(activity: Activity) {}

                override fun onActivityPaused(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {
                }

                override fun onActivityDestroyed(activity: Activity) {}
            }
        val application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        try {
            val lightTheme =
                DynamicColorTheme(
                    colorThemeType = ColorThemeType.TONAL_LIGHT,
                    textShadow = TextShadow.DARK_SHADOW,
                    timeUntilBackgroundSource = TimeUntilBackgroundSource.YELLOW,
                )
            val darkTheme =
                DynamicColorTheme(
                    colorThemeType = ColorThemeType.TONAL_DARK,
                    textShadow = TextShadow.LIGHT_SHADOW,
                    timeUntilBackgroundSource = TimeUntilBackgroundSource.WHITE,
                )
            provider.settings =
                provider.settings.copy(
                    colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
                    darkLightTheme = DarkLightTheme.LIGHT,
                    colorThemes = ColorThemes() + lightTheme + darkTheme,
                )
            // The user opens the Widget's settings for editing colors:
            // the app copies the light theme settings to the preferences
            val intent =
                WidgetConfigurationActivity
                    .intentToStartMe(context, provider.widgetId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(
                        WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION,
                        WidgetConfigurationActivity.EXTRA_GOTO_SECTION_COLORS,
                    )
            val scenario = ActivityScenario.launch<WidgetConfigurationActivity>(intent)
            try {
                var colorsFragment: DynamicColorsPreferencesFragment? = null
                scenario.onActivity { activity ->
                    activity.supportFragmentManager.executePendingTransactions()
                    colorsFragment =
                        activity.supportFragmentManager
                            .findFragmentByTag(WidgetConfigurationActivity.FRAGMENT_TAG) as? DynamicColorsPreferencesFragment
                }
                val fragment = colorsFragment.shouldNotBeNull()
                ApplicationPreferences.getTextShadow(context) shouldBe TextShadow.DARK_SHADOW
                // The user selects the dark part. The app closes the settings, saves them
                // and opens the settings of the dark part again:
                scenario.onActivity {
                    fragment
                        .findPreference<ListPreference>(InstanceSettings.PREF_DARK_LIGHT_THEME)
                        .shouldNotBeNull()
                        .value = DarkLightTheme.DARK.value
                }
                waitUntilSavedAndReopened()
                // Read the settings, which the app saved itself
                val saved = AllSettings.instanceFromId(context, provider.widgetId)
                saved.darkLightTheme shouldBe DarkLightTheme.DARK
                ApplicationPreferences.getTextShadow(context) shouldBe TextShadow.LIGHT_SHADOW
                ApplicationPreferences.getTimeUntilBackgroundSource(context) shouldBe
                    TimeUntilBackgroundSource.WHITE
                val darkPart = saved.colors()
                darkPart.textShadow shouldBe TextShadow.LIGHT_SHADOW
                darkPart.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.WHITE
                // Settings of the light part stay as they were
                val lightPart = saved.copy(darkLightTheme = DarkLightTheme.LIGHT).colors()
                lightPart.textShadow shouldBe TextShadow.DARK_SHADOW
                lightPart.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.YELLOW
            } finally {
                runCatching { scenario.close() }
            }
        } finally {
            instrumentation.waitForIdleSync()
            instrumentation.runOnMainSync {
                activities
                    .filter { !it.isFinishing && !it.isDestroyed }
                    .forEach { it.finish() }
            }
            instrumentation.waitForIdleSync()
            application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
            FakeCalendarContentProvider.tearDown()
        }
    }

    /** Waits until the app saves the change and reopens the settings for the dark theme */
    private fun waitUntilSavedAndReopened() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context: Context = instrumentation.targetContext
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            instrumentation.waitForIdleSync()
            val saved =
                AllSettings.instanceFromId(
                    context,
                    ApplicationPreferences.getWidgetId(context),
                )
            if (saved.darkLightTheme == DarkLightTheme.DARK &&
                ApplicationPreferences.getColorThemeType(context) == ColorThemeType.TONAL_DARK &&
                ApplicationPreferences.getTextShadow(context) == TextShadow.LIGHT_SHADOW
            ) {
                return
            }
            Thread.sleep(100)
        }
        throw AssertionError("The app didn't save the settings and reopen them for the dark theme")
    }

    companion object {
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
