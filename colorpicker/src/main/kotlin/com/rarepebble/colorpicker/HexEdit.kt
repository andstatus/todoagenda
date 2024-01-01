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

import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText

internal object HexEdit {
    private val withoutAlphaDigits = arrayOf<InputFilter>(ColorPasteLengthFilter())
    private val withAlphaDigits = arrayOf<InputFilter>(LengthFilter(8))
    fun setUpListeners(hexEdit: EditText, observableColor: ObservableColor) {
        class MultiObserver : ColorObserver, TextWatcher {
            override fun updateColor(observableColor: ObservableColor) {
                val colorString = formatColor(observableColor.color)
                // Prevent onTextChanged getting called when we update text programmatically
                hexEdit.removeTextChangedListener(this)
                hexEdit.setText(colorString)
                hexEdit.addTextChangedListener(this)
            }

            private fun formatColor(color: Int): String {
                return if (shouldTrimAlphaDigits()) String.format(
                    "%06x",
                    color and 0x00ffffff
                ) else String.format("%08x", color)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var color = parseHexColor(s)
                if (shouldTrimAlphaDigits()) color = color or -0x1000000
                observableColor.updateColor(color, this)
            }

            override fun afterTextChanged(s: Editable) {}
            private fun shouldTrimAlphaDigits(): Boolean {
                return hexEdit.filters == withoutAlphaDigits
            }
        }

        val multiObserver = MultiObserver()
        hexEdit.addTextChangedListener(multiObserver)
        observableColor.addObserver(multiObserver)
        setShowAlphaDigits(hexEdit, true)
    }

    private fun parseHexColor(s: CharSequence): Int {
        return try {
            (s.toString().toLong(16) and 0xffffffffL).toInt()
        } catch (e: NumberFormatException) {
            Color.GRAY
        }
    }

    fun setShowAlphaDigits(hexEdit: EditText, showAlphaDigits: Boolean) {
        hexEdit.filters = if (showAlphaDigits) withAlphaDigits else withoutAlphaDigits
        hexEdit.text = hexEdit.text // trigger a reformat of text
    }

    private class ColorPasteLengthFilter : InputFilter {
        private val sixDigitFilter: InputFilter = LengthFilter(MAX_LENGTH)
        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence {
            // If 8 digits have been pasted, replacing all source, trim alpha digits.
            // Otherwise standard LengthFilter behavior.
            val srcLength = end - start
            val dstSelLength = dend - dstart
            return if (srcLength == PASTED_LEN && dstSelLength == dest.length) {
                // Discard alpha digits:
                source.subSequence(
                    PASTED_LEN - MAX_LENGTH,
                    PASTED_LEN
                )
            } else {
                sixDigitFilter.filter(source, start, end, dest, dstart, dend)
            }
        }

        companion object {
            private const val MAX_LENGTH = 6
            private const val PASTED_LEN = 8
        }
    }
}
