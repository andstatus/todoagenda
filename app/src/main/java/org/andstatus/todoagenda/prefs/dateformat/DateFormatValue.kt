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
import org.andstatus.todoagenda.util.StringUtil

class DateFormatValue(val type: DateFormatType, val value: String) {
    fun save(): String {
        return if (type == type.toSave()) {
            if (type == DateFormatType.UNKNOWN) "" else type.code + ":" + pattern
        } else {
            toSave().save()
        }
    }

    fun hasPattern(): Boolean {
        return StringUtil.nonEmpty(pattern)
    }

    val pattern: String
        get() = if (StringUtil.isEmpty(value)) type.pattern else value

    fun getSummary(context: Context?): CharSequence {
        return context!!.getText(type.titleResourceId).toString() + if (type.isCustomPattern) ": $value" else ""
    }

    fun toSave(): DateFormatValue {
        return of(type.toSave(), value)
    }

    companion object {
        fun loadOrUnknown(defaultValue: Any?): DateFormatValue {
            return if (defaultValue == null) DateFormatType.unknownValue() else load(
                defaultValue.toString(),
                DateFormatType.unknownValue()
            )
        }

        fun load(storedValue: String?, defaultValue: DateFormatValue): DateFormatValue {
            return DateFormatType.load(storedValue, defaultValue)
        }

        fun of(type: DateFormatType?, value: String): DateFormatValue {
            return if (type!!.isCustomPattern && StringUtil.nonEmpty(value)) DateFormatValue(
                type,
                value
            ) else type.defaultValue
        }
    }
}
