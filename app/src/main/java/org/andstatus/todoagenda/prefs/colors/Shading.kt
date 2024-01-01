package org.andstatus.todoagenda.prefs.colors;

import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import org.andstatus.todoagenda.R;

/**
 * @author yvolk@yurivolkov.com
*/
public enum Shading {

    // For historical reasons we store theme names for text shadings i.e. "BLACK" theme for WHITE text shading
    WHITE(0xDCFFFFFF, Color.WHITE, Color.WHITE,
            R.string.appearance_theme_black, "BLACK", R.style.Theme_ToDoAgenda_Black),
    LIGHT(0x9AFFFFFF, 0xFFCCCCCC, 0xFFCCCCCC,
          R.string.appearance_theme_dark, "DARK", R.style.Theme_ToDoAgenda_Dark),
    DARK(0x99000000, 0xFF777777, 0xFF555555,
         R.string.appearance_theme_light, "LIGHT", R.style.Theme_ToDoAgenda_Light),
    BLACK(0xCC000000, Color.BLACK, Color.BLACK,
            R.string.appearance_theme_white, "WHITE", R.style.Theme_ToDoAgenda_White);

    @ColorInt
    public final int widgetHeaderColor;
    @ColorInt
    public final int dayHeaderColor;
    @ColorInt
    public final int titleColor;
    @StringRes
    public final int titleResId;
    public final String themeName;
    @StyleRes
    public final int themeResId;

    Shading(int widgetHeaderColor, int dayHeaderColor, int titleColor, int titleResId, String themeName, int themeResId) {
        this.widgetHeaderColor = widgetHeaderColor;
        this.titleColor = titleColor;
        this.dayHeaderColor = dayHeaderColor;
        this.titleResId = titleResId;
        this.themeName = themeName;
        this.themeResId = themeResId;
    }

    public static Shading fromThemeName(String themeName, Shading defaultShading) {
        for (Shading item : Shading.values()) {
            if (item.themeName.equals(themeName)) {
                return item;
            }
        }
        return defaultShading;
    }

}
