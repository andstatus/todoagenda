package org.andstatus.todoagenda;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.AbstractList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.IdRes;

import static org.andstatus.todoagenda.Theme.themeNameToResId;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    private static final String PACKAGE = "org.andstatus.todoagenda";
    public static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";
    public static final String ACTION_GOTO_POSITIONS = PACKAGE + ".action.GOTO_TODAY";
    public static final String EXTRA_WIDGET_LIST_POSITION1 = "widgetListPosition1";
    public static final String EXTRA_WIDGET_LIST_POSITION2 = "widgetListPosition2";
    public static final int MAX_NUMBER_OF_WIDGETS = 100;
    public static final int REQUEST_CODE_EMPTY = 1;
    public static final int REQUEST_CODE_ADD_EVENT = 2;
    public static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            AllSettings.delete(context, widgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("onUpdate", "WidgetIds:" + asList(appWidgetIds));
        for (int widgetId : appWidgetIds) {
            addWidgetViews(context, widgetId);
            updateWidget(context, widgetId);
        }
    }

    public static void addWidgetViews(Context context, int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
        addWidgetParts(settings, widgetId);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        configureWidgetHeader(settings, rv);
        configureList(settings, widgetId, rv);
        configureNoEvents(settings, rv);
        appWidgetManager.updateAppWidget(widgetId, rv);
    }

    private static void addWidgetParts(InstanceSettings settings, int widgetId) {
        RemoteViews rvParent = new RemoteViews(settings.getContext().getPackageName(), R.layout.widget);
        rvParent.removeAllViews(R.id.widget_parent);
        if (settings.getShowWidgetHeader()) {
            RemoteViews rv = new RemoteViews(settings.getContext().getPackageName(), R.layout.widget_header_one_line);
            rvParent.addView(R.id.widget_parent, rv);
        }
        RemoteViews rv = new RemoteViews(settings.getContext().getPackageName(), R.layout.widget_body);
        rvParent.addView(R.id.widget_parent, rv);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(settings.getContext());
        appWidgetManager.updateAppWidget(widgetId, rvParent);
    }

    private static void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        if (!settings.getShowWidgetHeader()) return;

        setBackgroundColor(rv, R.id.action_bar, settings.getWidgetHeaderBackgroundColor());
        configureCurrentDate(settings, rv);
        setActionIcons(settings, rv);
        configureAddEvent(settings, rv);
        configureRefresh(settings, rv);
        configureOverflowMenu(settings, rv);
    }

    private static void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
        int viewId = R.id.calendar_current_date;
        rv.setOnClickPendingIntent(viewId, createOpenCalendarPendingIntent(settings));
        String formattedDate = DateUtil.createDateString(settings,
                DateUtil.now(settings.getTimeZone())).toUpperCase(Locale.getDefault());
        rv.setTextViewText(viewId, formattedDate);
        setTextSize(settings, rv, viewId, R.dimen.widget_header_title);
        setTextColorFromAttr(settings.getWidgetHeaderThemeContext(), rv, viewId, R.attr.header);
    }

    private static void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        setImageFromAttr(settings.getWidgetHeaderThemeContext(), rv, R.id.go_to_today, R.attr.header_action_go_to_today);
        setImageFromAttr(settings.getWidgetHeaderThemeContext(), rv, R.id.add_event, R.attr.header_action_add_event);
        setImageFromAttr(settings.getWidgetHeaderThemeContext(), rv, R.id.refresh, R.attr.header_action_refresh);
        setImageFromAttr(settings.getWidgetHeaderThemeContext(), rv, R.id.overflow_menu, R.attr.header_action_overflow);
        int themeId = themeNameToResId(settings.getWidgetHeaderTheme());
        int alpha = 255;
        if (themeId == R.style.Theme_Calendar_Dark || themeId == R.style.Theme_Calendar_Light) {
            alpha = 154;
        }
        setAlpha(rv, R.id.go_to_today, alpha);
        setAlpha(rv, R.id.add_event, alpha);
        setAlpha(rv, R.id.refresh, alpha);
        setAlpha(rv, R.id.overflow_menu, alpha);
    }

    private static void configureAddEvent(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.add_event, getPermittedAddEventPendingIntent(settings));
    }

    private static PendingIntent getPermittedAddEventPendingIntent(InstanceSettings settings) {
        Context context = settings.getContext();
        Intent intent = PermissionsUtil.getPermittedActivityIntent(context,
                CalendarIntentUtil.createNewEventIntent(settings.getTimeZone()));
        return isIntentAvailable(context, intent) ?
                PendingIntent.getActivity(context, REQUEST_CODE_ADD_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT) :
                getEmptyPendingIntent(context);
    }

    public static PendingIntent getEmptyPendingIntent(Context context) {
        return PendingIntent.getActivity(
                context.getApplicationContext(),
                REQUEST_CODE_EMPTY,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        Intent intent = new Intent(settings.getContext(), EnvironmentChangedReceiver.class);
        intent.setAction(ACTION_REFRESH);
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private static void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        Intent intent = MainActivity.intentToConfigure(settings.getContext(), settings.getWidgetId());
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingActivityIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.overflow_menu, pendingIntent);
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static void configureList(InstanceSettings settings, int widgetId, RemoteViews rv) {
        Intent intent = new Intent(settings.getContext(), RemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.event_list, intent);
    }

    private static void configureNoEvents(InstanceSettings settings, RemoteViews rv) {
        boolean permissionsGranted = PermissionsUtil.arePermissionsGranted(settings.getContext());
        @IdRes int viewId = R.id.empty_event_list;
        rv.setEmptyView(R.id.event_list, viewId);
        rv.setTextViewText(viewId, settings.getContext().getText(
                permissionsGranted ? R.string.no_upcoming_events : R.string.grant_permissions_verbose
        ));
        rv.setOnClickPendingIntent(viewId, getPermittedAddEventPendingIntent(settings));
        if (permissionsGranted) {
            rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(settings));
            rv.setOnClickFillInIntent(viewId,
                    createOpenCalendarAtDayIntent(new DateTime(settings.getTimeZone())));
        }
        setTextSize(settings, rv, viewId, R.dimen.event_entry_details);
        setBackgroundColor(rv, viewId, settings.getEventsBackgroundColor());
        setTextColorFromAttr(settings.getEntryThemeContext(), rv, viewId, R.attr.eventEntryTitle);
    }

    private static List<Integer> asList(final int[] is) {
        return new AbstractList<Integer>() {
            public Integer get(int i) { return is[i]; }
            public int size() { return is.length; }
        };
    }

    static void updateAllWidgets(Context context) {
        updateWidgets(context, getWidgetIds(context));
    }

    public static int[] getWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager == null
                ? new int[]{}
                : appWidgetManager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider.class));
    }

    public static void updateWidget(Context context, int widgetId) {
        updateWidgets(context, new int[]{widgetId});
    }

    public static void updateWidgets(Context context, int[] widgetIds) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            for (int widgetId : widgetIds) {
                appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{widgetId}, R.id.event_list);
            }
        }
    }

}