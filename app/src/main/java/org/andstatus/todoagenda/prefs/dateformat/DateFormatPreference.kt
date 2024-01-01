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
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import androidx.preference.DialogPreference
import org.andstatus.todoagenda.prefs.ApplicationPreferences

class DateFormatPreference : DialogPreference {
    var defaultValue: DateFormatValue = DateFormatType.unknownValue()
    var value: DateFormatValue = DateFormatType.unknownValue()
        get() {
            return if (field.type == DateFormatType.UNKNOWN) defaultValue else field
        }
        set(value) {
            field = value
            ApplicationPreferences.setDateFormat(context, key, value)
            showValue()
        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return if (a.peekValue(index) != null && a.peekValue(index).type == TypedValue.TYPE_STRING) {
            DateFormatValue.load(a.getString(index), DateFormatType.unknownValue())
        } else DateFormatType.unknownValue()
    }

    override fun setDefaultValue(defaultValue: Any) {
        super.setDefaultValue(defaultValue)
        this.defaultValue = DateFormatValue.loadOrUnknown(defaultValue)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = ApplicationPreferences.getDateFormat(context, key, this.defaultValue)
        showValue()
    }

    override fun getSummary(): CharSequence {
        return value.getSummary(context)
    }

    private fun showValue() {
        summary = summary
    }
}
