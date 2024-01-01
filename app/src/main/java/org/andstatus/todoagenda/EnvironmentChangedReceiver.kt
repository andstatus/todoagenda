package org.andstatus.todoagenda;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.util.StringUtil;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.andstatus.todoagenda.AppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.newOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.widget.WidgetEntry.EXTRA_WIDGET_ENTRY_ID;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();
    private static final String TAG = EnvironmentChangedReceiver.class.getSimpleName();

    public static void registerReceivers(Map<Integer, InstanceSettings> instances) {
        if (instances.isEmpty()) return;

        InstanceSettings instanceSettings = instances.values().iterator().next();
        Context context = instanceSettings.getContext().getApplicationContext();
        synchronized (registeredReceiver) {
            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();
            EventProviderType.registerProviderChangedReceivers(context, receiver);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            context.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }
            scheduleMidnightAlarms(context, instances);
            schedulePeriodicAlarms(context, instances);

            Log.i(TAG, "Receivers are registered");
        }
    }

    private static void scheduleMidnightAlarms(Context context, Map<Integer, InstanceSettings> instances) {
        Set<DateTime> alarmTimes = new HashSet<>();
        for (InstanceSettings settings : instances.values()) {
            alarmTimes.add(settings.clock().now().withTimeAtStartOfDay().plusDays(1));
        }
        int counter = 0;
        for (DateTime alarmTime : alarmTimes) {
            Intent intent = new Intent(context, EnvironmentChangedReceiver.class)
                    .setAction(RemoteViewsFactory.ACTION_MIDNIGHT_ALARM)
                    .setData(Uri.parse("intent:midnightAlarm" + counter));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    RemoteViewsFactory.REQUEST_CODE_MIDNIGHT_ALARM + counter,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.set(AlarmManager.RTC, alarmTime.getMillis(), pendingIntent);
            }
            counter++;
        }
    }

    private static void schedulePeriodicAlarms(Context context, Map<Integer, InstanceSettings> instances) {
        int periodMinutes = (int) TimeUnit.DAYS.toMinutes(1);
        for (InstanceSettings settings : instances.values()) {
            int period = settings.getRefreshPeriodMinutes();
            if (period > 0 && period < periodMinutes) {
                periodMinutes = period;
            }
        }
        Intent intent = new Intent(context, EnvironmentChangedReceiver.class)
            .setAction(RemoteViewsFactory.ACTION_PERIODIC_ALARM);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                RemoteViewsFactory.REQUEST_CODE_PERIODIC_ALARM,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            DateTime alarmTime = DateUtil.exactMinutesPlusMinutes(DateTime.now(), periodMinutes);
            am.setInexactRepeating(AlarmManager.RTC, alarmTime.getMillis(),
                    TimeUnit.MINUTES.toMillis(periodMinutes), pendingIntent);
        }
    }

    public static void forget() {
        registeredReceiver.set(null);
    }

    private void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent: " + intent);
        AllSettings.ensureLoadedFromFiles(context, false);
        int widgetId = intent == null
                ? 0
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        InstanceSettings settings = widgetId == 0
                ? null
                : AllSettings.getLoadedInstances().get(widgetId);
        String action0 = intent == null ? "" : intent.getAction();
        String action = intent == null || settings == null || StringUtil.isEmpty(action0)
                ? RemoteViewsFactory.ACTION_REFRESH
                : action0;
        if (!action.equals(RemoteViewsFactory.ACTION_REFRESH) && !PermissionsUtil.arePermissionsGranted(context)) {
            action = RemoteViewsFactory.ACTION_CONFIGURE;
        }
        switch (action) {
            case RemoteViewsFactory.ACTION_OPEN_CALENDAR:
                Intent openCalendar = newOpenCalendarAtDayIntent(new DateTime(settings.clock().getZone()));
                startActivity(context, openCalendar, action, widgetId, "Open Calendar");
            case RemoteViewsFactory.ACTION_VIEW_ENTRY:
                onReceive(context, intent, action, widgetId);
                updateWidget(context, widgetId);
                break;
            case RemoteViewsFactory.ACTION_GOTO_TODAY:
                gotoToday(context, widgetId);
                break;
            case RemoteViewsFactory.ACTION_ADD_CALENDAR_EVENT:
                Intent addCalendarEvent = settings.getFirstSource(true).source.providerType
                        .getEventProvider(context, widgetId)
                        .getAddEventIntent();
                startActivity(context, addCalendarEvent, action, widgetId, "Add calendar event");
                break;
            case RemoteViewsFactory.ACTION_ADD_TASK:
                Intent addTask = settings.getFirstSource(false).source.providerType
                        .getEventProvider(context, widgetId)
                        .getAddEventIntent();
                startActivity(context, addTask, action, widgetId,"Add task");
                break;
            case RemoteViewsFactory.ACTION_CONFIGURE:
                Intent configure = MainActivity.intentToConfigure(context, widgetId);
                startActivity(context, configure, action, widgetId, "Open widget Settings");
                break;
            default:
                updateAllWidgets(context);
                break;
        }
    }

    private void gotoToday(Context context, int widgetId) {
        RemoteViewsFactory factory = RemoteViewsFactory.factories.get(widgetId);
        int position1 = factory == null ? 0 : factory.getTomorrowsPosition();
        int position2 = factory == null ? 0 : factory.getTodaysPosition();
        gotoPosition(context, widgetId, position1);
        if (position1 >= 0 && position2 >= 0 && position1 != position2) {
            sleep(1000);
        }
        gotoPosition(context, widgetId, position2);
    }

    private void onReceive(Context context, @NonNull Intent intent, @NonNull String action, int widgetId) {
        long entryId = intent.getLongExtra(EXTRA_WIDGET_ENTRY_ID, 0);
        Intent activityIntent = RemoteViewsFactory.getOnClickIntent(widgetId, entryId);
        startActivity(context, activityIntent, action, widgetId, "Open Calendar/Tasks app.\nentryId:" + entryId);
    }

    private void startActivity(Context context, Intent activityIntent, String action, int widgetId, String msg1) {
        String msgLog = msg1 + "; " + (activityIntent == null ? "(no intent), action:" + action : activityIntent) +
                ", widgetId:" + widgetId;
        if (activityIntent != null) {
            try {
                context.startActivity(activityIntent);
            } catch (Exception e) {
                msgLog = "Failed to open Calendar/Tasks app.\n" + msgLog;
                ErrorReportActivity.showMessage(context, msgLog, e);
            }
        }
        Log.d(TAG, msgLog);
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignored
        }
    }

    private void gotoPosition(Context context, int widgetId, int position) {
        if (widgetId == 0 || position < 0) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);
        Log.d(TAG, "gotoPosition, Scrolling widget " + widgetId + " to position " + position);
        rv.setScrollPosition(R.id.event_list, position);
        appWidgetManager.updateAppWidget(widgetId, rv);
    }

    public static void updateWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, AppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        Log.d(TAG, "updateWidget:" + widgetId + ", context:" + context);
        context.sendBroadcast(intent);
    }

    public static void updateAllWidgets(Context context) {
        Intent intent = new Intent(context, AppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] widgetIds = getWidgetIds(context);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        Log.d(TAG, "updateAllWidgets:" + AppWidgetProvider.asList(widgetIds) + ", context:" + context);
        context.sendBroadcast(intent);
    }
}
