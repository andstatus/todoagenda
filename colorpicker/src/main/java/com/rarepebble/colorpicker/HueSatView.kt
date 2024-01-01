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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent

class HueSatView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SquareView(context, attrs),
    ColorObserver {
    private val borderPaint: Paint?
    private val pointerPaint: Paint?
    private val pointerPath: Path?
    private val borderPath: Path
    private val viewRect = Rect()
    private var w = 0
    private var h = 0
    private val pointer = PointF()
    private var observableColor = ObservableColor(0)

    init {
        borderPaint = Resources.makeLinePaint(context)
        pointerPaint = Resources.makeLinePaint(context)
        pointerPaint.setColor(-0x1000000)
        pointerPath = Resources.makePointerPath(context)
        borderPath = Path()
        if (bitmap == null) {
            bitmap = makeBitmap(optimalBitmapSize())
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // Need clipPath() and setLayerType()...
            throw UnsupportedOperationException("Android API 10 and below is not supported.")
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // clipPath() is only supported on a software layer.
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun optimalBitmapSize(): Int {
        val scale = 2
        val maxBitmapSize = 128
        val dm = resources.displayMetrics
        return Math.min(maxBitmapSize, Math.min(dm.widthPixels, dm.heightPixels) / scale)
    }

    fun observeColor(observableColor: ObservableColor) {
        this.observableColor = observableColor
        observableColor.addObserver(this)
    }

    override fun updateColor(observableColor: ObservableColor) {
        setPointer(pointer, observableColor.hue, observableColor.sat, w.toFloat())
        optimisePointerColor()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        viewRect[0, 0, w] = h
        val inset = borderPaint!!.strokeWidth / 2
        makeBorderPath(borderPath, w, h, inset)

        // Sets pointer position
        updateColor(observableColor)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val withinPicker = clamp(pointer, event.x, event.y, true)
                if (withinPicker) update()
                return withinPicker
            }

            MotionEvent.ACTION_MOVE -> {
                clamp(pointer, event.x, event.y, false)
                update()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun update() {
        observableColor.updateHueSat(
            hueForPos(pointer.x, pointer.y, w.toFloat()),
            satForPos(pointer.x, pointer.y, w.toFloat()),
            this
        )
        optimisePointerColor()
        invalidate()
    }

    private fun clamp(pointer: PointF, x: Float, y: Float, rejectOutside: Boolean): Boolean {
        var x = x
        var y = y
        x = Math.min(x, w.toFloat())
        y = Math.min(y, h.toFloat())
        val dx = w - x
        val dy = h - y
        val r = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val outside = r > w
        if (!outside || !rejectOutside) {
            if (outside) {
                x = w - dx * w / r
                y = w - dy * w / r
            }
            pointer[x] = y
        }
        return !outside
    }

    private fun optimisePointerColor() {
        pointerPaint!!.color = if (observableColor.getLightnessWithValue(1f) > 0.5) -0x1000000 else -0x1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.clipPath(borderPath)
        canvas.drawBitmap(bitmap!!, null, viewRect, null)
        canvas.translate(pointer.x, pointer.y)
        canvas.drawPath(pointerPath!!, pointerPaint!!)
        canvas.restore()
        canvas.drawPath(borderPath, borderPaint!!)
    }

    companion object {
        // Bitmap is generated once and shared across instances.
        private var bitmap: Bitmap? = null
        private fun makeBorderPath(borderPath: Path, w: Int, h: Int, inset: Float) {
            var w = w
            var h = h
            w = (w - inset).toInt()
            h = (h - inset).toInt()
            borderPath.reset()
            borderPath.moveTo(w.toFloat(), inset)
            borderPath.lineTo(w.toFloat(), h.toFloat())
            borderPath.lineTo(inset, h.toFloat())
            borderPath.addArc(RectF(inset, inset, (2 * w).toFloat(), (2 * h).toFloat()), 180f, 270f)
            borderPath.close()
        }

        private fun makeBitmap(radiusPx: Int): Bitmap {
            val colors = IntArray(radiusPx * radiusPx)
            val hsv = floatArrayOf(0f, 0f, 1f)
            for (y in 0 until radiusPx) {
                for (x in 0 until radiusPx) {
                    val i = x + y * radiusPx
                    val sat = satForPos(x.toFloat(), y.toFloat(), radiusPx.toFloat())
                    val arcBleed = 2f / radiusPx // extend curved edge pixels just outside clip area.
                    if (sat <= 1 + arcBleed) {
                        hsv[0] = hueForPos(x.toFloat(), y.toFloat(), radiusPx.toFloat())
                        hsv[1] = sat
                        colors[i] = Color.HSVToColor(0xff, hsv)
                    }
                }
            }
            return Bitmap.createBitmap(colors, radiusPx, radiusPx, Bitmap.Config.ARGB_8888)
        }

        private fun hueForPos(x: Float, y: Float, radiusPx: Float): Float {
            val r = (radiusPx - 1).toDouble() // gives values 0...1 inclusive
            val dx = (r - x) / r
            val dy = (r - y) / r
            val angle = Math.atan2(dy, dx)
            val hue = 360 * angle / (Math.PI / 2)
            return hue.toFloat()
        }

        private fun satForPos(x: Float, y: Float, radiusPx: Float): Float {
            val r = (radiusPx - 1).toDouble() // gives values 0...1 inclusive
            val dx = (r - x) / r
            val dy = (r - y) / r
            val sat = dx * dx + dy * dy // leave it squared -- exaggerates pale colors
            return sat.toFloat()
        }

        private fun setPointer(pointer: PointF, hue: Float, sat: Float, radiusPx: Float) {
            val r = radiusPx - 1 // for values 0...1 inclusive
            val distance = r * Math.sqrt(sat.toDouble())
            val angle = hue / 360 * Math.PI / 2
            val dx = distance * Math.cos(angle)
            val dy = distance * Math.sin(angle)
            pointer[r - dx.toFloat()] = r - dy.toFloat()
        }
    }
}
