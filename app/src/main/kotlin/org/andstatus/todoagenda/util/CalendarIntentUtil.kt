package org.andstatus.todoagenda.util

import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

object CalendarIntentUtil {
    private const val TIME = "time"

    /**
     * https://developer.android.com/guide/topics/providers/calendar-provider.html#intents
     */
    fun newOpenCalendarAtDayIntent(goToTime: DateTime): Intent {
        val intent = IntentUtil.newViewIntent()
        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath(TIME)
        ContentUris.appendId(builder, goToTime.millis)
        intent.setData(builder.build())
        return intent
    }

    fun newAddCalendarEventIntent(timeZone: DateTimeZone?): Intent {
        val beginTime = DateTime(timeZone).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
            .withMillisOfSecond(0)
        val endTime = beginTime.plusHours(1)
        return IntentUtil.newIntent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.millis)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.millis)
    }
}
