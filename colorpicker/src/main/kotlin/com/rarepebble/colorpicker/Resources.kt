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
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.TypedValue

internal object Resources {
    private const val LINE_WIDTH_DIP = 1.5f
    private const val POINTER_RADIUS_DIP = 7f
    private const val VIEW_OUTLINE_COLOR = -0x7f7f80
    fun makeLinePaint(context: Context): Paint {
        val paint = Paint()
        paint.color = VIEW_OUTLINE_COLOR
        paint.strokeWidth =
            dipToPixels(context, LINE_WIDTH_DIP)
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        return paint
    }

    fun makeCheckerPaint(context: Context): Paint {
        val paint = Paint()
        val checkerBmp = BitmapFactory.decodeResource(context.resources, R.drawable.checker_background)
        paint.setShader(BitmapShader(checkerBmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT))
        paint.strokeWidth =
            dipToPixels(context, LINE_WIDTH_DIP)
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        return paint
    }

    fun makePointerPath(context: Context): Path {
        val pointerPath = Path()
        val radiusPx = dipToPixels(context, POINTER_RADIUS_DIP)
        pointerPath.addCircle(0f, 0f, radiusPx, Path.Direction.CW)
        return pointerPath
    }

    fun dipToPixels(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }
}
