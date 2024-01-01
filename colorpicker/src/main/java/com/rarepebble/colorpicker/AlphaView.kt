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
import android.graphics.Bitmap
import android.util.AttributeSet

class AlphaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SliderViewBase(context, attrs), ColorObserver {
    private var observableColor = ObservableColor(0)
    fun observeColor(observableColor: ObservableColor) {
        this.observableColor = observableColor
        observableColor.addObserver(this)
    }

    override fun updateColor(observableColor: ObservableColor) {
        setPos(observableColor.alpha.toFloat() / 0xff)
        updateBitmap()
        invalidate()
    }

    override fun notifyListener(currentPos: Float) {
        observableColor.updateAlpha((currentPos * 0xff).toInt(), this)
    }

    override fun getPointerColor(currentPos: Float): Int {
        val solidColorLightness = observableColor.lightness
        val posLightness = 1 + currentPos * (solidColorLightness - 1)
        return if (posLightness > 0.5f) -0x1000000 else -0x1
    }

    override fun makeBitmap(w: Int, h: Int): Bitmap? {
        val isWide = w > h
        val n = Math.max(w, h)
        val color = observableColor.color
        val colors = IntArray(n)
        for (i in 0 until n) {
            val alpha = if (isWide) i.toFloat() / n else 1 - i.toFloat() / n
            colors[i] = color and 0xffffff or ((alpha * 0xff).toInt() shl 24)
        }
        val bmpWidth = if (isWide) w else 1
        val bmpHeight = if (isWide) 1 else h
        return Bitmap.createBitmap(colors, bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
    }
}
