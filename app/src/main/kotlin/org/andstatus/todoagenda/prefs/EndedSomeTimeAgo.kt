package org.andstatus.todoagenda.prefs

import org.joda.time.DateTime

enum class EndedSomeTimeAgo(private val value: String, private val hoursAgo: Int) {
    NONE("NONE", 0),
    ONE_HOUR("ONE_HOUR", 1),
    TWO_HOURS("TWO_HOURS", 2),
    FOUR_HOURS("FOUR_HOURS", 4),
    TODAY("TODAY", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay()
        }
    },
    YESTERDAY("YESTERDAY", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusDays(1)
        }
    },
    ONE_WEEK("ONE_WEEK", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusDays(7)
        }
    },
    TWO_WEEKS("TWO_WEEKS", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusDays(14)
        }
    },
    ONE_MONTH("ONE_MONTH", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusMonths(1)
        }
    },
    TWO_MONTHS("TWO_MONTHS", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusMonths(2)
        }
    },
    THREE_MONTHS("THREE_MONTHS", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusMonths(3)
        }
    },
    SIX_MONTHS("SIX_MONTHS", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusMonths(6)
        }
    },
    ONE_YEAR("ONE_YEAR", 0) {
        override fun endedAt(now: DateTime?): DateTime? {
            return now!!.withTimeAtStartOfDay().minusYears(1)
        }
    };

    open fun endedAt(now: DateTime?): DateTime? {
        return now!!.minusHours(hoursAgo)
    }

    fun save(): String {
        return value
    }

    companion object {
        fun fromValue(valueIn: String?): EndedSomeTimeAgo {
            var ended = NONE
            for (item in entries) {
                if (item.value == valueIn) {
                    ended = item
                    break
                }
            }
            return ended
        }
    }
}
