package org.andstatus.todoagenda;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;
import static org.andstatus.todoagenda.widget.LastEntry.LastEntryType.NOT_LOADED;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.DAY_HEADER;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.END_OF_LIST_HEADER;
import static org.andstatus.todoagenda.widget.WidgetEntryPosition.PAST_AND_DUE_HEADER;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.prefs.colors.BackgroundColorPref;
import org.andstatus.todoagenda.prefs.colors.Shading;
import org.andstatus.todoagenda.prefs.colors.TextColorPref;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.util.InstanceId;
import org.andstatus.todoagenda.util.MyClock;
import org.andstatus.todoagenda.util.StringUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.LastEntryVisualizer;
import org.andstatus.todoagenda.widget.TimeSection;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.andstatus.todoagenda.widget.WidgetHeaderLayout;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = RemoteViewsFactory.class.getSimpleName();
    public final static ConcurrentHashMap<Integer, RemoteViewsFactory> factories = new ConcurrentHashMap<>();

    private static final int MAX_NUMBER_OF_WIDGETS = 100;
    private static final int REQUEST_CODE_ADD_EVENT = 2;
    static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;
    static final int REQUEST_CODE_PERIODIC_ALARM = REQUEST_CODE_MIDNIGHT_ALARM + MAX_NUMBER_OF_WIDGETS;

    public static final String PACKAGE = "org.andstatus.todoagenda";
    static final String ACTION_OPEN_CALENDAR = PACKAGE + ".action.OPEN_CALENDAR";
    static final String ACTION_GOTO_TODAY = PACKAGE + ".action.GOTO_TODAY";
    static final String ACTION_ADD_CALENDAR_EVENT = PACKAGE + ".action.ADD_CALENDAR_EVENT";
    static final String ACTION_ADD_TASK = PACKAGE + ".action.ADD_TASK";
    static final String ACTION_VIEW_ENTRY = PACKAGE + ".action.VIEW_ENTRY";
    static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";
    public static final String ACTION_CONFIGURE = PACKAGE + ".action.CONFIGURE";
    static final String ACTION_MIDNIGHT_ALARM = PACKAGE + ".action.MIDNIGHT_ALARM";
    static final String ACTION_PERIODIC_ALARM = PACKAGE + ".action.PERIODIC_ALARM";

    public final long instanceId = InstanceId.next();
    public final Context context;
    private final int widgetId;
    public final boolean createdByLauncher;

    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();

    public RemoteViewsFactory(Context context, int widgetId, boolean createdByLauncher) {
        this.context = context;
        this.widgetId = widgetId;
        this.createdByLauncher = createdByLauncher;
        visualizers.add(new LastEntryVisualizer(context, widgetId));
        widgetEntries.add(new LastEntry(getSettings(), NOT_LOADED, getSettings().clock().now()));
        logEvent("Init" + (createdByLauncher ? " by Launcher" : ""));
    }

    private void logEvent(String message) {
        Log.d(TAG, widgetId + " instance:" + instanceId + " " + message);
    }

    public void onCreate() {
        logEvent("onCreate");
    }

    public void onDestroy() {
        logEvent("onDestroy");
    }

    public int getCount() {
        logEvent("getCount:" + widgetEntries.size() + " " + InstanceState.get(widgetId).listRedrawn);
        if (widgetEntries.isEmpty()) {
            InstanceState.listRedrawn(widgetId);
        }
        return widgetEntries.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            WidgetEntryVisualizer<? extends WidgetEntry> visualizer = visualizerFor(entry);
            if (visualizer != null) {
                RemoteViews views = visualizer.getRemoteViews(entry, position);
                views.setOnClickFillInIntent(R.id.event_entry, entry.newOnClickFillInIntent());
                if (position == widgetEntries.size() - 1) {
                    InstanceState.listRedrawn(widgetId);
                }
                return views;
            } else {
                logEvent("no visualizer at:" + position + " for " + entry);
                return null;
            }
        }
        logEvent("no view at:" + position + ", size:" + widgetEntries.size());
        return null;
    }

    private WidgetEntryVisualizer<? extends WidgetEntry> visualizerFor(WidgetEntry entry) {
        for (WidgetEntryVisualizer<? extends WidgetEntry> visualizer : visualizers) {
            if (visualizer.isFor(entry)) return visualizer;
        }
        return null;
    }

    public static Intent getOnClickIntent(int widgetId, long entryId) {
        if (widgetId == 0 || entryId == 0) return null;

        RemoteViewsFactory factory = factories.get(widgetId);
        if (factory == null) return null;

        WidgetEntry entry = factory.getWidgetEntries().stream()
            .filter(we -> we.entryId == entryId)
            .findFirst().orElse(null);
        factory.logEvent("Clicked entryId:" + entryId + ", entry: " + entry);
        if (entry == null) return null;

        WidgetEntryVisualizer<? extends WidgetEntry> visualizer = factory.visualizerFor(entry);
        return visualizer == null
            ? null
            : visualizer.newViewEntryIntent(entry);
    }

    @NonNull
    private InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    @Override
    public void onDataSetChanged() {
        logEvent("onDataSetChanged");
        reload();
    }

    private void reload() {
        visualizers = getVisualizers();
        this.widgetEntries = queryWidgetEntries(getSettings());
        InstanceState.listReloaded(widgetId);
        logEvent("reload, visualizers:" + visualizers.size() + ", entries:" + this.widgetEntries.size());
    }

    static void updateWidget(Context context, int widgetId) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.d(TAG, widgetId + " updateWidget, appWidgetManager is null, context:" + context);
                return;
            }

            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);

            settings.clock().updateZone();
            configureWidgetHeader(settings, rv);
            configureWidgetEntriesList(settings, rv);

            appWidgetManager.updateAppWidget(widgetId, rv);
        } catch (Exception e) {
            Log.w(TAG, widgetId + " Exception in updateWidget, context:" + context, e);
        }
    }

    private List<WidgetEntryVisualizer<? extends WidgetEntry>> getVisualizers() {
        List<WidgetEntryVisualizer<? extends WidgetEntry>> visualizers = new ArrayList<>();
        DayHeaderVisualizer dayHeaderVisualizer = new DayHeaderVisualizer(
                getSettings().getContext(),
                widgetId);
        visualizers.add(dayHeaderVisualizer);
        for (EventProviderType type : getSettings().getTypesOfActiveEventProviders()) {
            visualizers.add(type.getVisualizer(getSettings().getContext(), widgetId));
        }
        visualizers.add(new LastEntryVisualizer(context, widgetId));
        return visualizers;
    }

    int getTodaysPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (getWidgetEntries().get(ind).timeSection != TimeSection.PAST) return ind;
        }
        return getWidgetEntries().size() - 1;
    }

    int getTomorrowsPosition() {
        for (int ind = 0; ind < getWidgetEntries().size() - 1; ind++) {
            if (getWidgetEntries().get(ind).timeSection == TimeSection.FUTURE) return ind;
        }
        return getWidgetEntries().size() > 0 ? 0 : -1;
    }

    private List<WidgetEntry> queryWidgetEntries(InstanceSettings settings) {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> visualizer : visualizers) {
            eventEntries.addAll(visualizer.queryEventEntries());
        }
        Collections.sort(eventEntries);
        List<WidgetEntry> noHidden = eventEntries.stream().filter(WidgetEntry::notHidden).collect(Collectors.toList());
        List<WidgetEntry> deduplicated = settings.getHideDuplicates() ? filterOutDuplicates(noHidden) : noHidden;
        List<WidgetEntry> widgetEntries = settings.getShowDayHeaders() ? addDayHeaders(deduplicated) : deduplicated;
        LastEntry.addLast(settings, widgetEntries);
        return widgetEntries;
    }

    private List<WidgetEntry> filterOutDuplicates(List<WidgetEntry> inputEntries) {
        List<WidgetEntry> deduplicated = new ArrayList<>();
        List<WidgetEntry> hidden = new ArrayList<>();
        for(int ind1 = 0; ind1 < inputEntries.size(); ind1++) {
            WidgetEntry inputEntry = inputEntries.get(ind1);
            if (!hidden.contains(inputEntry)) {
                deduplicated.add(inputEntry);
                for(int ind2 = ind1 + 1; ind2 < inputEntries.size(); ind2++) {
                    WidgetEntry entry2 = inputEntries.get(ind2);
                    if (!hidden.contains(entry2) && inputEntry.duplicates(entry2)) {
                        hidden.add(entry2);
                    }
                }
            }
        }
        return deduplicated;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            InstanceSettings settings = getSettings();
            DayHeader curDayBucket = new DayHeader(settings, DAY_HEADER, MyClock.DATETIME_MIN);
            boolean pastEventsHeaderAdded = false;
            boolean endOfListHeaderAdded = false;
            for (WidgetEntry entry : listIn) {
                DateTime nextEntryDay = entry.entryDay;
                switch (entry.entryPosition) {
                    case PAST_AND_DUE:
                        if(!pastEventsHeaderAdded) {
                            curDayBucket = new DayHeader(settings, PAST_AND_DUE_HEADER, MyClock.DATETIME_MIN);
                            listOut.add(curDayBucket);
                            pastEventsHeaderAdded = true;
                        }
                        break;
                    case END_OF_LIST:
                        if (!endOfListHeaderAdded) {
                            endOfListHeaderAdded = true;
                            curDayBucket = new DayHeader(settings, END_OF_LIST_HEADER, MyClock.DATETIME_MAX);
                            listOut.add(curDayBucket);
                        }
                        break;
                    default:
                        if (!nextEntryDay.isEqual(curDayBucket.entryDay)) {
                            if (settings.getShowDaysWithoutEvents()) {
                                addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.entryDay, nextEntryDay);
                            }
                            curDayBucket = new DayHeader(settings, DAY_HEADER, nextEntryDay);
                            listOut.add(curDayBucket);
                        }
                }
                listOut.add(entry);
            }
        }
        return listOut;
    }

    public void logWidgetEntries(String tag) {
        Log.v(tag, "Widget entries: " + getWidgetEntries().size());
        for (int ind = 0; ind < getWidgetEntries().size(); ind++) {
            WidgetEntry widgetEntry = getWidgetEntries().get(ind);
            Log.v(tag, String.format("%02d ", ind) + widgetEntry.toString());
        }
    }

    List<? extends WidgetEntry> getWidgetEntries() {
        return widgetEntries;
    }

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive, DateTime toDayExclusive) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = getSettings().clock().now().withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(getSettings(), DAY_HEADER, emptyDay));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        int result = 14;  // Actually this is maximum number of different layoutIDs
        logEvent("getViewTypeCount:" + result);
        return result;
    }

    public long getItemId(int position) {
        logEvent("getItemId: " + position);
        if (position < widgetEntries.size()) {
            return position + 1;
        }
        return 0;
    }

    public boolean hasStableIds() {
        return false;
    }

    private static void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        Log.d(TAG, settings.getWidgetId() + " configureWidgetHeader, layout:" + settings.getWidgetHeaderLayout());
        rv.removeAllViews(R.id.header_parent);

        if (settings.getWidgetHeaderLayout() != WidgetHeaderLayout.HIDDEN) {
            RemoteViews headerView = new RemoteViews(settings.getContext().getPackageName(),
                    settings.getWidgetHeaderLayout().layoutId);
            rv.addView(R.id.header_parent, headerView);

            setBackgroundColor(rv, R.id.action_bar,
                    settings.colors().getBackgroundColor(BackgroundColorPref.WIDGET_HEADER));
            configureCurrentDate(settings, rv);
            setActionIcons(settings, rv);
            configureGotoToday(settings, rv);
            configureAddCalendarEvent(settings, rv);
            configureAddTask(settings, rv);
            configureRefresh(settings, rv);
            configureOverflowMenu(settings, rv);
        }
    }

    private static void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
        int viewId = R.id.calendar_current_date;
        rv.setOnClickPendingIntent(viewId, getActionPendingIntent(settings, ACTION_OPEN_CALENDAR));
        String formattedDate = settings.widgetHeaderDateFormatter()
                .formatDate(settings.clock().now()).toString()
                .toUpperCase(Locale.getDefault());
        rv.setTextViewText(viewId, StringUtil.isEmpty(formattedDate) ? "                    " : formattedDate);
        setTextSize(settings, rv, viewId, R.dimen.widget_header_title);
        setTextColor(settings, TextColorPref.WIDGET_HEADER, rv, viewId, R.attr.header);
    }

    private static void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        ContextThemeWrapper themeContext = settings.colors().getThemeContext(TextColorPref.WIDGET_HEADER);
        setImageFromAttr(themeContext, rv, R.id.go_to_today, R.attr.header_action_go_to_today);
        setImageFromAttr(themeContext, rv, R.id.add_event, R.attr.header_action_add_event);
        setImageFromAttr(themeContext, rv, R.id.add_task, R.attr.header_action_add_task);
        setImageFromAttr(themeContext, rv, R.id.refresh, R.attr.header_action_refresh);
        setImageFromAttr(themeContext, rv, R.id.overflow_menu, R.attr.header_action_overflow);
        Shading shading = settings.colors().getShading(TextColorPref.WIDGET_HEADER);
        int alpha = 255;
        if (shading == Shading.DARK || shading == Shading.LIGHT) {
            alpha = 154;
        }
        setAlpha(rv, R.id.go_to_today, alpha);
        setAlpha(rv, R.id.add_event, alpha);
        setAlpha(rv, R.id.add_task, alpha);
        setAlpha(rv, R.id.refresh, alpha);
        setAlpha(rv, R.id.overflow_menu, alpha);
    }

    private static void configureAddCalendarEvent(InstanceSettings settings, RemoteViews rv) {
        if (settings.getFirstSource(true) == OrderedEventSource.EMPTY) {
            rv.setViewVisibility(R.id.add_event, View.GONE);
        } else {
            rv.setViewVisibility(R.id.add_event, View.VISIBLE);
            rv.setOnClickPendingIntent(R.id.add_event, getActionPendingIntent(settings, ACTION_ADD_CALENDAR_EVENT));
        }
    }

    private static void configureAddTask(InstanceSettings settings, RemoteViews rv) {
        if (settings.getFirstSource(false) == OrderedEventSource.EMPTY) {
            rv.setViewVisibility(R.id.add_task, View.GONE);
        } else {
            rv.setViewVisibility(R.id.add_task, View.VISIBLE);
            rv.setOnClickPendingIntent(R.id.add_task, getActionPendingIntent(settings, ACTION_ADD_TASK));
        }
    }

    private static void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.refresh, getActionPendingIntent(settings, ACTION_REFRESH));
    }

    private static void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.overflow_menu, getActionPendingIntent(settings, ACTION_CONFIGURE));
    }

    private static void configureWidgetEntriesList(InstanceSettings settings, RemoteViews rv) {
        Intent intent = new Intent(settings.getContext(), org.andstatus.todoagenda.RemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setRemoteAdapter(R.id.event_list, intent);
        rv.setPendingIntentTemplate(R.id.event_list, getActionPendingIntent(settings, ACTION_VIEW_ENTRY));
    }

    private static void configureGotoToday(InstanceSettings settings, RemoteViews rv) {
        rv.setOnClickPendingIntent(R.id.go_to_today, getActionPendingIntent(settings, ACTION_GOTO_TODAY));
    }

    public static PendingIntent getActionPendingIntent(InstanceSettings settings, String action) {
        // We need unique request codes for each widget
        int requestCode = action.hashCode() + settings.getWidgetId();
        Intent intent = new Intent(settings.getContext().getApplicationContext(), EnvironmentChangedReceiver.class)
                .setAction(action)
                .setData(Uri.parse("intent:" + action.toLowerCase() + settings.getWidgetId()))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags += PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.getBroadcast(settings.getContext(), requestCode, intent, flags);
    }

}
