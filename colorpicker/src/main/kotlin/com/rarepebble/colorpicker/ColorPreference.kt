/*
 * Copyright (C) 2015 Martin Stone
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
package com.rarepebble.colorpicker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder

class ColorPreference
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : DialogPreference(context, attrs) {
        var selectNoneButtonText: String? = null
        var defaultColor: Int? = null
        private var noneSelectedSummaryText: String? = null
        var sampleText1: String? = "5_ABC"
        var sampleText2: String? = "Abcde"
        private var sampleTextColor1: Int? = null
        private var sampleTextColor2: Int? = null
        private val summaryText: CharSequence?
        var showAlpha = false
        var showHex = false
        var showPreview = false
        private var thumbnail: View? = null
        private val mPicker: ColorPickerView? = null

        init {
            summaryText = super.getSummary()
            if (attrs != null) {
                val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ColorPicker, 0, 0)
                selectNoneButtonText = a.getString(R.styleable.ColorPicker_colorpicker_selectNoneButtonText)
                noneSelectedSummaryText = a.getString(R.styleable.ColorPicker_colorpicker_noneSelectedSummaryText)
                showAlpha = a.getBoolean(R.styleable.ColorPicker_colorpicker_showAlpha, true)
                showHex = a.getBoolean(R.styleable.ColorPicker_colorpicker_showHex, true)
                showPreview = a.getBoolean(R.styleable.ColorPicker_colorpicker_showPreview, true)
            } else {
                selectNoneButtonText = null
                noneSelectedSummaryText = null
                showAlpha = true
                showHex = true
                showPreview = true
            }
        }

        override fun onBindViewHolder(viewHolder: PreferenceViewHolder) {
            thumbnail = addThumbnail(viewHolder.itemView)
            showColor()
            // Only call after showColor sets any summary text:
            super.onBindViewHolder(viewHolder)
            val textView = viewHolder.findViewById(android.R.id.title) as? TextView?
            if (textView != null) {
                textView.isSingleLine = false
            }
        }

        override fun onGetDefaultValue(
            a: TypedArray,
            index: Int,
        ): Any? {
            defaultColor = readDefaultValue(a, index)
            return defaultColor
        }

        override fun setDefaultValue(defaultValue: Any) {
            super.setDefaultValue(defaultValue)
            defaultColor = parseDefaultValue(defaultValue)
        }

        @Deprecated("Deprecated in Java")
        override fun onSetInitialValue(
            restorePersistedValue: Boolean,
            defaultValue: Any?,
        ) {
            color = if (restorePersistedValue) color else parseDefaultValue(defaultValue)
        }

        private fun addThumbnail(view: View): View {
            val widgetFrameView = view.findViewById<View>(android.R.id.widget_frame) as LinearLayout
            widgetFrameView.visibility = View.VISIBLE
            widgetFrameView.removeAllViews()
            LayoutInflater.from(context).inflate(
                if (isEnabled) R.layout.color_preference_thumbnail else R.layout.color_preference_thumbnail_disabled,
                widgetFrameView,
            )
            return widgetFrameView.findViewById(R.id.thumbnail)
        }

        private val persistedColorOrDefaultOrNull: Int
            private get() =
                if (shouldPersist() &&
                    sharedPreferences!!.contains(key)
                ) {
                    Integer.valueOf(getPersistedInt(Color.GRAY))
                } else {
                    defaultColor!!
                }

        private fun showColor(color: Int? = persistedColorOrDefaultOrNull) {
            val thumbColor = color ?: defaultColor
            if (thumbnail != null) {
                thumbnail!!.visibility = if (thumbColor == null) View.GONE else View.VISIBLE
                thumbnail!!.findViewById<View>(R.id.colorPreview).setBackgroundColor(thumbColor ?: 0)
                if (sampleTextColor1 != null && sampleText1 != null) {
                    val textPreview =
                        thumbnail!!.findViewById<TextView>(if (sampleTextColor2 == null) R.id.textPreview else R.id.textPreviewUpper)
                    if (textPreview != null) {
                        textPreview.text = sampleText1
                        textPreview.setTextColor(sampleTextColor1!!)
                    }
                    if (sampleTextColor2 != null && sampleText2 != null) {
                        val textPreview2 = thumbnail!!.findViewById<TextView>(R.id.textPreviewLower)
                        if (textPreview2 != null) {
                            textPreview2.text = sampleText2
                            textPreview2.setTextColor(sampleTextColor2!!)
                        }
                    }
                }
            }
            if (noneSelectedSummaryText != null) {
                summary = if (thumbColor == null) noneSelectedSummaryText else summaryText
            }
        }

        private fun removeSetting() {
            if (shouldPersist()) {
                sharedPreferences
                    ?.edit()
                    ?.remove(key)
                    ?.apply()
            }
        }

        var color: Int?
            get() = persistedColorOrDefaultOrNull
            set(color) {
                color?.let { persistInt(it) } ?: removeSetting()
                showColor(color)
            }

        fun setSampleTextColor1(textColor: Int?) {
            sampleTextColor1 = textColor
            showColor()
        }

        fun setSampleTextColor2(textColor: Int?) {
            sampleTextColor2 = textColor
            showColor()
        }

        companion object {
            private fun readDefaultValue(
                a: TypedArray,
                index: Int,
            ): Int? {
                if (a.peekValue(index) != null) {
                    val type = a.peekValue(index).type
                    if (type == TypedValue.TYPE_STRING) {
                        return Color.parseColor(standardiseColorDigits(a.getString(index)))
                    } else if (TypedValue.TYPE_FIRST_COLOR_INT <= type && type <= TypedValue.TYPE_LAST_COLOR_INT) {
                        return a.getColor(index, Color.GRAY)
                    } else if (TypedValue.TYPE_FIRST_INT <= type && type <= TypedValue.TYPE_LAST_INT) {
                        return a.getInt(index, Color.GRAY)
                    }
                }
                return null
            }

            private fun parseDefaultValue(defaultValue: Any?): Int =
                if (defaultValue == null) {
                    Color.GRAY
                } else {
                    (
                        if (defaultValue is Int) {
                            defaultValue
                        } else {
                            Color.parseColor(
                                standardiseColorDigits(defaultValue.toString()),
                            )
                        }
                    )!!
                }

            private fun standardiseColorDigits(s: String?): String? =
                if (s!![0] == '#' && s.length <= "#argb".length) {
                    // Convert #[a]rgb to #[aa]rrggbb
                    var ss = "#"
                    for (i in 1 until s.length) {
                        ss += s[i]
                        ss += s[i]
                    }
                    ss
                } else {
                    s
                }
        }
    }
