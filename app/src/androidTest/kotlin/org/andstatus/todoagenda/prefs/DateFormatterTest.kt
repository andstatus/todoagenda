package org.andstatus.todoagenda.prefs

import org.andstatus.todoagenda.BaseWidgetTest
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.prefs.dateformat.DateFormatValue
import org.andstatus.todoagenda.prefs.dateformat.DateFormatter
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat

/**
 * @author yvolk@yurivolkov.com
 */
class DateFormatterTest : BaseWidgetTest() {
    @Test
    fun testTimeZones() {
        ensureNonEmptyResults()
        assertNow("2020-02-15T01:00:00.000+08:00")
        assertNow("2020-02-15T23:00:00.000+08:00")
        assertNow("2020-02-29T01:00:00.000+08:00")
        assertNow("2020-02-29T23:00:00.000+08:00")
        assertNow("2020-02-15T01:00:00.000-05:00")
        assertNow("2020-02-15T23:00:00.000-05:00")
        assertNow("2020-02-29T01:00:00.000-05:00")
        assertNow("2020-02-29T23:00:00.000-05:00")
        assertNow("2025-03-01T23:00:00.000-01:00")
    }

    private fun assertNow(pattern: String) {
        val now = DateTime.parse(pattern)
        provider!!.setExecutedAt(now)
        provider!!.updateAppSettings("DateFormatterTest pattern:'$pattern'")
        assertPattern(
            now,
            "MM-dd b",
            String.format(
                "%02d-%02d",
                now.monthOfYear().get(),
                now.dayOfMonth().get(),
            ) + " 0",
        )
        val yesterday = now.minusDays(1)
        assertPattern(
            yesterday,
            "MM-dd b",
            String.format(
                "%02d-%02d",
                yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get(),
            ) + " -1",
        )
        assertPattern(
            yesterday,
            "b MM.dd",
            "-1 " +
                String.format(
                    "%02d.%02d",
                    yesterday.monthOfYear().get(),
                    yesterday.dayOfMonth().get(),
                ),
        )
        assertPattern(
            yesterday,
            "MM.b.dd",
            String.format(
                "%02d.-1.%02d",
                yesterday.monthOfYear().get(),
                yesterday.dayOfMonth().get(),
            ),
        )
        val tomorrow = now.plusDays(1)
        assertPattern(
            tomorrow,
            "MM-dd b",
            String.format(
                "%02d-%02d",
                tomorrow.monthOfYear().get(),
                tomorrow.dayOfMonth().get(),
            ) + " 1",
        )
    }

    @Test
    fun customPatterns() {
        ensureNonEmptyResults()
        val settings = settings
        val now =
            settings!!
                .clock
                .now()
                .withTimeAtStartOfDay()
                .plusHours(1)
        provider!!.setExecutedAt(now)
        val todayText = provider.context.getText(R.string.today)
        val tomorrowText = provider.context.getText(R.string.tomorrow).toString()
        val inTwoDaysText = String.format(provider.context.getText(R.string.in_N_days).toString(), 2)
        val javaPattern = "yyyy-MM-dd"
        val javaFormatted =
            String.format(
                "%04d-%02d-%02d",
                now.yearOfEra().get(),
                now.monthOfYear().get(),
                now.dayOfMonth().get(),
            )
        Assert.assertEquals(javaFormatted, javaFormatted(javaPattern, now))
        assertPattern(now, javaPattern, javaFormatted)
        assertPattern(now, "$javaPattern b", "$javaFormatted 0")
        assertPattern(now, "BBB $javaPattern b", "$todayText $javaFormatted")
        assertPattern(
            now.plusDays(1),
            "BBB $javaPattern b",
            tomorrowText + " " +
                javaFormatted(javaPattern, now.plusDays(1)),
        )
        assertPattern(
            now.plusDays(1),
            "BBB, $javaPattern b",
            tomorrowText + ", " +
                javaFormatted(javaPattern, now.plusDays(1)),
        )
        assertPattern(
            now.plusDays(2),
            "BBB $javaPattern b",
            javaFormatted(javaPattern, now.plusDays(2)) + " 2",
        )
        assertPattern(
            now.plusDays(2),
            "BBB, $javaPattern b",
            javaFormatted(javaPattern, now.plusDays(2)) + " 2",
        )
        assertPattern(now, "BBB $javaPattern BBBB", "$todayText $javaFormatted")
        assertPattern(now, "BBB $javaPattern, BBBB", "$todayText $javaFormatted")
        assertPattern(
            now.plusDays(1),
            "BBB $javaPattern BBBB",
            tomorrowText + " " +
                javaFormatted(javaPattern, now.plusDays(1)),
        )
        assertPattern(
            now.plusDays(1),
            "BBB $javaPattern, BBBB",
            tomorrowText + " " +
                javaFormatted(javaPattern, now.plusDays(1)),
        )
        assertPattern(
            now.plusDays(2),
            "BBB $javaPattern BBBB",
            javaFormatted(javaPattern, now.plusDays(2)) + " " + inTwoDaysText,
        )
        assertPattern(now, "b", "0")
        assertPattern(
            now,
            "MM-dd bb",
            String.format("%02d-%02d", now.monthOfYear().get(), now.dayOfMonth().get()) + " 00",
        )
        assertPattern(
            now.plusDays(1),
            "",
            "(not implemented: " +
                DateFormatValue.of(DateFormatType.CUSTOM, "").getSummary(provider.context) + ")",
        )
        assertPattern(now.plusDays(1), "b", "1")
        assertPattern(now.plusDays(1), "bbb", "001")
        assertPattern(now.plusDays(1), "bbbb", tomorrowText)
        assertPattern(now.plusDays(1), "BBB", tomorrowText)
        assertPattern(now.plusDays(1), "BBBB", tomorrowText)
        assertPattern(now.plusDays(-2), "BBB", "")
        assertPattern(
            now.plusDays(-2),
            "BBBB",
            String.format(provider.context.getText(R.string.N_days_ago).toString(), 2),
        )
        assertPattern(now.plusDays(2), "BBB", "")
        assertPattern(now.plusDays(2), "BBBB", inTwoDaysText)
        assertPattern(now.plusDays(5), "b", "5")
        assertPattern(now.plusDays(5), "bbb", "005")
        assertPattern(now.plusDays(5), "bbbb", "5")
        assertPattern(now.plusDays(1), "'begin' b", "begin 1")
        assertPattern(now.minusDays(5), "bbb", "-05")
        assertPattern(now.minusDays(5), "bbbb", "-5")
    }

    private fun assertPattern(
        date: DateTime,
        pattern: String,
        expected: String,
    ) {
        val format = DateFormatValue.of(DateFormatType.CUSTOM, pattern)
        val now = settings.clock.now()
        val formatter = DateFormatter(settings.context, format, now)
        Assert.assertEquals("Date: $date, Now:$now, Pattern: [$pattern]", expected, formatter.formatDate(date))
    }

    companion object {
        private fun javaFormatted(
            javaPattern: String,
            date: DateTime,
        ): String {
            val locale = MyLocale.locale
            return SimpleDateFormat(javaPattern, locale).format(DateFormatter.toJavaDate(date))
        }
    }
}
