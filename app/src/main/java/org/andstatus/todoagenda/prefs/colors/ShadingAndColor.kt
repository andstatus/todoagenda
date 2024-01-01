package org.andstatus.todoagenda.prefs.colors

import androidx.annotation.ColorInt

/**
 * @author yvolk@yurivolkov.com
 */
class ShadingAndColor private constructor(
    val shading: Shading,
    val luminance: Double,
    @field:ColorInt val color: Int
) {
    internal constructor(shading: Shading) : this(shading, colorToLuminance(shading.titleColor), shading.titleColor)
    internal constructor(color: Int) : this(colorToLuminance(color), color)
    private constructor(luminance: Double, color: Int) : this(luminanceToShading(luminance), luminance, color)

    companion object {
        private fun luminanceToShading(luminance: Double): Shading {
            // And this is my own guess
            return if (luminance >= 0.70) {
                Shading.WHITE
            } else if (luminance >= 0.5) {
                Shading.LIGHT
            } else if (luminance >= 0.30) {
                Shading.DARK
            } else {
                Shading.BLACK
            }
        }

        private fun colorToLuminance(@ColorInt color: Int): Double {
            val r = (color shr 16 and 0xff) / 255.0f
            val g = (color shr 8 and 0xff) / 255.0f
            val b = (color and 0xff) / 255.0f

            // The formula is from https://stackoverflow.com/a/596243/297710
            return Math.sqrt(0.299 * r * r + 0.587 * g * g + 0.114 * b * b)
        }
    }
}
