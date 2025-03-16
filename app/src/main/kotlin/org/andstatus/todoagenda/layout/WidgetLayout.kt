package org.andstatus.todoagenda.layout

import androidx.annotation.LayoutRes
import org.andstatus.todoagenda.R

enum class WidgetLayout(
    @field:LayoutRes @param:LayoutRes val noShadowLayoutId: Int,
    @field:LayoutRes @param:LayoutRes val darkShadowLayoutId: Int = noShadowLayoutId,
    @field:LayoutRes @param:LayoutRes val lightShadowLayoutId: Int = noShadowLayoutId,
) {
    CURRENT_TIME_LINE(R.layout.current_time_line),
    DAY_HEADER_SEPARATOR_ABOVE(
        R.layout.day_header_separator_above,
        R.layout.day_header_separator_above_shadow_dark,
        R.layout.day_header_separator_above_shadow_light,
    ),
    DAY_HEADER_SEPARATOR_BELOW(
        R.layout.day_header_separator_below,
        R.layout.day_header_separator_below_shadow_dark,
        R.layout.day_header_separator_below_shadow_light,
    ),
    EVENT_ENTRY_TIME_BELOW_TITLE(
        R.layout.event_entry,
        R.layout.event_entry_shadow_dark,
        R.layout.event_entry_shadow_light,
    ),
    EVENT_ENTRY_ONE_LINE(
        R.layout.event_entry_one_line,
        R.layout.event_entry_one_line_shadow_dark,
        R.layout.event_entry_one_line_shadow_light,
    ),
    ENTRY_LAST(R.layout.entry_last, R.layout.entry_last_shadow_dark, R.layout.entry_last_shadow_light),
    WIDGET_SCROLLABLE(R.layout.widget_scrollable),
    WIDGET_HEADER_ONE_ROW(
        R.layout.widget_header_one_row,
        R.layout.widget_header_one_row_shadow_dark,
        R.layout.widget_header_one_row_shadow_light,
    ),
    WIDGET_HEADER_TWO_ROWS(
        R.layout.widget_header_two_rows,
        R.layout.widget_header_two_rows_shadow_dark,
        R.layout.widget_header_two_rows_shadow_light,
    ),

    ;

    fun shadowed(textShadow: TextShadow?): Int =
        when (textShadow) {
            TextShadow.NO_SHADOW -> noShadowLayoutId
            TextShadow.DARK_SHADOW -> darkShadowLayoutId
            TextShadow.LIGHT_SHADOW -> lightShadowLayoutId
            else -> noShadowLayoutId
        }
}
