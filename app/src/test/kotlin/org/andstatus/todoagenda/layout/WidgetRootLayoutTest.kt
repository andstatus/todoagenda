package org.andstatus.todoagenda.layout

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class WidgetRootLayoutTest : AnnotationSpec() {
    @Test
    fun eachHeaderShapeUsesItsOwnRootLayout() {
        for (isScrollable in listOf(true, false)) {
            withClue("scrollable=$isScrollable") {
                val roots = WidgetHeaderLayout.entries.map { WidgetRootLayout.forWidget(isScrollable, it) }
                roots.toSet().size shouldBe WidgetHeaderLayout.entries.size
            }
        }
    }

    @Test
    fun allRootLayoutIdsAreUnique() {
        val layoutIds = WidgetRootLayout.entries.map { it.layoutId }
        withClue("All root layout ids") {
            layoutIds.toSet().size shouldBe WidgetRootLayout.entries.size
        }
    }

    @Test
    fun rootLayoutMatchesListAndHeaderShape() {
        withClue("scrollable one-row") {
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.ONE_ROW) shouldBe
                WidgetRootLayout.SCROLLABLE_HEADER_ONE_ROW
        }
        withClue("scrollable two-rows") {
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.TWO_ROWS) shouldBe
                WidgetRootLayout.SCROLLABLE_HEADER_TWO_ROWS
        }
        withClue("scrollable hidden") {
            WidgetRootLayout.forWidget(true, WidgetHeaderLayout.HIDDEN) shouldBe
                WidgetRootLayout.SCROLLABLE_NO_HEADER
        }
        withClue("non-scrollable one-row") {
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.ONE_ROW) shouldBe
                WidgetRootLayout.NON_SCROLLABLE_HEADER_ONE_ROW
        }
        withClue("non-scrollable two-rows") {
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.TWO_ROWS) shouldBe
                WidgetRootLayout.NON_SCROLLABLE_HEADER_TWO_ROWS
        }
        withClue("non-scrollable hidden") {
            WidgetRootLayout.forWidget(false, WidgetHeaderLayout.HIDDEN) shouldBe
                WidgetRootLayout.NON_SCROLLABLE_NO_HEADER
        }
    }
}
