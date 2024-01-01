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
import androidx.annotation.StringRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.util.StringUtil

/** See https://github.com/andstatus/todoagenda/issues/7  */
enum class DateFormatType(val code: String, @field:StringRes val titleResourceId: Int, val pattern: String) {
    HIDDEN("hidden", R.string.hidden, ""),
    DEVICE_DEFAULT("deviceDefault", R.string.device_default, ""),
    DEFAULT_WEEKDAY("defaultWeekday", R.string.date_format_default_weekday, ""),
    DEFAULT_YTT("defaultYtt", R.string.date_format_default_ytt, ""),
    DEFAULT_DAYS("defaultDays", R.string.date_format_default_days, ""),
    ABBREVIATED("abbrev", R.string.appearance_abbreviate_dates_title, ""),
    NUMBER_OF_DAYS("days", R.string.date_format_number_of_days_to_event, "bbbb"),
    DAY_IN_MONTH("dayInMonth", R.string.date_format_day_in_month, "dd"),
    MONTH_DAY("monthDay", R.string.date_format_month_day, "MM-dd"),
    WEEK_IN_YEAR("weekInYear", R.string.date_format_week_in_year, "ww"),
    DEFAULT_EXAMPLE("example", R.string.pattern_example, "BBB, EEEE d MMM yyyy, BBBB"),
    PATTERN_EXAMPLE1("example1", R.string.pattern_example, "b 'days,' EEE, d MMM yyyy, 'week' ww"),
    CUSTOM("custom-01", R.string.custom_pattern, ""),
    UNKNOWN("unknown", R.string.not_found, "");

    val defaultValue: DateFormatValue by lazy {
        DateFormatValue(this@DateFormatType, "")
    }

    val spinnerPosition: Int
        get() {
            for (position in entries.toTypedArray().indices) {
                val type = entries[position]
                if (type == UNKNOWN) break
                if (type == this) return position
            }
            return 0
        }

    fun hasPattern(): Boolean {
        return StringUtil.nonEmpty(pattern)
    }

    val isPatternExample: Boolean
        get() = titleResourceId == R.string.pattern_example
    val isCustomPattern: Boolean
        get() = isPatternExample || this == CUSTOM

    fun toSave(): DateFormatType {
        return if (isCustomPattern) CUSTOM else this
    }

    companion object {
        val DEFAULT = DEFAULT_WEEKDAY
        fun load(storedValue: String?, defaultValue: DateFormatValue): DateFormatValue {
            val formatType = load(storedValue, UNKNOWN)
            return when (formatType) {
                UNKNOWN -> defaultValue
                CUSTOM -> DateFormatValue(
                    formatType,
                    storedValue!!.substring(CUSTOM.code.length + 1)
                )

                else -> formatType.defaultValue
            }
        }

        private fun load(storedValue: String?, defaultType: DateFormatType): DateFormatType {
            if (storedValue == null) return defaultType
            for (type in entries) {
                if (storedValue.startsWith(type.code + ":")) return type
            }
            return defaultType
        }

        fun unknownValue(): DateFormatValue {
            return UNKNOWN.defaultValue
        }

        fun getSpinnerEntryList(context: Context): List<CharSequence> {
            val list: MutableList<CharSequence> = ArrayList()
            var exampleInd = 0
            for (type in entries) {
                if (type == UNKNOWN) break
                list.add(
                    context.getText(type.titleResourceId)
                        .toString() + if (type.isPatternExample) " " + ++exampleInd else ""
                )
            }
            return list
        }
    }
}
