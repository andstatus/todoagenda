package org.andstatus.todoagenda.task.astrid

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.ColorRes
import androidx.core.database.getStringOrNull
import io.vavr.control.Try
import org.andstatus.todoagenda.BuildConfig
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.task.AbstractTaskProvider
import org.andstatus.todoagenda.task.TaskEvent
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.widget.WidgetEvent
import java.util.function.Function

class AstridCloneTasksProvider private constructor(
    type: EventProviderType, context: Context, widgetId: Int, private val taskSource: AstridCloneTaskSource
) : AbstractTaskProvider(type, context, widgetId) {
    override fun queryTasks(): List<TaskEvent> {
        myContentResolver.onQueryEvents()
        val where = whereClause
        return myContentResolver.foldEvents(
            TODOAGENDA_URI,
            null,
            where,
            null,
            null,
            ArrayList()
        ) { tasks: ArrayList<TaskEvent> ->
            Function { cursor: Cursor ->
                val task = newTask(cursor)
                if (matchedFilter(task)) {
                    tasks.add(task)
                }
                tasks
            }
        }
    }

    private val whereClause: String
        get() {
            val whereBuilder = StringBuilder()
            if (filterMode == FilterMode.NORMAL_FILTER) {
                whereBuilder
                    .append(TASKS_COLUMN_COMPLETED)
                    .append(EQUALS)
                    .append(0)
                    .append(AND)
                    .append(TASKS_COLUMN_START_DATE)
                    .append(LTE)
                    .append(mEndOfTimeRange.millis)
            }
            val taskLists: MutableSet<String> = HashSet()
            for (orderedSource in settings.getActiveEventSources(type)) {
                taskLists.add(orderedSource.source.id.toString())
            }
            if (taskLists.isNotEmpty()) {
                if (whereBuilder.isNotEmpty()) {
                    whereBuilder.append(AND)
                }
                whereBuilder
                    .append(taskSource.listColumnId)
                    .append(IN)
                    .append(OPEN_BRACKET)
                    .append(TextUtils.join(",", taskLists))
                    .append(CLOSING_BRACKET)
            }
            return whereBuilder.toString()
        }

    @SuppressLint("Range")
    private fun newTask(cursor: Cursor): TaskEvent {
        val source = settings.getActiveEventSource(
            type, cursor.getInt(cursor.getColumnIndex(taskSource.listColumnId))
        )
        val task = TaskEvent(settings)
        task.setEventSource(source)
        task.setId(cursor.getLong(cursor.getColumnIndex(TASKS_COLUMN_ID)))
        task.title = cursor.getStringOrNull(cursor.getColumnIndex(TASKS_COLUMN_TITLE)) ?: ""
        val startMillis: Long? = getPositiveLongOrNull(cursor, TASKS_COLUMN_START_DATE)
        val dueMillisRaw: Long? = getPositiveLongOrNull(cursor, TASKS_COLUMN_DUE_DATE)
        task.isAllDay = taskSource.isAllDay(dueMillisRaw)
        val dueMillis = taskSource.toDueMillis(dueMillisRaw, settings.timeZone)
        task.setDates(startMillis, dueMillis)
        val priority = cursor.getInt(cursor.getColumnIndex(TASKS_COLUMN_IMPORTANCE))
        val color = context.getColor(priorityToColor(priority))
        task.color = getAsOpaque(color)
        return task
    }

    override fun fetchAvailableSources(): Try<MutableList<EventSource>> {
        return myContentResolver.foldAvailableSources(
            taskSource.listUri, null, ArrayList()
        ) { eventSources: MutableList<EventSource> -> loadList(eventSources) }
    }

    @SuppressLint("Range")
    private fun loadList(eventSources: MutableList<EventSource>): (Cursor) -> MutableList<EventSource> {
        return { cursor: Cursor ->
            val indSummary = cursor.getColumnIndex(taskSource.listColumnAccount)
            val source = EventSource(
                type,
                cursor.getInt(cursor.getColumnIndex(taskSource.listColumnId)),
                cursor.getStringOrNull(cursor.getColumnIndex(taskSource.listColumnTitle)),
                if (indSummary >= 0) cursor.getStringOrNull(indSummary) else null,
                cursor.getInt(cursor.getColumnIndex(taskSource.listColumnListColor)),
                true
            )
            eventSources.add(source)
            eventSources
        }
    }

    @ColorRes
    private fun priorityToColor(priority: Int): Int {
        return when (priority) {
            0 -> R.color.tasks_priority_high
            1 -> R.color.tasks_priority_medium
            2 -> R.color.tasks_priority_low
            else -> R.color.tasks_priority_none
        }
    }

    override fun newViewEventIntent(event: WidgetEvent): Intent {
        return IntentUtil.newViewIntent()
            .setData(ContentUris.withAppendedId(TASKS_URI, event.eventId))
    }

    override val addEventIntent: Intent
        get() = ADD_TASK_INTENT

    companion object {
        const val AUTHORITY = BuildConfig.ORG_TASKS_AUTHORITY
        const val PERMISSION = "$AUTHORITY.permission.READ_TASKS"
        private const val CONTENT_URI_STRING = "content://$AUTHORITY"
        private val TASKS_URI = Uri.parse("$CONTENT_URI_STRING/tasks")
        val TASKS_LISTS_URI: Uri = Uri.parse("$CONTENT_URI_STRING/lists")
        val GOOGLE_LISTS_URI: Uri = Uri.parse("$CONTENT_URI_STRING/google_lists")
        private val TODOAGENDA_URI: Uri = Uri.parse("$CONTENT_URI_STRING/todoagenda")
        private val ADD_TASK_INTENT = IntentUtil.newViewIntent()
            .setData(ContentUris.withAppendedId(TASKS_URI, 0))
        private const val TASKS_COLUMN_ID = "_id"
        private const val TASKS_COLUMN_TITLE = "title"
        private const val TASKS_COLUMN_DUE_DATE = "dueDate"
        private const val TASKS_COLUMN_START_DATE = "hideUntil"
        private const val TASKS_COLUMN_IMPORTANCE = "importance"
        private const val TASKS_COLUMN_COMPLETED = "completed"

        fun newTasksProvider(type: EventProviderType, context: Context, widgetId: Int): AstridCloneTasksProvider {
            return AstridCloneTasksProvider(type, context, widgetId, AstridCloneTaskSource.ASTRID_TASKS)
        }

        fun newGoogleTasksProvider(
            type: EventProviderType,
            context: Context,
            widgetId: Int
        ): AstridCloneTasksProvider {
            return AstridCloneTasksProvider(type, context, widgetId, AstridCloneTaskSource.GOOGLE_TASKS)
        }
    }
}
