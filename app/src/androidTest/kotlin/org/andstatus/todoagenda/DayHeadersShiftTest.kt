package org.andstatus.todoagenda

import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import org.andstatus.todoagenda.prefs.MyLocale
import org.andstatus.todoagenda.prefs.TestLocale
import org.andstatus.todoagenda.widget.DayHeader
import org.junit.Assert
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class DayHeadersShiftTest : BaseWidgetTest() {
    @Test
    fun testDayHeadersShift() {
        val method = "testDayHeadersShift"
        val inputs = provider.loadResultsAndSettings(
            org.andstatus.todoagenda.test.R.raw.day_headers_shift
        )
        provider.addResults(inputs)
        playResults(method)
        val dayHeader0 = factory.widgetEntries[0] as DayHeader
        Assert.assertEquals(
            "First day header should be Jan 8 ${factory.widgetEntries}", 8,
            dayHeader0.entryDate.dayOfMonth().get().toLong()
        )

        val context1 = InstrumentationRegistry.getInstrumentation().targetContext
        val context = context1.applicationContext as ContextWrapper
        val locale = MyLocale.locale.toLanguageTag()
        val enUsLocale = "en_US"
        try {
            if (locale != enUsLocale) {
                TestLocale.setLocale(context, enUsLocale)
            }
            val dayHeaderTitle = settings.dayHeaderDateFormatter().formatDate(dayHeader0.entryDate)
            Assert.assertEquals(
                "First day header should show Jan 8 ${factory.widgetEntries}",
                "Wednesday, January 8, 2020", dayHeaderTitle
            )
        } finally {
            if (locale != enUsLocale) {
                TestLocale.setLocale(context, locale)
            }
        }
    }
}
