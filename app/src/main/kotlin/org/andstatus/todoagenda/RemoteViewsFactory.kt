package org.andstatus.todoagenda

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.prefs.colors.BackgroundColorPref
import org.andstatus.todoagenda.prefs.colors.Shading
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.util.InstanceId
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.RemoteViewsUtil
import org.andstatus.todoagenda.util.StringUtil
import org.andstatus.todoagenda.widget.DayHeader
import org.andstatus.todoagenda.widget.DayHeaderVisualizer
import org.andstatus.todoagenda.widget.LastEntry
import org.andstatus.todoagenda.widget.LastEntry.LastEntryType
import org.andstatus.todoagenda.widget.LastEntryVisualizer
import org.andstatus.todoagenda.widget.TimeSection
import org.andstatus.todoagenda.widget.WidgetEntry
import org.andstatus.todoagenda.widget.WidgetEntryPosition
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer
import org.andstatus.todoagenda.widget.WidgetHeaderLayout
import org.andstatus.todoagenda.widget.WidgetLayout
import org.joda.time.DateTime
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.concurrent.Volatile

class RemoteViewsFactory(val context: Context, private val widgetId: Int, createdByLauncher: Boolean) :
    android.widget.RemoteViewsService.RemoteViewsFactory {
    val instanceId = InstanceId.next()

    @Volatile
    var widgetEntries: MutableList<WidgetEntry<*>> = ArrayList()

    @Volatile
    private var visualizers: MutableList<WidgetEntryVisualizer<out WidgetEntry<*>>> = ArrayList()

    init {
        visualizers.add(LastEntryVisualizer(context, widgetId))
        widgetEntries.add(LastEntry(settings, LastEntryType.NOT_LOADED, settings.clock().now()))
        logEvent("Init" + if (createdByLauncher) " by Launcher" else "")
    }

    private fun logEvent(message: String) {
        Log.d(TAG, "$widgetId instance:$instanceId $message")
    }

    override fun onCreate() {
        logEvent("onCreate")
    }

    override fun onDestroy() {
        logEvent("onDestroy")
    }

    override fun getCount(): Int {
        logEvent("getCount:" + widgetEntries.size + " " + InstanceState[widgetId].listRedrawn)
        if (widgetEntries.isEmpty()) {
            InstanceState.listRedrawn(widgetId)
        }
        return widgetEntries.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < widgetEntries.size) {
            val entry = widgetEntries[position]
            val visualizer = visualizerFor(entry)
            return if (visualizer != null) {
                val views = visualizer.getRemoteViews(entry, position)
                views.setOnClickFillInIntent(R.id.event_entry, entry.newOnClickFillInIntent())
                if (position == widgetEntries.size - 1) {
                    InstanceState.listRedrawn(widgetId)
                }
                views
            } else {
                logEvent("no visualizer at:$position for $entry")
                null
            }
        }
        logEvent("no view at:" + position + ", size:" + widgetEntries.size)
        return null
    }

    private fun visualizerFor(entry: WidgetEntry<*>?): WidgetEntryVisualizer<out WidgetEntry<*>>? {
        for (visualizer in visualizers) {
            if (visualizer.isFor(entry!!)) return visualizer
        }
        return null
    }

    private val settings: InstanceSettings
        get() = AllSettings.instanceFromId(context, widgetId)

    override fun onDataSetChanged() {
        logEvent("onDataSetChanged")
        reload()
    }

    private fun reload() {
        visualizers = getVisualizers()
        widgetEntries = queryWidgetEntries(settings)
        InstanceState.listReloaded(widgetId)
        logEvent("reload, visualizers:" + visualizers.size + ", entries:" + widgetEntries.size)
    }

    private fun getVisualizers(): MutableList<WidgetEntryVisualizer<out WidgetEntry<*>>> {
        val visualizers: MutableList<WidgetEntryVisualizer<out WidgetEntry<*>>> = ArrayList()
        val dayHeaderVisualizer = DayHeaderVisualizer(
            settings.context,
            widgetId
        )
        visualizers.add(dayHeaderVisualizer)
        for (type in settings.typesOfActiveEventProviders) {
            visualizers.add(type.getVisualizer(settings.context, widgetId))
        }
        visualizers.add(LastEntryVisualizer(context, widgetId))
        return visualizers
    }

    val todaysPosition: Int
        get() {
            for (ind in 0 until widgetEntries.size - 1) {
                if (widgetEntries[ind].timeSection != TimeSection.PAST) return ind
            }
            return widgetEntries.size - 1
        }
    val tomorrowsPosition: Int
        get() {
            for (ind in 0 until widgetEntries.size - 1) {
                if (widgetEntries[ind].timeSection == TimeSection.FUTURE) return ind
            }
            return if (widgetEntries.isNotEmpty()) 0 else -1
        }

    private fun queryWidgetEntries(settings: InstanceSettings): MutableList<WidgetEntry<*>> {
        val eventEntries: MutableList<WidgetEntry<*>> = ArrayList()
        for (visualizer in visualizers) {
            eventEntries.addAll(visualizer.queryEventEntries())
        }
        eventEntries.sort()
        val noHidden: MutableList<WidgetEntry<*>> =
            eventEntries.stream().filter { obj: WidgetEntry<*>? -> obj!!.notHidden() }.collect(Collectors.toList())
        val deduplicated = if (settings.hideDuplicates) filterOutDuplicates(noHidden) else noHidden
        val widgetEntries: MutableList<WidgetEntry<*>> =
            if (settings.showDayHeaders) addDayHeaders(deduplicated) else deduplicated
        LastEntry.addLast(settings, widgetEntries)
        return widgetEntries
    }

    private fun filterOutDuplicates(inputEntries: List<WidgetEntry<*>>): MutableList<WidgetEntry<*>> {
        val deduplicated: MutableList<WidgetEntry<*>> = ArrayList()
        val hidden: MutableList<WidgetEntry<*>> = ArrayList()
        for (ind1 in inputEntries.indices) {
            val inputEntry = inputEntries[ind1]
            if (!hidden.contains(inputEntry)) {
                deduplicated.add(inputEntry)
                for (ind2 in ind1 + 1 until inputEntries.size) {
                    val entry2 = inputEntries[ind2]
                    if (!hidden.contains(entry2) && inputEntry.duplicates(entry2)) {
                        hidden.add(entry2)
                    }
                }
            }
        }
        return deduplicated
    }

    private fun addDayHeaders(listIn: List<WidgetEntry<*>>): MutableList<WidgetEntry<*>> {
        val listOut: MutableList<WidgetEntry<*>> = ArrayList()
        if (listIn.isNotEmpty()) {
            val settings = settings
            var curDayBucket = DayHeader(settings, WidgetEntryPosition.DAY_HEADER, MyClock.DATETIME_MIN)
            var pastEventsHeaderAdded = false
            var endOfListHeaderAdded = false
            for (entry in listIn) {
                val nextEntryDay = entry.entryDay
                when (entry.entryPosition) {
                    WidgetEntryPosition.PAST_AND_DUE -> if (!pastEventsHeaderAdded) {
                        curDayBucket =
                            DayHeader(settings, WidgetEntryPosition.PAST_AND_DUE_HEADER, MyClock.DATETIME_MIN)
                        listOut.add(curDayBucket)
                        pastEventsHeaderAdded = true
                    }

                    WidgetEntryPosition.END_OF_LIST -> if (!endOfListHeaderAdded) {
                        endOfListHeaderAdded = true
                        curDayBucket =
                            DayHeader(settings, WidgetEntryPosition.END_OF_LIST_HEADER, MyClock.DATETIME_MAX)
                        listOut.add(curDayBucket)
                    }

                    else -> if (!nextEntryDay.isEqual(curDayBucket.entryDay)) {
                        if (settings.showDaysWithoutEvents) {
                            addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.entryDay, nextEntryDay)
                        }
                        curDayBucket = DayHeader(settings, WidgetEntryPosition.DAY_HEADER, nextEntryDay)
                        listOut.add(curDayBucket)
                    }
                }
                listOut.add(entry)
            }
        }
        return listOut
    }

    fun logWidgetEntries(tag: String?) {
        Log.v(tag, "Widget entries: " + widgetEntries.size)
        for (ind in widgetEntries.indices) {
            val widgetEntry = widgetEntries[ind]
            Log.v(tag, String.format("%02d ", ind) + widgetEntry.toString())
        }
    }

    private fun addEmptyDayHeadersBetweenTwoDays(
        entries: MutableList<WidgetEntry<*>>,
        fromDayExclusive: DateTime?,
        toDayExclusive: DateTime?
    ) {
        var emptyDay = fromDayExclusive!!.plusDays(1)
        val today = settings.clock().now().withTimeAtStartOfDay()
        if (emptyDay.isBefore(today)) {
            emptyDay = today
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(DayHeader(settings, WidgetEntryPosition.DAY_HEADER, emptyDay))
            emptyDay = emptyDay.plusDays(1)
        }
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        val result = 14 // Actually this is maximum number of different layoutIDs
        logEvent("getViewTypeCount:$result")
        return result
    }

    override fun getItemId(position: Int): Long {
        logEvent("getItemId: $position")
        return if (position < widgetEntries.size) {
            (position + 1).toLong()
        } else 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    companion object {
        private val TAG = RemoteViewsFactory::class.java.simpleName
        val factories = ConcurrentHashMap<Int, RemoteViewsFactory>()
        private const val MAX_NUMBER_OF_WIDGETS = 100
        private const val REQUEST_CODE_ADD_EVENT = 2
        const val REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS
        const val REQUEST_CODE_PERIODIC_ALARM = REQUEST_CODE_MIDNIGHT_ALARM + MAX_NUMBER_OF_WIDGETS
        const val PACKAGE = "org.andstatus.todoagenda"
        const val ACTION_OPEN_CALENDAR = "$PACKAGE.action.OPEN_CALENDAR"
        const val ACTION_GOTO_TODAY = "$PACKAGE.action.GOTO_TODAY"
        const val ACTION_ADD_CALENDAR_EVENT = "$PACKAGE.action.ADD_CALENDAR_EVENT"
        const val ACTION_ADD_TASK = "$PACKAGE.action.ADD_TASK"
        const val ACTION_VIEW_ENTRY = "$PACKAGE.action.VIEW_ENTRY"
        const val ACTION_REFRESH = "$PACKAGE.action.REFRESH"
        const val ACTION_CONFIGURE = "$PACKAGE.action.CONFIGURE"
        const val ACTION_MIDNIGHT_ALARM = "$PACKAGE.action.MIDNIGHT_ALARM"
        const val ACTION_PERIODIC_ALARM = "$PACKAGE.action.PERIODIC_ALARM"
        fun getOnClickIntent(widgetId: Int, entryId: Long): Intent? {
            if (widgetId == 0 || entryId == 0L) return null
            val factory = factories[widgetId] ?: return null
            val entry = factory.widgetEntries.stream()
                .filter { we: WidgetEntry<*>? -> we!!.entryId == entryId }
                .findFirst().orElse(null)
            factory.logEvent("Clicked entryId:$entryId, entry: $entry")
            if (entry == null) return null
            val visualizer = factory.visualizerFor(entry)
            return visualizer?.newViewEntryIntent(entry)
        }

        fun updateWidget(context: Context, widgetId: Int) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                if (appWidgetManager == null) {
                    Log.d(TAG, "$widgetId updateWidget, appWidgetManager is null, context:$context")
                    return
                }
                val settings = AllSettings.instanceFromId(context, widgetId)
                val rv = RemoteViews(context.packageName, WidgetLayout.WIDGET_INITIAL.shadowed(settings.textShadow))
                settings.clock().updateZone()
                configureWidgetHeader(settings, rv)
                configureWidgetEntriesList(settings, rv)
                appWidgetManager.updateAppWidget(widgetId, rv)
            } catch (e: Exception) {
                Log.w(TAG, "$widgetId Exception in updateWidget, context:$context", e)
            }
        }

        private fun configureWidgetHeader(settings: InstanceSettings, rv: RemoteViews) {
            Log.d(TAG, settings.widgetId.toString() + " configureWidgetHeader, layout:" + settings.widgetHeaderLayout)
            rv.removeAllViews(R.id.header_parent)
            if (settings.widgetHeaderLayout != WidgetHeaderLayout.HIDDEN) {
                val headerView = RemoteViews(
                    settings.context.packageName,
                    settings.widgetHeaderLayout.widgetLayout?.shadowed(settings.textShadow) ?: 0
                )
                rv.addView(R.id.header_parent, headerView)
                RemoteViewsUtil.setBackgroundColor(
                    rv, R.id.action_bar,
                    settings.colors().getBackgroundColor(BackgroundColorPref.WIDGET_HEADER)
                )
                configureCurrentDate(settings, rv)
                setActionIcons(settings, rv)
                configureGotoToday(settings, rv)
                configureAddCalendarEvent(settings, rv)
                configureAddTask(settings, rv)
                configureRefresh(settings, rv)
                configureOverflowMenu(settings, rv)
            }
        }

        private fun configureCurrentDate(settings: InstanceSettings, rv: RemoteViews) {
            val viewId = R.id.calendar_current_date
            rv.setOnClickPendingIntent(viewId, getActionPendingIntent(settings, ACTION_OPEN_CALENDAR))
            val formattedDate = settings.widgetHeaderDateFormatter()
                .formatDate(settings.clock().now()).toString()
                .uppercase(Locale.getDefault())
            rv.setTextViewText(viewId, if (StringUtil.isEmpty(formattedDate)) "                    " else formattedDate)
            RemoteViewsUtil.setTextSize(settings, rv, viewId, R.dimen.widget_header_title)
            RemoteViewsUtil.setTextColor(settings, TextColorPref.WIDGET_HEADER, rv, viewId, R.attr.header)
        }

        private fun setActionIcons(settings: InstanceSettings, rv: RemoteViews) {
            val themeContext = settings.colors().getThemeContext(TextColorPref.WIDGET_HEADER)
            RemoteViewsUtil.setImageFromAttr(themeContext, rv, R.id.go_to_today, R.attr.header_action_go_to_today)
            RemoteViewsUtil.setImageFromAttr(themeContext, rv, R.id.add_event, R.attr.header_action_add_event)
            RemoteViewsUtil.setImageFromAttr(themeContext, rv, R.id.add_task, R.attr.header_action_add_task)
            RemoteViewsUtil.setImageFromAttr(themeContext, rv, R.id.refresh, R.attr.header_action_refresh)
            RemoteViewsUtil.setImageFromAttr(themeContext, rv, R.id.overflow_menu, R.attr.header_action_overflow)
            val shading = settings.colors().getShading(TextColorPref.WIDGET_HEADER)
            var alpha = 255
            if (shading == Shading.DARK || shading == Shading.LIGHT) {
                alpha = 154
            }
            RemoteViewsUtil.setAlpha(rv, R.id.go_to_today, alpha)
            RemoteViewsUtil.setAlpha(rv, R.id.add_event, alpha)
            RemoteViewsUtil.setAlpha(rv, R.id.add_task, alpha)
            RemoteViewsUtil.setAlpha(rv, R.id.refresh, alpha)
            RemoteViewsUtil.setAlpha(rv, R.id.overflow_menu, alpha)
        }

        private fun configureAddCalendarEvent(settings: InstanceSettings, rv: RemoteViews) {
            if (settings.getFirstSource(true) === OrderedEventSource.EMPTY) {
                rv.setViewVisibility(R.id.add_event, View.GONE)
            } else {
                rv.setViewVisibility(R.id.add_event, View.VISIBLE)
                rv.setOnClickPendingIntent(R.id.add_event, getActionPendingIntent(settings, ACTION_ADD_CALENDAR_EVENT))
            }
        }

        private fun configureAddTask(settings: InstanceSettings, rv: RemoteViews) {
            if (settings.getFirstSource(false) === OrderedEventSource.EMPTY) {
                rv.setViewVisibility(R.id.add_task, View.GONE)
            } else {
                rv.setViewVisibility(R.id.add_task, View.VISIBLE)
                rv.setOnClickPendingIntent(R.id.add_task, getActionPendingIntent(settings, ACTION_ADD_TASK))
            }
        }

        private fun configureRefresh(settings: InstanceSettings, rv: RemoteViews) {
            rv.setOnClickPendingIntent(R.id.refresh, getActionPendingIntent(settings, ACTION_REFRESH))
        }

        private fun configureOverflowMenu(settings: InstanceSettings, rv: RemoteViews) {
            rv.setOnClickPendingIntent(R.id.overflow_menu, getActionPendingIntent(settings, ACTION_CONFIGURE))
        }

        private fun configureWidgetEntriesList(settings: InstanceSettings, rv: RemoteViews) {
            val intent = Intent(settings.context, RemoteViewsService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.widgetId)
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
            rv.setRemoteAdapter(R.id.event_list, intent)
            rv.setPendingIntentTemplate(R.id.event_list, getActionPendingIntent(settings, ACTION_VIEW_ENTRY))
        }

        private fun configureGotoToday(settings: InstanceSettings, rv: RemoteViews) {
            rv.setOnClickPendingIntent(R.id.go_to_today, getActionPendingIntent(settings, ACTION_GOTO_TODAY))
        }

        fun getActionPendingIntent(settings: InstanceSettings, action: String): PendingIntent {
            // We need unique request codes for each widget
            val requestCode = action.hashCode() + settings.widgetId
            val intent = Intent(settings.context.applicationContext, EnvironmentChangedReceiver::class.java)
                .setAction(action)
                .setData(Uri.parse("intent:" + action.lowercase(Locale.getDefault()) + settings.widgetId))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.widgetId)
            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags += PendingIntent.FLAG_MUTABLE
            }
            return PendingIntent.getBroadcast(settings.context, requestCode, intent, flags)
        }
    }
}
