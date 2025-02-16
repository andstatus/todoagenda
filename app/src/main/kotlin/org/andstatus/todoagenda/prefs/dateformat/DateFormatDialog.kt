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
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.PreferenceDialogFragmentCompat
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.MyLocale.APP_DEFAULT_LOCALE
import org.joda.time.DateTime
import java.text.ParseException
import java.text.SimpleDateFormat

class DateFormatDialog(
    private val preference: DateFormatPreference,
) : PreferenceDialogFragmentCompat(),
    OnItemSelectedListener,
    View.OnKeyListener,
    TextWatcher {
    private var typeSpinner: Spinner? = null
    private var customPatternText: EditText? = null
    private var sampleDateText: EditText? = null
    private var resultText: TextView? = null
    private val sampleDateFormatValue: DateFormatValue =
        DateFormatValue.of(DateFormatType.CUSTOM, "yyyy-MM-dd")

    init {
        val b = Bundle()
        b.putString(ARG_KEY, preference.key)
        arguments = b
    }

    override fun onCreateDialogView(context: Context): View {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dateformat_preference, null) as LinearLayout
        typeSpinner =
            dialogView.findViewById<Spinner?>(R.id.date_format_type).also {
                val adapter: ArrayAdapter<CharSequence> =
                    ArrayAdapter<CharSequence>(
                        context,
                        android.R.layout.simple_spinner_item,
                        DateFormatType.getSpinnerEntryList(context),
                    )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                it.setAdapter(adapter)
                it.setSelection(preference.value.type.spinnerPosition)
                it.setOnItemSelectedListener(this)
            }
        customPatternText =
            dialogView.findViewById<EditText?>(R.id.custom_pattern).apply {
                setText(preference.value.pattern)
                addTextChangedListener(this@DateFormatDialog)
            }
        sampleDateText =
            dialogView.findViewById<EditText?>(R.id.sample_date).apply {
                setText(getSampleDateText())
                addTextChangedListener(this@DateFormatDialog)
            }
        resultText = dialogView.findViewById(R.id.result)
        return dialogView
    }

    private fun getSampleDateText(): CharSequence =
        DateFormatter(context, sampleDateFormatValue, settings.clock.now())
            .formatDate(settings.clock.now())

    override fun onResume() {
        super.onResume()
        calcResult()
    }

    // Two methods to listen for the Spinner changes
    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long,
    ) {
        if (value.type.hasPattern()) {
            customPatternText!!.setText(value.type.pattern)
        } else if (!value.hasPattern() && value.type.isCustomPattern) {
            customPatternText!!.setText(DateFormatType.DEFAULT_EXAMPLE.pattern)
        }
        calcResult()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        calcResult()
    }

    // Four methods to listen to the Custom pattern text changes
    override fun beforeTextChanged(
        s: CharSequence,
        start: Int,
        count: Int,
        after: Int,
    ) {}

    override fun onTextChanged(
        s: CharSequence,
        start: Int,
        before: Int,
        count: Int,
    ) {}

    override fun afterTextChanged(s: Editable) {
        calcResult()
    }

    override fun onKey(
        v: View,
        keyCode: Int,
        event: KeyEvent,
    ): Boolean = false

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value = value.toSave()
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }
    }

    private val value: DateFormatValue
        get() {
            val position = typeSpinner!!.selectedItemPosition
            if (position >= 0) {
                val selectedType = DateFormatType.entries[position]
                return DateFormatValue.of(selectedType, customPatternText!!.text.toString())
            }
            return DateFormatType.UNKNOWN.defaultValue
        }

    private fun calcResult() {
        val dateFormatValue = value
        val sampleFormat = sampleDateFormat
        var result: CharSequence?
        try {
            if (customPatternText!!.isEnabled != dateFormatValue.type.isCustomPattern) {
                customPatternText!!.isEnabled = dateFormatValue.type.isCustomPattern
            }
            val sampleDate = sampleFormat.parse(sampleDateText!!.text.toString())
            result =
                if (sampleDate == null) {
                    "null"
                } else {
                    DateFormatter(this.context, dateFormatValue, settings.clock.now())
                        .formatDate(DateTime(sampleDate.time, settings.timeZone))
                }
        } catch (e: ParseException) {
            result = e.localizedMessage
        }
        resultText!!.text = result
    }

    private val sampleDateFormat: SimpleDateFormat
        get() = SimpleDateFormat(sampleDateFormatValue.pattern, APP_DEFAULT_LOCALE)
    private val settings: InstanceSettings
        get() {
            val widgetId = ApplicationPreferences.getWidgetId(activity)
            return AllSettings.instanceFromId(requireActivity(), widgetId)
        }
}
