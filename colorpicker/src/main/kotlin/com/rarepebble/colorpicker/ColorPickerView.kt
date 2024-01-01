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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout

class ColorPickerView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : FrameLayout(
    context!!, attrs
) {
    private val alphaView: AlphaView
    private val hexEdit: EditText
    private val observableColor = ObservableColor(0)
    private val swatchView: SwatchView

    init {
        LayoutInflater.from(context).inflate(R.layout.picker, this)
        swatchView = findViewById<View>(R.id.swatchView) as SwatchView
        swatchView.observeColor(observableColor)
        val hueSatView = findViewById<View>(R.id.hueSatView) as HueSatView
        hueSatView.observeColor(observableColor)
        val valueView = findViewById<View>(R.id.valueView) as ValueView
        valueView.observeColor(observableColor)
        alphaView = findViewById<View>(R.id.alphaView) as AlphaView
        alphaView.observeColor(observableColor)
        hexEdit = findViewById<View>(R.id.hexEdit) as EditText
        HexEdit.setUpListeners(hexEdit, observableColor)
        applyAttributes(attrs)

        // We get all our state saved and restored for free,
        // thanks to the EditText and its listeners!
    }

    private fun applyAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ColorPicker, 0, 0)
            showAlpha(a.getBoolean(R.styleable.ColorPicker_colorpicker_showAlpha, true))
            showHex(a.getBoolean(R.styleable.ColorPicker_colorpicker_showHex, true))
            showPreview(a.getBoolean(R.styleable.ColorPicker_colorpicker_showPreview, true))
        }
    }

    var color: Int
        /** Returns the color selected by the user  */
        get() = observableColor.color
        /** Sets the original color swatch and the current color to the specified value.  */
        set(color) {
            setOriginalColor(color)
            setCurrentColor(color)
        }

    /** Sets the original color swatch without changing the current color.  */
    fun setOriginalColor(color: Int) {
        swatchView.setOriginalColor(color)
    }

    /** Updates the current color without changing the original color swatch.  */
    fun setCurrentColor(color: Int) {
        observableColor.updateColor(color, null)
    }

    fun showAlpha(showAlpha: Boolean) {
        alphaView.visibility = if (showAlpha) VISIBLE else GONE
        HexEdit.setShowAlphaDigits(hexEdit, showAlpha)
    }

    fun addColorObserver(observer: ColorObserver) {
        observableColor.addObserver(observer)
    }

    fun showHex(showHex: Boolean) {
        hexEdit.visibility = if (showHex) VISIBLE else GONE
    }

    fun showPreview(showPreview: Boolean) {
        swatchView.visibility = if (showPreview) VISIBLE else GONE
    }
}
