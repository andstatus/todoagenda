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
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

abstract class SliderViewBase(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val borderPaint: Paint?
    private val checkerPaint: Paint?
    private val viewRect = Rect()
    private var w = 0
    private var h = 0
    private val borderPath: Path
    private var bitmap: Bitmap? = null
    private val pointerPath: Path?
    private val pointerPaint: Paint?
    private var currentPos = 0f

    init {
        checkerPaint = Resources.makeCheckerPaint(context)
        borderPaint = Resources.makeLinePaint(context)
        pointerPaint = Resources.makeLinePaint(context)
        pointerPath = Resources.makePointerPath(context)
        borderPath = Path()
    }

    protected abstract fun notifyListener(currentPos: Float)
    protected abstract fun makeBitmap(w: Int, h: Int): Bitmap?
    protected abstract fun getPointerColor(currentPos: Float): Int
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.w = w
        this.h = h
        viewRect[0, 0, w] = h
        val inset = borderPaint!!.strokeWidth / 2
        borderPath.reset()
        borderPath.addRect(RectF(inset, inset, w - inset, h - inset), Path.Direction.CW)
        updateBitmap()
    }

    protected fun setPos(pos: Float) {
        currentPos = pos
        optimisePointerColor()
    }

    protected fun updateBitmap() {
        if (w > 0 && h > 0) {
            bitmap = makeBitmap(w, h)
            optimisePointerColor()
        }
        // else not ready yet
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                currentPos = valueForTouchPos(event.x, event.y)
                optimisePointerColor()
                notifyListener(currentPos)
                invalidate()
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(borderPath, checkerPaint!!)
        canvas.drawBitmap(bitmap!!, null, viewRect, null)
        canvas.drawPath(borderPath, borderPaint!!)
        canvas.save()
        if (isWide) {
            canvas.translate(w * currentPos, (h / 2).toFloat())
        } else {
            canvas.translate((w / 2).toFloat(), h * (1 - currentPos))
        }
        canvas.drawPath(pointerPath!!, pointerPaint!!)
        canvas.restore()
    }

    private val isWide: Boolean
        private get() = w > h

    private fun valueForTouchPos(x: Float, y: Float): Float {
        val `val` = if (isWide) x / w else 1 - y / h
        return Math.max(0f, Math.min(1f, `val`))
    }

    private fun optimisePointerColor() {
        pointerPaint!!.color = getPointerColor(currentPos)
    }
}
