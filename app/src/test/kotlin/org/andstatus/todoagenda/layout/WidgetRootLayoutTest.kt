package org.andstatus.todoagenda.layout

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WidgetRootLayoutTest {
    @Test
    fun eachHeaderShapeUsesItsOwnRootLayout() {
        for (isScrollable in listOf(true, false)) {
            val roots = WidgetHeaderLayout.entries.map { WidgetRootLayout.forWidget(isScrollable, it) }
            assertEquals(WidgetHeaderLayout.entries.size, roots.toSet().size)
        }
    }

    @Test
    fun allRootLayoutIdsAreUnique() {
        val layoutIds = WidgetRootLayout.entries.map { it.layoutId }
        assertEquals(WidgetRootLayout.entries.size, layoutIds.toSet().size)
    }

    @Test
    fun rootLayoutMatchesListAndHeaderShape() {
        assertEquals(
            WidgetRootLayout.SCROLLABLE_HEADER_ONE_ROW,
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.ONE_ROW),
        )
        assertEquals(
            WidgetRootLayout.SCROLLABLE_HEADER_TWO_ROWS,
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.TWO_ROWS),
        )
        assertEquals(
            WidgetRootLayout.SCROLLABLE_NO_HEADER,
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.HIDDEN),
        )
        assertEquals(
            WidgetRootLayout.NON_SCROLLABLE_HEADER_ONE_ROW,
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.ONE_ROW),
        )
        assertEquals(
            WidgetRootLayout.NON_SCROLLABLE_HEADER_TWO_ROWS,
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.TWO_ROWS),
        )
        assertEquals(
            WidgetRootLayout.NON_SCROLLABLE_NO_HEADER,
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.HIDDEN),
        )
    }
}
