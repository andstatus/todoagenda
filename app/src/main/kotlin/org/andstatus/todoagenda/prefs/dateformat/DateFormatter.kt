/*
 * Copyright (c) 2019 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.andstatus.todoagenda.prefs.dateformat

import android.content.Context
import android.text.format.DateUtils
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.MyLocale
import org.andstatus.todoagenda.util.StringUtil
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Formatter

class DateFormatter(
    private val context: Context?,
    private val dateFormatValue: DateFormatValue?,
    private val now: DateTime,
) {
    fun formatDate(date: DateTime): CharSequence {
        return try {
            if (dateFormatValue!!.hasPattern()) {
                return formatDateCustom(date, dateFormatValue.pattern)
            }
            when (dateFormatValue.type) {
                DateFormatType.HIDDEN -> ""
                DateFormatType.DEVICE_DEFAULT -> formatDateTime(date, DateUtils.FORMAT_SHOW_DATE)
                DateFormatType.DEFAULT_WEEKDAY ->
                    formatDateTime(
                        date,
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY,
                    )

                DateFormatType.ABBREVIATED ->
                    formatDateTime(
                        date,
                        DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_SHOW_DATE or
                            DateUtils.FORMAT_SHOW_WEEKDAY,
                    )

                DateFormatType.DEFAULT_DAYS -> formatDefaultWithNumberOfDaysToEvent(date)
                DateFormatType.DEFAULT_YTT -> {
                    val str1 =
                        formatNumberOfDaysToEventText(
                            context,
                            3,
                            getNumberOfDaysToEvent(date),
                        )
                    (if (str1.length == 0) "" else "$str1, ") + formatDateTime(date, DateUtils.FORMAT_SHOW_DATE)
                }

                DateFormatType.NUMBER_OF_DAYS ->
                    formatNumberOfDaysToEvent(
                        context,
                        5,
                        getNumberOfDaysToEvent(date),
                    )

                else -> "(not implemented: " + dateFormatValue.getSummary(context) + ")"
            }
        } catch (e: Exception) {
            e.localizedMessage
        }
    }

    private fun formatDefaultWithNumberOfDaysToEvent(date: DateTime): CharSequence {
        val dateStub = "dateStub"
        return formatDateCustom(date, "BBB, '$dateStub', BBBB")
            .replace(dateStub, formatDateTime(date, DateUtils.FORMAT_SHOW_DATE))
    }

    private fun formatDateTime(
        date: DateTime,
        flags: Int,
    ): String {
        val millis = toJavaDate(date).time
        return DateUtils
            .formatDateRange(
                context,
                Formatter(StringBuilder(50), MyLocale.locale),
                millis,
                millis,
                flags,
                date.zone.id,
            ).toString()
    }

    private fun getNumberOfDaysToEvent(date: DateTime): Int =
        Days
            .daysBetween(
                now.withZone(date.zone).withTimeAtStartOfDay(),
                date.withTimeAtStartOfDay(),
            ).days

    private fun formatDateCustom(
        date: DateTime,
        pattern: String?,
    ): String =
        if (StringUtil.isEmpty(pattern)) {
            ""
        } else {
            try {
                val pattern2 = preProcessNumberOfDaysToEvent(date, pattern)
                val simpleDateFormat = SimpleDateFormat(pattern2, MyLocale.locale)
                simpleDateFormat.format(toJavaDate(date))
            } catch (e: Exception) {
                e.localizedMessage
            }
        }

    private fun preProcessNumberOfDaysToEvent(
        date: DateTime,
        pattern: String?,
        startIndex: Int = 0,
        alreadyShown: Boolean = false,
    ): String? {
        val ind1 = getIndexOfNumberOfDaysLetter(pattern, startIndex)
        if (ind1 < startIndex) return pattern
        val patternLetter = pattern!![ind1]
        var ind2 = ind1
        while (ind2 < pattern.length && pattern[ind2] == patternLetter) {
            ind2++
        }
        val numberOfDaysFormatted =
            if (alreadyShown) {
                ""
            } else if (patternLetter == NUMBER_OF_DAYS_LOWER_LETTER) {
                formatNumberOfDaysToEvent(
                    context,
                    ind2 - ind1,
                    getNumberOfDaysToEvent(date),
                )
            } else {
                formatNumberOfDaysToEventText(
                    context,
                    ind2 - ind1,
                    getNumberOfDaysToEvent(date),
                )
            }
        val replacement = if (numberOfDaysFormatted.length == 0) "" else "'$numberOfDaysFormatted'"
        val pattern2 =
            (if (ind1 > 0) pattern.substring(0, ind1) else "") +
                replacement +
                if (ind2 < pattern.length) pattern.substring(ind2) else ""
        return preProcessNumberOfDaysToEvent(
            date,
            trimPattern(pattern2),
            startIndex + replacement.length,
            alreadyShown || replacement.length > 0,
        )
    }

    private fun trimPattern(pattern: String): String {
        var pattern2 = pattern.trim { it <= ' ' }
        if (pattern2.endsWith(",")) pattern2 = pattern2.substring(0, pattern2.length - 1)
        if (pattern2.startsWith(",")) pattern2 = pattern2.substring(1)
        return if (pattern2 == pattern) pattern else trimPattern(pattern2)
    }

    private fun getIndexOfNumberOfDaysLetter(
        pattern: String?,
        startIndex: Int,
    ): Int {
        var inQuotes = false
        for (ind in startIndex until pattern!!.length) {
            if ((pattern[ind] == NUMBER_OF_DAYS_LOWER_LETTER || pattern[ind] == NUMBER_OF_DAYS_UPPER_LETTER) &&
                !inQuotes
            ) {
                return ind
            }
            if (pattern[ind] == '\'') inQuotes = !inQuotes
        }
        return -1
    }

    companion object {
        private const val NUMBER_OF_DAYS_LOWER_LETTER = 'b'
        private const val NUMBER_OF_DAYS_UPPER_LETTER = 'B'

        fun toJavaDate(date: DateTime): Date = Date(date.yearOfEra - 1900, date.monthOfYear - 1, date.dayOfMonth)

        fun formatNumberOfDaysToEvent(
            context: Context?,
            formatLength: Int,
            daysToEvent: Int,
        ): CharSequence {
            if (formatLength >= 4) {
                val ytt = getYtt(context, daysToEvent)
                if (ytt.length > 0) return ytt
            }
            if (Math.abs(daysToEvent) > 9999) return "..."
            val days1: CharSequence = Integer.toString(daysToEvent)
            return if (days1.length > formatLength || formatLength >= 4) {
                days1
            } else {
                String.format(
                    "%0" + formatLength + "d",
                    daysToEvent,
                )
            }
        }

        fun formatNumberOfDaysToEventText(
            context: Context?,
            formatLength: Int,
            daysToEvent: Int,
        ): CharSequence {
            val ytt = getYtt(context, daysToEvent)
            if (ytt.length > 0) return ytt
            return if (formatLength < 4) {
                ""
            } else {
                String.format(
                    context!!.getText(if (daysToEvent < 0) R.string.N_days_ago else R.string.in_N_days).toString(),
                    Math.abs(daysToEvent),
                )
            }
        }

        fun getYtt(
            context: Context?,
            daysToEvent: Int,
        ): CharSequence =
            when (daysToEvent) {
                -1 -> context!!.getText(R.string.yesterday)
                0 -> context!!.getText(R.string.today)
                1 -> context!!.getText(R.string.tomorrow)
                else -> ""
            }
    }
}
