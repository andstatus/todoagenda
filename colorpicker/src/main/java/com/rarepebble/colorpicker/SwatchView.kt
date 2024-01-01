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
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet

class SwatchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : SquareView(context, attrs),
    ColorObserver {
    private val borderPaint: Paint?
    private val borderPath: Path
    private val checkerPaint: Paint?
    private val oldFillPath: Path
    private val newFillPath: Path
    private val oldFillPaint: Paint
    private val newFillPaint: Paint
    private var radialMarginPx = 0f

    init {
        radialMarginPx = if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.SwatchView, 0, 0)
            a.getDimension(R.styleable.SwatchView_radialMargin, 0f)
        } else {
            0f
        }
        borderPaint = Resources.makeLinePaint(context)
        checkerPaint = Resources.makeCheckerPaint(context)
        oldFillPaint = Paint()
        newFillPaint = Paint()
        borderPath = Path()
        oldFillPath = Path()
        newFillPath = Path()
    }

    fun setOriginalColor(color: Int) {
        oldFillPaint.color = color
        invalidate()
    }

    fun observeColor(observableColor: ObservableColor) {
        observableColor.addObserver(this)
    }

    override fun updateColor(observableColor: ObservableColor) {
        newFillPaint.color = observableColor.color
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // We expect to be stacked behind a square HueSatView and fill the top left corner.
        val inset = borderPaint!!.strokeWidth / 2
        val r = Math.min(w, h).toFloat()

        // Trim the corners of the swatch is where it is radialMarginPx thick.
        // find how long the outeredges are:
        val margin = radialMarginPx
        val diagonal = r + 2 * margin
        val opp = Math.sqrt((diagonal * diagonal - r * r).toDouble()).toFloat()
        val edgeLen = r - opp

        // Arc angles for drawing CCW
        val outerAngle = Math.toDegrees(Math.atan2(opp.toDouble(), r.toDouble())).toFloat()
        val startAngle = 270 - outerAngle
        // Sweep angle for each half of the swatch:
        val sweepAngle = outerAngle - 45
        // Sweep angle for the smooth corners:
        val cornerSweepAngle = 90 - outerAngle

        // Outer border:
        beginBorder(borderPath, inset, edgeLen, margin, cornerSweepAngle)
        mainArc(borderPath, r, margin, startAngle, 2 * sweepAngle)
        endBorder(borderPath, inset, edgeLen, margin, cornerSweepAngle)

        // Old fill shape:
        oldFillPath.reset()
        oldFillPath.moveTo(inset, inset)
        mainArc(oldFillPath, r, margin, 225f, sweepAngle)
        endBorder(oldFillPath, inset, edgeLen, margin, cornerSweepAngle)

        // New fill shape:
        beginBorder(newFillPath, inset, edgeLen, margin, cornerSweepAngle)
        mainArc(newFillPath, r, margin, startAngle, sweepAngle)
        newFillPath.lineTo(inset, inset)
        newFillPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(borderPath, checkerPaint!!)
        canvas.drawPath(oldFillPath, oldFillPaint)
        canvas.drawPath(newFillPath, newFillPaint)
        canvas.drawPath(borderPath, borderPaint!!)
    }

    companion object {
        private fun beginBorder(
            path: Path,
            inset: Float,
            edgeLen: Float,
            cornerRadius: Float,
            cornerSweepAngle: Float
        ) {
            path.reset()
            path.moveTo(inset, inset)
            cornerArc(path, edgeLen, inset, cornerRadius - inset, 0f, cornerSweepAngle)
        }

        private fun endBorder(path: Path, inset: Float, edgeLen: Float, cornerRadius: Float, cornerSweepAngle: Float) {
            cornerArc(path, inset, edgeLen, cornerRadius - inset, 90 - cornerSweepAngle, cornerSweepAngle)
            path.lineTo(inset, inset)
            path.close()
        }

        private fun cornerArc(path: Path, cx: Float, cy: Float, r: Float, startAngle: Float, sweepAngle: Float) {
            val ovalRect = RectF(-r, -r, r, r)
            ovalRect.offset(cx, cy)
            path.arcTo(ovalRect, startAngle, sweepAngle)
        }

        private fun mainArc(path: Path, viewSize: Float, margin: Float, startAngle: Float, sweepAngle: Float) {
            val r = viewSize + margin
            val ovalRect = RectF(-r, -r, r, r)
            ovalRect.offset(viewSize, viewSize)
            path.arcTo(ovalRect, startAngle, sweepAngle)
        }
    }
}
