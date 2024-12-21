package org.andstatus.todoagenda.calendar

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.provider.CalendarContract.Attendees
import android.provider.CalendarContract.Instances.ALL_DAY
import android.provider.CalendarContract.Instances.BEGIN
import android.provider.CalendarContract.Instances.CALENDAR_COLOR
import android.provider.CalendarContract.Instances.CALENDAR_ID
import android.provider.CalendarContract.Instances.CONTENT_URI
import android.provider.CalendarContract.Instances.DESCRIPTION
import android.provider.CalendarContract.Instances.DISPLAY_COLOR
import android.provider.CalendarContract.Instances.END
import android.provider.CalendarContract.Instances.EVENT_ID
import android.provider.CalendarContract.Instances.EVENT_LOCATION
import android.provider.CalendarContract.Instances.HAS_ALARM
import android.provider.CalendarContract.Instances.RRULE
import android.provider.CalendarContract.Instances.START_DAY
import android.provider.CalendarContract.Instances.STATUS
import android.provider.CalendarContract.Instances.TITLE
import android.util.Log
import androidx.core.database.getStringOrNull
import io.vavr.control.Try
import org.andstatus.todoagenda.RemoteViewsFactory
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.CalendarIntentUtil
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.widget.EventStatus
import org.joda.time.DateTime
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors

