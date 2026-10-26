package org.andstatus.todoagenda.layout

import androidx.annotation.LayoutRes
import org.andstatus.todoagenda.R

/**
 * Root layout of a widget.
 *
 * The root layout depends on the list type and on the header shape. Structural header changes
 * must change the root layout id, so that a launcher fully re-inflates the widget instead of
 * replaying `addView`/`removeAllViews` actions on an existing view hierarchy.
 *
 * @author yvolk@yurivolkov.com
 */
enum class WidgetRootLayout(
    @field:LayoutRes @param:LayoutRes val layoutId: Int,
) {
    SCROLLABLE_HEADER_ONE_ROW(R.layout.widget_scrollable),
    SCROLLABLE_HEADER_TWO_ROWS(R.layout.widget_scrollable_two_rows),
    SCROLLABLE_NO_HEADER(R.layout.widget_scrollable_no_header),
    NON_SCROLLABLE_HEADER_ONE_ROW(R.layout.widget_non_scrollable),
    NON_SCROLLABLE_HEADER_TWO_ROWS(R.layout.widget_non_scrollable_two_rows),
    NON_SCROLLABLE_NO_HEADER(R.layout.widget_non_scrollable_no_header),
    ;

    companion object {
        fun forWidget(
            isScrollable: Boolean,
            headerLayout: WidgetHeaderLayout,
        ): WidgetRootLayout =
            when (headerLayout) {
                WidgetHeaderLayout.ONE_ROW ->
                    if (isScrollable) SCROLLABLE_HEADER_ONE_ROW else NON_SCROLLABLE_HEADER_ONE_ROW

                WidgetHeaderLayout.TWO_ROWS ->
                    if (isScrollable) SCROLLABLE_HEADER_TWO_ROWS else NON_SCROLLABLE_HEADER_TWO_ROWS

                WidgetHeaderLayout.HIDDEN ->
                    if (isScrollable) SCROLLABLE_NO_HEADER else NON_SCROLLABLE_NO_HEADER
            }
    }
}
