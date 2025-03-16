package org.andstatus.todoagenda.layout

enum class TimeSection(
    val preferenceCategoryKey: String,
) {
    PAST("PastTime"),
    ONGOING("OngoingTime"),
    TODAY("TodayTime"),
    FUTURE("FutureTime"),
    ALL("AllTime"),
    ;

    fun <T> select(
        past: T,
        ongoing: T,
        today: T,
        future: T,
    ): T =
        when (this) {
            PAST -> past
            ONGOING -> ongoing
            TODAY -> today
            FUTURE, ALL -> future
        }
}