class CalendarEventProvider(type: EventProviderType, context: Context, widgetId: Int) :
    EventProvider(type, context, widgetId) {
    fun queryEvents(): List<CalendarEvent> {
        initialiseParameters()
        myContentResolver.onQueryEvents()
        if (myContentResolver.isPermissionNeeded(type) ||
            settings.getActiveEventSources(type).isEmpty()
        ) {
            return emptyList()
        }
        val eventList = timeFilteredEventList
        if (settings.showPastEventsWithDefaultColor) {
            addPastEventsWithDefaultColor(eventList)
        }
        if (settings.filterMode != FilterMode.NO_FILTERING) {
            if (settings.showOnlyClosestInstanceOfRecurringEvent) {
                filterShowOnlyClosestInstanceOfRecurringEvent(eventList)
            }
        }
        return eventList
    }

    private fun addPastEventsWithDefaultColor(eventList: MutableList<CalendarEvent>) {
        for (event in pastEventsWithColorList) {
            if (eventList.contains(event)) {
                eventList.remove(event)
            }
            eventList.add(event)
        }
    }

    private fun filterShowOnlyClosestInstanceOfRecurringEvent(eventList: MutableList<CalendarEvent>) {
        val eventIds: MutableMap<Long, CalendarEvent> = HashMap()
        val toRemove: MutableList<CalendarEvent> = ArrayList()
        for (event in eventList) {
            val otherEvent = eventIds[event.eventId]
            if (otherEvent == null) {
                eventIds[event.eventId] = event
            } else if (Math.abs(settings.clock.getNumberOfMinutesTo(event.startDate)) <
                Math.abs(settings.clock.getNumberOfMinutesTo(otherEvent.startDate))
            ) {
                toRemove.add(otherEvent)
                eventIds[event.eventId] = event
            } else {
                toRemove.add(event)
            }
        }
        eventList.removeAll(toRemove)
    }

    val endOfTimeRange: DateTime
        get() = mEndOfTimeRange
    val startOfTimeRange: DateTime
        get() = mStartOfTimeRange!!
    private val timeFilteredEventList: MutableList<CalendarEvent>
        get() {
            val filterMode = settings.filterMode
            val builder = CONTENT_URI.buildUpon()
            val startDateForQuery = if (filterMode == FilterMode.NORMAL_FILTER) correctStartOfTimeRangeForQuery(
                mStartOfTimeRange!!
            ) else MyClock.DATETIME_MIN
            ContentUris.appendId(builder, startDateForQuery.millis)
            val endDateForQuery =
                if (filterMode == FilterMode.NORMAL_FILTER) mEndOfTimeRange else MyClock.DATETIME_MAX
            ContentUris.appendId(builder, endDateForQuery.millis)
            val eventList = queryList(builder.build(), calendarSelection)
            if (settings.isForTestsReplaying) {
                val tag = "eventsQuerying"
                Log.d(
                    tag, "widget " + widgetId + ", start: " + startDateForQuery +
                        ", (before correction: " + mStartOfTimeRange + ")" +
                        ", end: " + endDateForQuery +
                        ", got " + eventList.size + " events"
                )
                val factory: RemoteViewsFactory? = RemoteViewsFactory.factories.get(widgetId)
                if (factory != null) {
                    factory.logWidgetEntries(tag)
                }
            }
            when (filterMode) {
                FilterMode.NO_FILTERING -> {}
                else ->                 // Filters in a query are not exactly correct for AllDay events:
                    // for them we are selecting events some days/time before what is defined in settings.
                    // This is why we need to do additional filtering after querying a Content Provider:
                {
                    val it = eventList.iterator()
                    while (it.hasNext()) {
                        val event = it.next()
                        if (!event.endDate.isAfter(mStartOfTimeRange)
                            || !mEndOfTimeRange.isAfter(event.startDate)
                        ) {
                            // We remove using Iterator to avoid ConcurrentModificationException
                            it.remove()
                        }
                    }
                }
            }
            return eventList
        }
    private val calendarSelection: String
        get() {
            val activeSources = settings.getActiveEventSources(type)
            val stringBuilder = StringBuilder(EVENT_SELECTION)
            if (!activeSources.isEmpty()) {
                stringBuilder.append(AND_BRACKET)
                val iterator: Iterator<OrderedEventSource> = activeSources.iterator()
                while (iterator.hasNext()) {
                    val source = iterator.next().source
                    stringBuilder.append(CALENDAR_ID)
                    stringBuilder.append(EQUALS)
                    stringBuilder.append(source.id)
                    if (iterator.hasNext()) {
                        stringBuilder.append(OR)
                    }
                }
                stringBuilder.append(CLOSING_BRACKET)
            }
            return stringBuilder.toString()
        }

    private fun queryList(uri: Uri, selection: String): MutableList<CalendarEvent> {
        return myContentResolver.foldEvents<ArrayList<CalendarEvent>>(uri,
            projection,
            selection,
            null,
            EVENT_SORT_ORDER,
            ArrayList(),
            { eventList: ArrayList<CalendarEvent> ->
                Function { cursor: Cursor ->
                    val event = newCalendarEvent(cursor)
                    if (!eventList.contains(event) &&
                        !hideBasedOnKeywordsFilter!!.matched(event.title) &&
                        showBasedOnKeywordsFilter!!.matched(event.title)
                    ) {
                        eventList.add(event)
                    }
                    eventList
                }
            })
    }

    private val pastEventsWithColorList: List<CalendarEvent>
        get() {
            val builder = CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, 0)
            ContentUris.appendId(builder, settings.clock.now().millis)
            return queryList(builder.build(), pastEventsWithColorSelection).stream()
                .filter { ev: CalendarEvent -> settings.filterMode != FilterMode.DEBUG_FILTER || ev.hasDefaultCalendarColor() }
                .collect(Collectors.toList())
        }
    private val pastEventsWithColorSelection: String
        get() = calendarSelection +
            AND_BRACKET +
            DISPLAY_COLOR + EQUALS + CALENDAR_COLOR +
            CLOSING_BRACKET

    @SuppressLint("Range")
    private fun newCalendarEvent(cursor: Cursor): CalendarEvent {
        val source = settings
            .getActiveEventSource(type, cursor.getInt(cursor.getColumnIndex(CALENDAR_ID)))
        val allDay = cursor.getInt(cursor.getColumnIndex(ALL_DAY)) > 0
        val event = CalendarEvent(settings, context, allDay)
        event.setEventSource(source)
        event.setEventId(cursor.getInt(cursor.getColumnIndex(EVENT_ID)))
        event.status =
            EventStatus.fromCalendarStatus(cursor.getInt(cursor.getColumnIndex(STATUS)))
        event.title = cursor.getStringOrNull(cursor.getColumnIndex(TITLE)) ?: ""
        event.startMillis = cursor.getLong(cursor.getColumnIndex(BEGIN))
        event.endMillis = cursor.getLong(cursor.getColumnIndex(END))
        event.location = cursor.getStringOrNull(cursor.getColumnIndex(EVENT_LOCATION))
        event.description = cursor.getStringOrNull(cursor.getColumnIndex(DESCRIPTION))
        event.isAlarmActive = cursor.getInt(cursor.getColumnIndex(HAS_ALARM)) > 0
        event.isRecurring = cursor.getStringOrNull(cursor.getColumnIndex(RRULE)) != null
        event.color = getAsOpaque(cursor.getInt(cursor.getColumnIndex(DISPLAY_COLOR)))
        getColumnIndex(cursor, CALENDAR_COLOR)
            .map<Int>({ ind: Int? -> getAsOpaque(cursor.getInt(ind!!)) })
            .ifPresent({ color: Int -> event.calendarColor = Optional.of(color) })
        return event
    }

    override fun fetchAvailableSources(): Try<MutableList<EventSource>> {
        return myContentResolver.foldAvailableSources<MutableList<EventSource>>(
            CalendarContract.Calendars.CONTENT_URI.buildUpon().build(),
            EVENT_SOURCES_PROJECTION,
            ArrayList<EventSource>(),
            { eventSources: MutableList<EventSource> ->
                { cursor: Cursor ->
                    val indId = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    val indTitle = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val indSummary = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                    val indColor = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
                    val source = EventSource(
                        type, cursor.getInt(indId), cursor.getStringOrNull(indTitle),
                        cursor.getStringOrNull(indSummary), cursor.getInt(indColor), true
                    )
                    eventSources.add(source)
                    eventSources
                }
            })
    }

    fun newViewEventIntent(event: CalendarEvent): Intent {
        val intent = IntentUtil.newViewIntent()
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId))
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startMillis)
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endMillis)
        return intent
    }

    override val addEventIntent: Intent
        get() = CalendarIntentUtil.newAddCalendarEventIntent(settings.clock.zone)

    companion object {
        private val TAG = CalendarEventProvider::class.java.simpleName
        private val EVENT_SOURCES_PROJECTION = arrayOf<String>(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        const val EVENT_SORT_ORDER = "${START_DAY} ASC, ${ALL_DAY} DESC, ${BEGIN} ASC "
        private const val EVENT_SELECTION = (CalendarContract.Instances.SELF_ATTENDEE_STATUS + "!="
            + Attendees.ATTENDEE_STATUS_DECLINED)

        fun correctStartOfTimeRangeForQuery(startDateIn: DateTime): DateTime {
            return if (startDateIn.isAfter(MyClock.DATETIME_MIN)) {
                startDateIn.minusDays(2)
            } else {
                startDateIn
            }
        }

        val projection: Array<String> =
            run {
                val columnNames: MutableList<String> = ArrayList()
                columnNames.add(CALENDAR_ID)
                columnNames.add(EVENT_ID)
                columnNames.add(STATUS)
                columnNames.add(TITLE)
                columnNames.add(BEGIN)
                columnNames.add(END)
                columnNames.add(ALL_DAY)
                columnNames.add(EVENT_LOCATION)
                columnNames.add(DESCRIPTION)
                columnNames.add(HAS_ALARM)
                columnNames.add(RRULE)
                columnNames.add(DISPLAY_COLOR)
                columnNames.add(CALENDAR_COLOR)
                columnNames.toTypedArray<String>()
            }
    }
}
