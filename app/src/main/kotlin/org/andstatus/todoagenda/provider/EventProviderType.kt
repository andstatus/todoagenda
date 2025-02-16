package org.andstatus.todoagenda.provider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import org.andstatus.todoagenda.EnvironmentChangedReceiver
import org.andstatus.todoagenda.calendar.CalendarEventProvider
import org.andstatus.todoagenda.calendar.CalendarEventVisualizer
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.task.TaskVisualizer
import org.andstatus.todoagenda.task.astrid.AstridCloneTasksProvider
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider
import org.andstatus.todoagenda.util.PermissionsUtil
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.Volatile

/** All supported Event providers  */
enum class EventProviderType(
    val id: Int,
    val isCalendar: Boolean,
    val permission: String,
    private val authority: String,
) {
    EMPTY(0, true, "", ""),
    CALENDAR(1, true, Manifest.permission.READ_CALENDAR, "com.android.calendar") {
        override fun getEventProvider(
            context: Context,
            widgetId: Int,
        ): EventProvider = CalendarEventProvider(this, context, widgetId)
    },
    DMFS_OPEN_TASKS(2, false, DmfsOpenTasksContract.PERMISSION, "org.dmfs.tasks") {
        override fun getEventProvider(
            context: Context,
            widgetId: Int,
        ): EventProvider = DmfsOpenTasksProvider(this, context, widgetId)
    },
    SAMSUNG_TASKS(3, false, Manifest.permission.READ_CALENDAR, "com.android.calendar") {
        override fun getEventProvider(
            context: Context,
            widgetId: Int,
        ): EventProvider = SamsungTasksProvider(this, context, widgetId)
    },
    ASTRID_CLONE_TASKS(
        4,
        false,
        AstridCloneTasksProvider.PERMISSION,
        AstridCloneTasksProvider.AUTHORITY,
    ) {
        override fun getEventProvider(
            context: Context,
            widgetId: Int,
        ): EventProvider = AstridCloneTasksProvider.newTasksProvider(this, context, widgetId)
    },
    ASTRID_CLONE_GOOGLE_TASKS(
        5,
        false,
        AstridCloneTasksProvider.PERMISSION,
        AstridCloneTasksProvider.AUTHORITY,
    ) {
        override fun getEventProvider(
            context: Context,
            widgetId: Int,
        ): EventProvider = AstridCloneTasksProvider.newGoogleTasksProvider(this, context, widgetId)
    },
    DAY_HEADER(6, true, "", ""),
    LAST_ENTRY(7, true, "", ""),
    CURRENT_TIME(8, true, "", ""),
    ;

    val isPermissionNeeded: Boolean get() = neededPermissions.contains(permission)

    open fun getEventProvider(
        context: Context,
        widgetId: Int,
    ): EventProvider = EventProvider(this, context, widgetId)

    fun getVisualizer(
        context: Context,
        widgetId: Int,
    ): WidgetEntryVisualizer {
        val eventProvider = getEventProvider(context, widgetId)
        return if (isCalendar) CalendarEventVisualizer(eventProvider) else TaskVisualizer(eventProvider)
    }

    fun hasEventSources(): Boolean {
        for (orderedSource in availableSources) {
            if (orderedSource.source.providerType === this) return true
        }
        return false
    }

    companion object {
        private val TAG = EventProviderType::class.java.simpleName
        val availableSources: MutableList<OrderedEventSource> = CopyOnWriteArrayList()
        val neededPermissions: MutableSet<String> = CopyOnWriteArraySet()

        @Volatile
        private var initialized = false

        fun initialize(
            context: Context,
            reInitialize: Boolean,
        ) {
            if (initialized && !reInitialize) return
            forget()
            for (type in entries) {
                val provider = type.getEventProvider(context, 0)
                provider
                    .fetchAvailableSources()
                    .onSuccess { ss: List<EventSource> ->
                        Log.i(TAG, "provider " + type + ", " + (if (ss.isEmpty()) "no" else ss.size) + " sources")
                        availableSources.addAll(OrderedEventSource.fromSources(ss))
                    }.onFailure { e: Throwable ->
                        if (PermissionsUtil.isPermissionGranted(context, type.permission)) {
                            Log.i(
                                TAG,
                                "provider '$type' has granted permission ${type.permission}" +
                                    ", initialization error: ${e.message}",
                            )
                        } else {
                            Log.i(
                                TAG,
                                "provider '$type' needs permission ${type.permission}" +
                                    ", initialization error: ${e.message}",
                            )
                            neededPermissions.add(type.permission)
                        }
                    }
            }
            if (availableSources.isEmpty()) {
                ApplicationPreferences.setAskForPermissions(context, true)
            }
            initialized = true
        }

        fun fromId(id: Int): EventProviderType {
            for (type in entries) {
                if (type.id == id) return type
            }
            return EMPTY
        }

        fun forget() {
            availableSources.clear()
            neededPermissions.clear()
            initialized = false
        }

        fun registerProviderChangedReceivers(
            context: Context,
            receiver: EnvironmentChangedReceiver,
        ) {
            val registeredAuthorities: MutableSet<String> = HashSet()
            for (type in entries) {
                val authority = type.authority
                if (type.hasEventSources() && authority.length > 0 && !registeredAuthorities.contains(authority)) {
                    registeredAuthorities.add(authority)
                    registerProviderChangedReceiver(context, receiver, authority)
                }
            }
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        private fun registerProviderChangedReceiver(
            context: Context,
            receiver: EnvironmentChangedReceiver,
            authority: String,
        ) {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.intent.action.PROVIDER_CHANGED")
            intentFilter.addDataScheme("content")
            intentFilter.addDataAuthority(authority, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(receiver, intentFilter)
            }
        }
    }
}
