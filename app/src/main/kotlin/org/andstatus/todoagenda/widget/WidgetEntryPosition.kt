package org.andstatus.todoagenda.widget

/**
 * On special positions see https://github.com/plusonelabs/calendar-widget/issues/356#issuecomment-559910887
 * @author yvolk@yurivolkov.com
 */
enum class WidgetEntryPosition(
    val value: String,
    val entryDateIsRequired: Boolean,
    val globalOrder: Int,
    val sameDayOrder: Int,
) {
    PAST_AND_DUE_HEADER("PastAndDueHeader", false, 1, 1),
    PAST_AND_DUE("PastAndDue", false, 2, 2),
    DAY_HEADER("DayHeader", true, 3, 1),
    START_OF_TODAY("StartOfToday", false, 3, 2),
    START_OF_DAY("StartOfDay", true, 3, 3),
    ENTRY_DATE("EntryDate", true, 3, 4),
    END_OF_DAY("EndOfDay", true, 3, 5),
    END_OF_TODAY("EndOfToday", false, 3, 6),
    END_OF_LIST_HEADER("EndOfListHeader", false, 5, 1),
    END_OF_LIST("EndOfList", false, 5, 1),
    LIST_FOOTER("ListFooter", false, 6, 1),
    HIDDEN("Hidden", false, 9, 9),
    UNKNOWN("Unknown", false, 9, 9),
    ;

    companion object {
        var defaultValue = UNKNOWN

        fun fromValue(value: String): WidgetEntryPosition {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
