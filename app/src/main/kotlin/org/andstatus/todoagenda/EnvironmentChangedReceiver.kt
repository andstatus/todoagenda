package org.andstatus.todoagenda

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.net.toUri
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.CalendarIntentUtil
import org.andstatus.todoagenda.util.DateUtil
import org.andstatus.todoagenda.util.PermissionsUtil
import org.andstatus.todoagenda.widget.WidgetEntry
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class EnvironmentChangedReceiver : BroadcastReceiver() {
    private fun unRegister(context: Context) {
        context.unregisterReceiver(this)
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.i(TAG, "Received intent: $intent")
        AllSettings.ensureLoadedFromFiles(context)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)
        val settings = if (widgetId == 0) null else AllSettings.loadedInstances[widgetId]
        val action =
            if (settings == null || intent.action.isNullOrBlank()) {
                RemoteViewsFactory.ACTION_REFRESH
            } else if (PermissionsUtil.mustRequestPermissions(context)) {
                // Recheck
                AllSettings.reInitialize(context)
                if (PermissionsUtil.mustRequestPermissions(context)) RemoteViewsFactory.ACTION_CONFIGURE else intent.action
            } else {
                intent.action
            }
        when (action) {
            RemoteViewsFactory.ACTION_OPEN_CALENDAR -> {
                val openCalendar =
                    CalendarIntentUtil.newOpenCalendarAtDayIntent(
                        DateTime(
                            settings!!.timeZone,
                        ),
                    )
                startActivity(context, openCalendar, action, widgetId, "Open Calendar")
                onReceive(context, intent, action, widgetId)
                updateWidget(context, widgetId)
            }

            RemoteViewsFactory.ACTION_VIEW_ENTRY -> {
                onReceive(context, intent, action, widgetId)
                updateWidget(context, widgetId)
            }

            RemoteViewsFactory.ACTION_GOTO_TODAY -> {
                gotoToday(context, widgetId)
            }

            RemoteViewsFactory.ACTION_ADD_CALENDAR_EVENT -> {
                val addCalendarEvent =
                    settings!!
                        .getFirstSource(true)
                        .source.providerType
                        .getEventProvider(context, widgetId)
                        .addEventIntent
                startActivity(context, addCalendarEvent, action, widgetId, "Add calendar event")
            }

            RemoteViewsFactory.ACTION_ADD_TASK -> {
                val addTask =
                    settings!!
                        .getFirstSource(false)
                        .source.providerType
                        .getEventProvider(context, widgetId)
                        .addEventIntent
                startActivity(context, addTask, action, widgetId, "Add task")
            }

            RemoteViewsFactory.ACTION_CONFIGURE -> {
                val activityIntent: Intent = MainActivity.intentToConfigure(context, widgetId)
                startActivity(context, activityIntent, action, widgetId, "Open widget Settings")
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                ApplicationPreferences.clearBootCompleted(context)
                updateAllWidgets(context)
            }

            else -> {
                updateAllWidgets(context)
            }
        }
    }

    private fun onReceive(
        context: Context,
        intent: Intent,
        action: String,
        widgetId: Int,
    ) {
        val entryId = intent.getLongExtra(WidgetEntry.EXTRA_WIDGET_ENTRY_ID, 0)
        val activityIntent: Intent? = RemoteViewsFactory.getOnClickIntent(widgetId, entryId)
        startActivity(
            context,
            activityIntent,
            action,
            widgetId,
            "Open Calendar/Tasks app.\nentryId:$entryId",
        )
    }

    private fun startActivity(
        context: Context,
        activityIntent: Intent?,
        action: String,
        widgetId: Int,
        msg1: String,
    ) {
        var msgLog =
            msg1 + "; " + (activityIntent ?: "(no intent), action:$action") +
                ", widgetId:" + widgetId
        if (activityIntent != null) {
            try {
                context.startActivity(activityIntent)
            } catch (e: Exception) {
                msgLog = "Failed to open Calendar/Tasks app.\n$msgLog"
                ErrorReportActivity.showMessage(context, msgLog, e)
            }
        }
        Log.d(TAG, msgLog)
    }

    companion object {
        private val registeredReceiver = AtomicReference<EnvironmentChangedReceiver>()
        private val TAG = EnvironmentChangedReceiver::class.java.simpleName

        fun registerReceivers(instances: Map<Int, InstanceSettings>) {
            if (instances.isEmpty()) return
            val instanceSettings = instances.values.iterator().next()
            val context = instanceSettings.context.applicationContext
            synchronized(registeredReceiver) {
                val receiver = EnvironmentChangedReceiver()
                EventProviderType.registerProviderChangedReceivers(context, receiver)
                val filter = IntentFilter()
                filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED)
                filter.addAction(Intent.ACTION_DREAMING_STOPPED)
                filter.addAction(Intent.ACTION_TIME_CHANGED)
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
                context.registerReceiver(receiver, filter)
                val oldReceiver = registeredReceiver.getAndSet(receiver)
                oldReceiver?.unRegister(context)
                scheduleStartOfDayAlarms(context, instances)
                schedulePeriodicAlarms(context, instances)
                Log.i(TAG, "Receivers are registered")
            }
        }

        private fun scheduleStartOfDayAlarms(
            context: Context,
            instances: Map<Int, InstanceSettings>,
        ) {
            val alarmTimes: MutableSet<DateTime> = HashSet()
            for (settings in instances.values) {
                alarmTimes.add(
                    settings.clock
                        .now()
                        .withTimeAtStartOfDay()
                        .plusDays(1),
                )
            }
            for ((counter, alarmTime) in alarmTimes.withIndex()) {
                val intent =
                    Intent(context, EnvironmentChangedReceiver::class.java)
                        .setAction(RemoteViewsFactory.ACTION_MIDNIGHT_ALARM)
                        .setData("intent:midnightAlarm$counter".toUri())
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        RemoteViewsFactory.REQUEST_CODE_MIDNIGHT_ALARM + counter,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE,
                    )
                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
                // setAndAllowWhileIdle is delivered from Doze (possibly inexact), unlike a
                // plain set(), so the header date is refreshed shortly after midnight even when
                // the device is idle.
                am?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.millis, pendingIntent)
            }
        }

        private fun schedulePeriodicAlarms(
            context: Context,
            instances: Map<Int, InstanceSettings>,
        ) {
            var periodMinutes = TimeUnit.DAYS.toMinutes(1).toInt()
            for (settings in instances.values) {
                val period = settings.refreshPeriodMinutes
                if (period in 1..<periodMinutes) {
                    periodMinutes = period
                }
            }
            val intent =
                Intent(context, EnvironmentChangedReceiver::class.java)
                    .setAction(RemoteViewsFactory.ACTION_PERIODIC_ALARM)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    RemoteViewsFactory.REQUEST_CODE_PERIODIC_ALARM,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE,
                )
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            if (am != null) {
                val alarmTime = DateUtil.exactMinutesPlusMinutes(DateTime.now(), periodMinutes)
                am.setInexactRepeating(
                    AlarmManager.RTC,
                    alarmTime.millis,
                    TimeUnit.MINUTES.toMillis(periodMinutes.toLong()),
                    pendingIntent,
                )
            }
        }

        fun forget() {
            registeredReceiver.set(null)
        }

        fun sleep(millis: Int) {
            try {
                Thread.sleep(millis.toLong())
            } catch (_: InterruptedException) {
                // Ignored
            }
        }

        fun updateWidget(
            context: Context,
            widgetId: Int,
        ) {
            val intent = Intent(context, AppWidgetProvider::class.java)
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            Log.d(TAG, "updateWidget:$widgetId, context:$context")
            context.sendBroadcast(intent)
        }

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, AppWidgetProvider::class.java)
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            val widgetIds: IntArray = AppWidgetProvider.getWidgetIds(context)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            Log.d(
                TAG,
                "updateAllWidgets:" + AppWidgetProvider.asList(widgetIds) + ", context:" + context,
            )
            context.sendBroadcast(intent)
        }
    }
}
