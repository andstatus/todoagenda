package org.andstatus.todoagenda.util;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class CalendarIntentUtil {

    private static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
    private static final String TIME = "time";

    public static Intent newOpenCalendarAtDayIntent(DateTime goToTime) {
        Intent intent = IntentUtil.newViewIntent();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath(TIME);
        if (goToTime.getMillis() != 0) {
            intent.putExtra(KEY_DETAIL_VIEW, true);
            ContentUris.appendId(builder, goToTime.getMillis());
        }
        intent.setData(builder.build());
        return intent;
    }

    public static Intent newAddCalendarEventIntent(DateTimeZone timeZone) {
        DateTime beginTime = new DateTime(timeZone).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
                .withMillisOfSecond(0);
        DateTime endTime = beginTime.plusHours(1);
        return IntentUtil.newIntent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getMillis());
    }
}
