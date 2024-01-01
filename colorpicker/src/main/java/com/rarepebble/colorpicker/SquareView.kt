package com.rarepebble.colorpicker

import android.content.Context
import android.util.AttributeSet
import android.view.View

open class SquareView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val minSizePx: Int

    init {
        minSizePx = Resources.dipToPixels(context, MIN_SIZE_DIP.toFloat()).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Constrain to square
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val modeW = MeasureSpec.getMode(widthMeasureSpec)
        val modeH = MeasureSpec.getMode(heightMeasureSpec)
        var size = minSizePx
        size = if (modeW == MeasureSpec.UNSPECIFIED) {
            h
        } else if (modeH == MeasureSpec.UNSPECIFIED) {
            w
        } else {
            Math.min(w, h)
        }
        size = Math.max(size, minSizePx)
        setMeasuredDimension(size, size)
    }

    companion object {
        private const val MIN_SIZE_DIP = 200
    }
}
