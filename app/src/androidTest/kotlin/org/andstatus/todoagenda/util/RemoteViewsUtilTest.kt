package org.andstatus.todoagenda.util

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.widget.RemoteViews
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.runner.junit4.KotestTestRunner
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.EntrySpacing
import org.andstatus.todoagenda.prefs.FontFamily
import org.andstatus.todoagenda.prefs.FontWidth
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.junit.runner.RunWith

@RunWith(KotestTestRunner::class)
class RemoteViewsUtilTest : AnnotationSpec() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun defaultFontFamilyKeepsTextAsIs() {
        val text: CharSequence = "Event title"
        (RemoteViewsUtil.withFontFamily(FontFamily.DEFAULT, text) === text) shouldBe true
    }

    @Test
    fun selectedFontFamilyIsAppliedAsTypefaceSpan() {
        val result = RemoteViewsUtil.withFontFamily(FontFamily.SANS_SERIF_CONDENSED, "Event title")
        val spanned = result as Spanned
        val spans = spanned.getSpans(0, spanned.length, TypefaceSpan::class.java)
        spans.size shouldBe 1
        spans.first().family shouldBe "sans-serif-condensed"
    }

    @Test
    fun existingSpansArePreserved() {
        val source = SpannableString("Event title")
        source.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            source.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        val result = RemoteViewsUtil.withFontFamily(FontFamily.SERIF, source) as Spanned
        result.getSpans(0, result.length, TypefaceSpan::class.java).size shouldBe 1
        result.getSpans(0, result.length, StyleSpan::class.java).size shouldBe 1
    }

    @Test
    fun remoteViewsApplyFontFamily() {
        val view = applyToTextView(instance(fontFamily = FontFamily.SANS_SERIF_CONDENSED))
        view.text.toString() shouldBe "Event title"
        val spans = (view.text as Spanned).getSpans(0, view.text.length, TypefaceSpan::class.java)
        spans.size shouldBe 1
        spans.first().family shouldBe "sans-serif-condensed"
    }

    @Test
    fun remoteViewsApplyFontWidth() {
        val view = applyToTextView(instance(fontWidth = FontWidth.CONDENSED))
        view.textScaleX shouldBe 0.8f
    }

    @Test
    fun defaultFontSettingsKeepTheLayoutFont() {
        val view = applyToTextView(instance())
        view.text.toString() shouldBe "Event title"
        (view.text is Spanned) shouldBe false
        view.textScaleX shouldBe 1.0f
    }

    @Test
    fun entrySpacingIncreasesVerticalPadding() {
        val normal = applyToEntry(EntrySpacing.NORMAL)
        val relaxed = applyToEntry(EntrySpacing.RELAXED)
        val loose = applyToEntry(EntrySpacing.LOOSE)
        (relaxed.paddingTop > normal.paddingTop) shouldBe true
        (loose.paddingTop > relaxed.paddingTop) shouldBe true
        normal.paddingTop shouldBe normal.paddingBottom
    }

    @Test
    fun compactEntrySpacingRemovesAllPadding() {
        val compact = applyToEntry(EntrySpacing.COMPACT)
        compact.paddingLeft shouldBe 0
        compact.paddingTop shouldBe 0
        compact.paddingRight shouldBe 0
        compact.paddingBottom shouldBe 0
    }

    private fun instance(
        fontFamily: FontFamily = FontFamily.DEFAULT,
        fontWidth: FontWidth = FontWidth.NORMAL,
        entrySpacing: EntrySpacing = EntrySpacing.NORMAL,
    ) = InstanceSettings(
        contextIn = context,
        widgetId = 1,
        entrySpacing = entrySpacing,
        fontFamily = fontFamily,
        fontWidth = fontWidth,
    )

    private fun applyToTextView(settings: InstanceSettings): TextView {
        val rv = RemoteViews(context.packageName, R.layout.event_entry)
        RemoteViewsUtil.setText(settings, rv, R.id.event_entry_title, "Event title")
        return rv.apply(context, null).findViewById(R.id.event_entry_title)
    }

    private fun applyToEntry(entrySpacing: EntrySpacing): View {
        val rv = RemoteViews(context.packageName, R.layout.event_entry)
        RemoteViewsUtil.setEntrySpacing(instance(entrySpacing = entrySpacing), rv)
        return rv.apply(context, null)
    }
}
