package org.andstatus.todoagenda.widget

enum class TimeSection(val preferenceCategoryKey: String) {
    PAST("PastTime"),
    TODAY("TodayTime"),
    FUTURE("FutureTime"),
    ALL("AllTime");

    fun <T> select(past: T, today: T, future: T): T {
        return when (this) {
            PAST -> past
            TODAY -> today
            else -> future
        }
    }
}
