package org.andstatus.todoagenda.prefs.colors

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.layout.TextShadow
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.InstanceSettings.Companion.PREF_COLOR_SETTINGS_SOURCE
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Edits colors of a test widget the same way the user does: opens the Widget's settings, changes
 * settings, lets the app itself close and save them, and reopen the settings for the selected theme.
 */
@RunWith(KotestTestRunner::class)
class ColorThemeSettingsEditingTest : AnnotationSpec() {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val activities = CopyOnWriteArrayList<Activity>()
    private val resumedActivities = CopyOnWriteArrayList<Activity>()
    private lateinit var application: Application
    private lateinit var provider: FakeCalendarContentProvider

    private val lifecycleCallbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle?,
            ) {
                activities.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                resumedActivities.add(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                resumedActivities.remove(activity)
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle,
            ) {
            }

            override fun onActivityDestroyed(activity: Activity) {}
        }

    @Before
    fun setUp() {
        activities.clear()
        resumedActivities.clear()
        provider = FakeCalendarContentProvider.contentProvider
        application = context.applicationContext as Application
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    @After
    fun tearDown() {
        instrumentation.waitForIdleSync()
        finishActivities()
        if (::application.isInitialized) {
            application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        }
        FakeCalendarContentProvider.tearDown()
    }

    @Test
    fun switchingColorThemeKeepsSettingsOfBothThemes() {
        if (!ColorSettingsSource.MATERIAL_YOU_TONAL.supportedByDevice) return
        provider.settings =
            provider.settings.copy(
                colorSettingsSource = ColorSettingsSource.MATERIAL_YOU_TONAL,
                darkLightTheme = DarkLightTheme.LIGHT,
                colorThemes =
                    ColorThemes() +
                        tonalTheme(
                            ColorThemeType.TONAL_LIGHT,
                            TextShadow.DARK_SHADOW,
                            TimeUntilBackgroundSource.YELLOW,
                        ) +
                        tonalTheme(
                            ColorThemeType.TONAL_DARK,
                            TextShadow.LIGHT_SHADOW,
                            TimeUntilBackgroundSource.WHITE,
                        ),
            )
        val scenario = launchSettings()
        try {
            val fragment = waitForCurrentColorsFragment()
            fragment.shouldBeInstanceOf<DynamicColorsPreferencesFragment>()
            ApplicationPreferences.getTextShadow(context) shouldBe TextShadow.DARK_SHADOW
            withClue(
                "The user selects the dark part. The app closes the settings, saves them " +
                    "and opens the settings of the dark part again:",
            ) {
                setPreference(
                    fragment,
                    InstanceSettings.PREF_DARK_LIGHT_THEME,
                    DarkLightTheme.DARK.value,
                )
                waitUntil("The app didn't save the settings and reopen them for the dark theme") {
                    val saved = savedSettings()
                    saved.darkLightTheme == DarkLightTheme.DARK &&
                        ApplicationPreferences.getColorThemeType(context) == ColorThemeType.TONAL_DARK &&
                        ApplicationPreferences.getTextShadow(context) == TextShadow.LIGHT_SHADOW
                }
            }
            withClue("Read the settings, which the app saved itself") {
                val saved = savedSettings()
                saved.darkLightTheme shouldBe DarkLightTheme.DARK
                ApplicationPreferences.getTextShadow(context) shouldBe TextShadow.LIGHT_SHADOW
                ApplicationPreferences.getTimeUntilBackgroundSource(context) shouldBe
                    TimeUntilBackgroundSource.WHITE
                val darkPart = saved.colors()
                darkPart.textShadow shouldBe TextShadow.LIGHT_SHADOW
                darkPart.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.WHITE
            }
            withClue("Settings of the light part stay as they were") {
                val lightPart = savedTheme(ColorSettingsSource.MATERIAL_YOU_TONAL, DarkLightTheme.LIGHT)
                lightPart.textShadow shouldBe TextShadow.DARK_SHADOW
                lightPart.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.YELLOW
            }
        } finally {
            runCatching { scenario.close() }
        }
    }

    @Test
    fun changingColorSettingsSourceSavesPreviousThemeAndOpensNewTheme() {
        if (!ColorSettingsSource.MATERIAL_YOU_TONAL.supportedByDevice) return
        val manualLightTheme =
            ManualColorTheme(
                colorThemeType = ColorThemeType.LIGHT,
                textShadow = TextShadow.NO_SHADOW,
                timeUntilBackgroundSource = TimeUntilBackgroundSource.AUTO,
            )
        val tonalLightTheme =
            tonalTheme(
                ColorThemeType.TONAL_LIGHT,
                TextShadow.LIGHT_SHADOW,
                TimeUntilBackgroundSource.WHITE,
            )
        provider.settings =
            provider.settings.copy(
                colorSettingsSource = ColorSettingsSource.SINGLE_MANUAL,
                darkLightTheme = DarkLightTheme.LIGHT,
                colorThemes = ColorThemes() + manualLightTheme + tonalLightTheme,
            )
        val scenario = launchSettings()
        try {
            val manualFragment = waitForCurrentColorsFragment()
            manualFragment.shouldBeInstanceOf<ManualColorsPreferencesFragment>()
            withClue("The user edits the currently selected (manual) theme") {
                setPreference(manualFragment, ColorTheme.PREF_TEXT_SHADOW, TextShadow.DARK_SHADOW.value)
                savedTheme(ColorSettingsSource.SINGLE_MANUAL).textShadow shouldBe TextShadow.DARK_SHADOW
            }
            withClue("... and then selects another \"Color settings source\"") {
                setPreference(
                    manualFragment,
                    PREF_COLOR_SETTINGS_SOURCE,
                    ColorSettingsSource.MATERIAL_YOU_TONAL.value,
                )
            }
            withClue(
                "The app saves settings of the previous theme, loads settings of the new theme " +
                    "to ApplicationPreferences and opens the settings for the new theme",
            ) {
                waitUntil("The settings of the new theme were not opened") {
                    ApplicationPreferences.getSupportedColorSettingsSource(context) ==
                        ColorSettingsSource.MATERIAL_YOU_TONAL &&
                        ApplicationPreferences.getColorThemeType(context) == ColorThemeType.TONAL_LIGHT &&
                        ApplicationPreferences.getTextShadow(context) == TextShadow.LIGHT_SHADOW &&
                        ApplicationPreferences.getTimeUntilBackgroundSource(context) ==
                        TimeUntilBackgroundSource.WHITE
                }
            }
            val dynamicFragment = waitForCurrentColorsFragment()
            dynamicFragment.shouldBeInstanceOf<DynamicColorsPreferencesFragment>()
            withClue(
                "The changed Color settings source is only a request to edit the new theme: " +
                    "it is not stored to the Widget's settings yet",
            ) {
                savedSettings().colorSettingsSource shouldBe ColorSettingsSource.SINGLE_MANUAL
                savedSettings().colors().textShadow shouldBe TextShadow.DARK_SHADOW
            }
            withClue("The previous theme's change is saved") {
                savedTheme(ColorSettingsSource.SINGLE_MANUAL).textShadow shouldBe TextShadow.DARK_SHADOW
            }
            withClue("The user edits the new theme") {
                setPreference(dynamicFragment, ColorTheme.PREF_TEXT_SHADOW, TextShadow.NO_SHADOW.value)
                setPreference(
                    dynamicFragment,
                    ColorTheme.PREF_TIME_UNTIL_BACKGROUND_SOURCE,
                    TimeUntilBackgroundSource.BLACK.value,
                )
            }
            withClue("The user exits the settings, and the app saves the changes") {
                finishActivities()
                waitUntil("The settings of the new theme were not saved") {
                    val tonal = savedTheme(ColorSettingsSource.MATERIAL_YOU_TONAL)
                    tonal.textShadow == TextShadow.NO_SHADOW &&
                        tonal.timeUntilBackgroundSource == TimeUntilBackgroundSource.BLACK
                }
            }
            val manual = savedTheme(ColorSettingsSource.SINGLE_MANUAL)
            manual.textShadow shouldBe TextShadow.DARK_SHADOW
            manual.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.AUTO
            val tonal = savedTheme(ColorSettingsSource.MATERIAL_YOU_TONAL)
            tonal.textShadow shouldBe TextShadow.NO_SHADOW
            tonal.timeUntilBackgroundSource shouldBe TimeUntilBackgroundSource.BLACK
            savedSettings().colorSettingsSource shouldBe ColorSettingsSource.MATERIAL_YOU_TONAL
        } finally {
            runCatching { scenario.close() }
        }
    }

    private fun launchSettings(): ActivityScenario<WidgetConfigurationActivity> {
        val intent =
            WidgetConfigurationActivity
                .intentToStartMe(context, provider.widgetId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(
                    WidgetConfigurationActivity.EXTRA_GOTO_PREFERENCES_SECTION,
                    WidgetConfigurationActivity.EXTRA_GOTO_SECTION_COLORS,
                )
        return ActivityScenario.launch(intent)
    }

    /** Waits until the settings screen is displayed and returns its current fragment */
    private fun waitForCurrentColorsFragment(): PreferenceFragmentCompat {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            instrumentation.waitForIdleSync()
            val activity =
                activities
                    .filterIsInstance<WidgetConfigurationActivity>()
                    .lastOrNull { resumedActivities.contains(it) }
            if (activity != null) {
                var fragment: PreferenceFragmentCompat? = null
                instrumentation.runOnMainSync {
                    activity.supportFragmentManager.executePendingTransactions()
                    fragment =
                        activity.supportFragmentManager
                            .findFragmentByTag(WidgetConfigurationActivity.FRAGMENT_TAG) as? PreferenceFragmentCompat
                }
                if (fragment?.isResumed == true) return fragment!!
            }
            Thread.sleep(50)
        }
        throw AssertionError("Widget settings for colors were not opened")
    }

    private fun setPreference(
        fragment: PreferenceFragmentCompat,
        key: String,
        value: String,
    ) {
        instrumentation.runOnMainSync {
            val preference = fragment.findPreference<ListPreference>(key).shouldNotBeNull()
            withClue("The same sequence of calls as when the user selects a value in the list") {
                if (preference.callChangeListener(value)) {
                    preference.value = value
                }
            }
        }
    }

    private fun waitUntil(
        message: String,
        condition: () -> Boolean,
    ) {
        val deadline = System.currentTimeMillis() + TIMEOUT_MILLIS
        while (System.currentTimeMillis() < deadline) {
            instrumentation.waitForIdleSync()
            if (condition()) return
            Thread.sleep(100)
        }
        throw AssertionError(message)
    }

    private fun finishActivities() {
        instrumentation.runOnMainSync {
            activities
                .filter { !it.isFinishing && !it.isDestroyed }
                .forEach { it.finish() }
        }
        instrumentation.waitForIdleSync()
    }

    private fun savedSettings(): InstanceSettings = AllSettings.instanceFromId(context, provider.widgetId)

    private fun savedTheme(
        colorSettingsSource: ColorSettingsSource,
        darkLightTheme: DarkLightTheme = DarkLightTheme.LIGHT,
    ): ColorTheme =
        savedSettings()
            .copy(
                colorSettingsSource = colorSettingsSource,
                darkLightTheme = darkLightTheme,
            ).colors()

    private fun tonalTheme(
        colorThemeType: ColorThemeType,
        textShadow: TextShadow,
        timeUntilBackgroundSource: TimeUntilBackgroundSource,
    ): DynamicColorTheme =
        DynamicColorTheme(
            colorThemeType = colorThemeType,
            textShadow = textShadow,
            timeUntilBackgroundSource = timeUntilBackgroundSource,
        )

    companion object {
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
