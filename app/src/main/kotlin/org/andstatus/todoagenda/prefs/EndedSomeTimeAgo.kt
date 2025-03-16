package org.andstatus.todoagenda.prefs

import org.andstatus.todoagenda.prefs.EndedSomeTimeAgo.entries
import org.andstatus.todoagenda.util.MyClock
import org.joda.time.DateTime

enum class EndedSomeTimeAgo(
    private val value: String,
    private val hoursAgo: Int,
) {
    NONE("NONE", 0),
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    TODAY("TODAY", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday()
    },
    YESTERDAY("YESTERDAY", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusDays(1)
    },
    ONE_WEEK("ONE_WEEK", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusDays(7)
    },
    TWO_WEEKS("TWO_WEEKS", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusDays(14)
    },
    ONE_MONTH("ONE_MONTH", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusMonths(1)
    },
    TWO_MONTHS("TWO_MONTHS", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusMonths(2)
    },
    THREE_MONTHS("THREE_MONTHS", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusMonths(3)
    },
    SIX_MONTHS("SIX_MONTHS", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusMonths(6)
    },
    ONE_YEAR("ONE_YEAR", 0) {
        override fun endedAt(clock: MyClock): DateTime? = clock.startOfToday().minusYears(1)
    }, ;

    open fun endedAt(clock: MyClock): DateTime? = clock.now().minusHours(hoursAgo)

    fun save(): String = value

    companion object {
        val defaultValue = TWO_HOURS

        fun fromValue(valueIn: String?): EndedSomeTimeAgo {
            for (item in entries) {
                if (item.value == valueIn) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
