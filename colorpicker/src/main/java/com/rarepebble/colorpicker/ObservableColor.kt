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

class ObservableColor(color: Int) {
    // Store as HSV & A, otherwise round-trip to int causes color drift.
    private val hsv = floatArrayOf(0f, 0f, 0f)
    var alpha: Int
        private set
    private val observers: MutableList<ColorObserver> = ArrayList()

    init {
        Color.colorToHSV(color, hsv)
        alpha = Color.alpha(color)
    }

    fun getHsv(hsvOut: FloatArray) {
        hsvOut[0] = hsv[0]
        hsvOut[1] = hsv[1]
        hsvOut[2] = hsv[2]
    }

    val color: Int
        get() = Color.HSVToColor(alpha, hsv)
    val hue: Float
        get() = hsv[0]
    val sat: Float
        get() = hsv[1]
    val value: Float
        get() = hsv[2]
    val lightness: Float
        get() = getLightnessWithValue(hsv[2])

    fun getLightnessWithValue(value: Float): Float {
        val hsV = floatArrayOf(hsv[0], hsv[1], value)
        val color = Color.HSVToColor(hsV)
        return (Color.red(color) * 0.2126f + Color.green(color) * 0.7152f + Color.blue(color) * 0.0722f) / 0xff
    }

    fun addObserver(observer: ColorObserver) {
        observers.add(observer)
    }

    fun updateHueSat(hue: Float, sat: Float, sender: ColorObserver?) {
        hsv[0] = hue
        hsv[1] = sat
        notifyOtherObservers(sender)
    }

    fun updateValue(value: Float, sender: ColorObserver?) {
        hsv[2] = value
        notifyOtherObservers(sender)
    }

    fun updateAlpha(alpha: Int, sender: ColorObserver?) {
        this.alpha = alpha
        notifyOtherObservers(sender)
    }

    fun updateColor(color: Int, sender: ColorObserver?) {
        Color.colorToHSV(color, hsv)
        alpha = Color.alpha(color)
        notifyOtherObservers(sender)
    }

    private fun notifyOtherObservers(sender: ColorObserver?) {
        for (observer in observers) {
            if (observer !== sender) {
                observer.updateColor(this)
            }
        }
    }
}
