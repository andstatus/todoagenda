package org.andstatus.todoagenda.widget

import android.content.Intent
import org.andstatus.todoagenda.RemoteViewsFactory
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.prefs.dateformat.DateFormatType
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.MyClock
import org.joda.time.DateTime
import java.util.concurrent.atomic.AtomicLong

abstract class WidgetEntry<T : WidgetEntry<T>> protected constructor(
    val settings: InstanceSettings,
    val entryPosition: WidgetEntryPosition,
    entryDate: DateTime,
    val allDay: Boolean,
    val endDate: DateTime?
) : Comparable<WidgetEntry<*>> {
    val entryId = idGenerator.incrementAndGet()
    val entryDate: DateTime
    val entryDay: DateTime
    val timeSection: TimeSection

    init {
        this.entryDate = fixEntryDate(entryPosition, entryDate)
        entryDay = calcEntryDay(settings, entryPosition, this.entryDate)
        timeSection = calcTimeSection(settings, entryPosition, entryDay, endDate)
    }

    val isLastEntryOfEvent: Boolean
        get() = endDate == null ||
            !entryPosition.entryDateIsRequired ||
            endDate.isBefore(MyClock.startOfNextDay(entryDate))
    open val eventTimeString: String
        get() = ""
    abstract val source: OrderedEventSource
    open val title: String
        get() = ""
    open val status: EventStatus?
        get() = EventStatus.CONFIRMED
    val locationString: String
        get() = if (hideLocation()) "" else location!!

    private fun hideLocation(): Boolean {
        return location!!.isEmpty() || !settings.showLocation
    }

    open val location: String?
        get() = ""

    override operator fun compareTo(other: WidgetEntry<*>): Int {
        val globalSignum = Integer.signum(entryPosition.globalOrder - other.entryPosition.globalOrder)
        if (globalSignum != 0) return globalSignum
        if (DateUtil.isSameDay(entryDay, other.entryDay)) {
            val sameDaySignum = Integer.signum(entryPosition.sameDayOrder - other.entryPosition.sameDayOrder)
            if (sameDaySignum != 0 && DateUtil.isSameDay(entryDay, other.entryDay)) return sameDaySignum
            if (entryDate.isAfter(other.entryDate)) {
                return 1
            } else if (entryDate.isBefore(other.entryDate)) {
                return -1
            }
        } else {
            if (entryDay.isAfter(other.entryDay)) {
                return 1
            } else if (entryDay.isBefore(other.entryDay)) {
                return -1
            }
        }
        val sourceSignum = Integer.signum(source.order - other.source.order)
        return if (sourceSignum == 0) title.compareTo(other.title) else sourceSignum
    }

    fun duplicates(other: WidgetEntry<*>): Boolean {
        return entryPosition == other.entryPosition && entryDate == other.entryDate &&
            DateUtil.isSameDate(endDate, other.endDate) && title == other.title && location == other.location
    }

    fun formatEntryDate(): CharSequence {
        return if (settings.entryDateFormat.type == DateFormatType.HIDDEN || !MyClock.isDateDefined(entryDate)) ""
        else settings.entryDateFormatter()
            .formatDate(entryDate)
    }

    fun newOnClickFillInIntent(): Intent {
        return Intent().putExtra(EXTRA_WIDGET_ENTRY_ID, entryId)
    }

    override fun toString(): String {
        return entryPosition.value + " [" +
            "entryDate=" +
            (if (entryDate === MyClock.DATETIME_MIN) "min" else if (entryDate === MyClock.DATETIME_MAX) "max" else entryDate) +
            ", endDate=" + endDate +
            (if (allDay) ", allDay" else "") +
            "]"
    }

    open val event: WidgetEvent?
        get() = null

    fun notHidden(): Boolean {
        return entryPosition != WidgetEntryPosition.HIDDEN
    }

    companion object {
        val EXTRA_WIDGET_ENTRY_ID: String = RemoteViewsFactory.PACKAGE + ".extra.WIDGET_ENTRY_ID"
        private val idGenerator = AtomicLong(0)
        private fun fixEntryDate(entryPosition: WidgetEntryPosition, entryDate: DateTime?): DateTime {
            return when (entryPosition) {
                WidgetEntryPosition.ENTRY_DATE -> {
                    throwIfNull(entryPosition, entryDate)
                    entryDate!!
                }

                WidgetEntryPosition.PAST_AND_DUE_HEADER,
                WidgetEntryPosition.PAST_AND_DUE,
                WidgetEntryPosition.START_OF_TODAY,
                WidgetEntryPosition.HIDDEN -> entryDate ?: MyClock.DATETIME_MIN

                WidgetEntryPosition.DAY_HEADER, WidgetEntryPosition.START_OF_DAY -> {
                    throwIfNull(entryPosition, entryDate)
                    entryDate!!.withTimeAtStartOfDay()
                }

                WidgetEntryPosition.END_OF_DAY -> {
                    throwIfNull(entryPosition, entryDate)
                    entryDate!!.withTimeAtStartOfDay().plusDays(1).minusMillis(1)
                }

                WidgetEntryPosition.END_OF_TODAY,
                WidgetEntryPosition.END_OF_LIST_HEADER,
                WidgetEntryPosition.END_OF_LIST,
                WidgetEntryPosition.LIST_FOOTER -> entryDate ?: MyClock.DATETIME_MAX

                else -> throw IllegalArgumentException("Invalid position $entryPosition; entryDate: $entryDate")
            }
        }

        private fun calcEntryDay(
            settings: InstanceSettings,
            entryPosition: WidgetEntryPosition?,
            entryDate: DateTime?
        ): DateTime {
            return when (entryPosition) {
                WidgetEntryPosition.START_OF_TODAY, WidgetEntryPosition.END_OF_TODAY -> settings.clock().now()
                    .withTimeAtStartOfDay()

                else -> entryDate!!.withTimeAtStartOfDay()
            }
        }

        private fun calcTimeSection(
            settings: InstanceSettings, entryPosition: WidgetEntryPosition?,
            entryDay: DateTime, endDate: DateTime?
        ): TimeSection {
            when (entryPosition) {
                WidgetEntryPosition.PAST_AND_DUE_HEADER -> return TimeSection.PAST
                WidgetEntryPosition.START_OF_TODAY -> return TimeSection.TODAY
                WidgetEntryPosition.END_OF_TODAY, WidgetEntryPosition.END_OF_LIST_HEADER, WidgetEntryPosition.END_OF_LIST, WidgetEntryPosition.LIST_FOOTER -> return TimeSection.FUTURE
                else -> {}
            }
            if (settings.clock().isToday(entryDay)) {
                if (entryPosition == WidgetEntryPosition.DAY_HEADER) return TimeSection.TODAY
                return if (settings.clock().isToday(endDate)) {
                    if (settings.clock().isBeforeNow(endDate)) TimeSection.PAST else TimeSection.TODAY
                } else TimeSection.TODAY
            }
            return if (settings.clock().isBeforeToday(entryDay)) TimeSection.PAST else if (settings.clock()
                    .isToday(endDate)
            ) TimeSection.TODAY else TimeSection.FUTURE
        }

        private fun throwIfNull(entryPosition: WidgetEntryPosition, entryDate: DateTime?) {
            requireNotNull(entryDate) { "Invalid entry date: $entryDate at position $entryPosition" }
        }

        fun getEntryPosition(
            settings: InstanceSettings,
            allDay: Boolean,
            mainDate: DateTime?,
            otherDate: DateTime?
        ): WidgetEntryPosition {
            if (mainDate == null && otherDate == null) return settings.taskWithoutDates.widgetEntryPosition
            val refDate = mainDate ?: otherDate
            if (settings.showPastEventsUnderOneHeader && settings.clock().isBeforeToday(refDate)) {
                return WidgetEntryPosition.PAST_AND_DUE
            }
            if (refDate!!.isAfter(settings.endOfTimeRange)) return WidgetEntryPosition.END_OF_LIST
            return if (allDay) settings.allDayEventsPlacement.widgetEntryPosition else WidgetEntryPosition.ENTRY_DATE
        }
    }
}
