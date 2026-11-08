package org.andstatus.todoagenda.prefs

import android.content.Context
import android.content.Intent
import androidx.preference.ListPreference
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.WidgetConfigurationActivity
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.junit.runner.RunWith

/**
 * The "Font width" entries contain a '%' character (e.g. "Normal (100%)").
 * [ListPreference.getSummary] treats a summary text as a format string, so setting such
 * an entry as a summary directly crashed the settings screen. The screen must use a
 * summary provider instead.
 */
@RunWith(KotestTestRunner::class)
class LayoutPreferencesSummariesTest : AnnotationSpec() {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private lateinit var provider: FakeCalendarContentProvider

    @Before
    fun setUp() {
        provider = FakeCalendarContentProvider.contentProvider
    }

    @After
    fun tearDown() {
        FakeCalendarContentProvider.tearDown()
    }

    @Test
    fun summariesWithPercentCharacterAreShownWithoutCrash() {
        val scenario =
            ActivityScenario.launch<WidgetConfigurationActivity>(
                WidgetConfigurationActivity
                    .intentToStartMe(context, provider.widgetId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        try {
            var fragment: LayoutPreferencesFragment? = null
            scenario.onActivity { activity ->
                fragment = LayoutPreferencesFragment()
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings_container, fragment!!, WidgetConfigurationActivity.FRAGMENT_TAG)
                    .commitNow()
            }
            instrumentation.waitForIdleSync()
            val layoutFragment = fragment.shouldNotBeNull()
            withClue("Bind all preferences, including the Font section at the bottom") {
                scenario.onActivity {
                    val listView = layoutFragment.listView
                    listView.scrollToPosition(listView.adapter!!.itemCount - 1)
                }
                instrumentation.waitForIdleSync()
            }
            withClue("The summary contains '%' and is not treated as a format string") {
                val fontWidth =
                    layoutFragment
                        .findPreference<ListPreference>(InstanceSettings.PREF_FONT_WIDTH)
                        .shouldNotBeNull()
                fontWidth.entry shouldBe "Normal (100%)"
                fontWidth.summary shouldBe "Normal (100%)"
            }
        } finally {
            scenario.close()
        }
    }
}
