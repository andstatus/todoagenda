package org.andstatus.todoagenda.prefs.colors;

import androidx.annotation.ColorInt;

/**
 * @author yvolk@yurivolkov.com
 */
public class ShadingAndColor {
    public final Shading shading;
    final double luminance;
    @ColorInt
    public final int color;

    ShadingAndColor(Shading shading) {
        this(shading, colorToLuminance(shading.titleColor), shading.titleColor);
    }

    ShadingAndColor(int color) {
        this(colorToLuminance(color), color);
    }

    private ShadingAndColor(double luminance, int color) {
        this(luminanceToShading(luminance), luminance, color);
    }

    private ShadingAndColor(Shading shading, double luminance, int color) {
        this.shading = shading;
        this.luminance = luminance;
        this.color = color;
    }

    private static Shading luminanceToShading(double luminance) {
        // And this is my own guess
        if (luminance >= 0.70) {
            return Shading.WHITE;
        } else if (luminance >= 0.5) {
            return Shading.LIGHT;
        } else if (luminance >= 0.30) {
            return Shading.DARK;
        } else {
            return Shading.BLACK;
        }
    }

    private static double colorToLuminance(@ColorInt int color) {
        float r = ((color >> 16) & 0xff) / 255.0f;
        float g = ((color >>  8) & 0xff) / 255.0f;
        float b = ((color      ) & 0xff) / 255.0f;

        // The formula is from https://stackoverflow.com/a/596243/297710
        return Math.sqrt(0.299 * r * r + 0.587 * g * g + 0.114 * b * b);
    }
}
