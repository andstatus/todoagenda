package org.andstatus.todoagenda.task.dmfs

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.text.TextUtils
import androidx.core.database.getStringOrNull
import io.vavr.control.Try
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.task.AbstractTaskProvider
import org.andstatus.todoagenda.task.TaskEvent
import org.andstatus.todoagenda.task.TaskStatus
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.widget.WidgetEvent
import java.util.function.Function

class DmfsOpenTasksProvider(type: EventProviderType, context: Context, widgetId: Int) :
    AbstractTaskProvider(type, context, widgetId) {
    override fun queryTasks(): List<TaskEvent> {
        myContentResolver.onQueryEvents()
        val projection = arrayOf(
            DmfsOpenTasksContract.Tasks.COLUMN_LIST_ID,
            DmfsOpenTasksContract.Tasks.COLUMN_ID,
            DmfsOpenTasksContract.Tasks.COLUMN_TITLE,
            DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE,
            DmfsOpenTasksContract.Tasks.COLUMN_START_DATE,
            DmfsOpenTasksContract.Tasks.COLUMN_IS_ALLDAY,
            DmfsOpenTasksContract.Tasks.COLUMN_COLOR,
            DmfsOpenTasksContract.Tasks.COLUMN_STATUS
        )
        val where = whereClause
        return myContentResolver.foldEvents(
            DmfsOpenTasksContract.Tasks.PROVIDER_URI,
            projection,
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
                whereBuilder.append(DmfsOpenTasksContract.Tasks.COLUMN_STATUS)
                    .append(NOT_EQUALS)
                    .append(DmfsOpenTasksContract.Tasks.STATUS_COMPLETED)
                whereBuilder.append(
                    AND_BRACKET +
                        DmfsOpenTasksContract.Tasks.COLUMN_START_DATE + LTE + mEndOfTimeRange.millis +
                        OR + DmfsOpenTasksContract.Tasks.COLUMN_START_DATE + IS_NULL +
                        CLOSING_BRACKET
                )
            }
            val taskLists: MutableSet<String> = HashSet()
            for (orderedSource in settings.getActiveEventSources(type)) {
                taskLists.add(orderedSource.source.id.toString())
            }
            if (taskLists.isNotEmpty()) {
                if (whereBuilder.isNotEmpty()) {
                    whereBuilder.append(AND)
                }
                whereBuilder.append(DmfsOpenTasksContract.Tasks.COLUMN_LIST_ID)
                whereBuilder.append(" IN ( ")
                whereBuilder.append(TextUtils.join(",", taskLists))
                whereBuilder.append(CLOSING_BRACKET)
            }
            return whereBuilder.toString()
        }

    @SuppressLint("Range")
    private fun newTask(cursor: Cursor): TaskEvent {
        val source = settings
            .getActiveEventSource(
                type,
                cursor.getInt(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_LIST_ID))
            )
        val task = TaskEvent(settings)
        task.setEventSource(source)
        task.setId(cursor.getLong(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_ID)))
        task.title =
            cursor.getStringOrNull(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_TITLE)) ?: ""
        val startMillis: Long? =
            getPositiveLongOrNull(cursor, DmfsOpenTasksContract.Tasks.COLUMN_START_DATE)
        val allDayEventIdx = cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_IS_ALLDAY)
        task.isAllDay = !cursor.isNull(allDayEventIdx) && cursor.getInt(allDayEventIdx) != 0
        val dueMillis: Long? =
            getPositiveLongOrNull(cursor, DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE)
        task.setDates(startMillis, dueMillis)
        task.color =
            getAsOpaque(cursor.getInt(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_COLOR)))
        task.status = loadStatus(cursor)
        return task
    }

    private fun loadStatus(cursor: Cursor): TaskStatus {
        val columnIndex = cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_STATUS)
        return if (columnIndex < 0) TaskStatus.UNKNOWN else when (cursor.getInt(columnIndex)) {
            0 -> TaskStatus.NEEDS_ACTION
            1 -> TaskStatus.IN_PROGRESS
            2 -> TaskStatus.COMPLETED
            3 -> TaskStatus.CANCELLED
            else -> TaskStatus.UNKNOWN
        }
    }

    override fun fetchAvailableSources(): Try<MutableList<EventSource>> {
        val projection = arrayOf(
            DmfsOpenTasksContract.TaskLists.COLUMN_ID,
            DmfsOpenTasksContract.TaskLists.COLUMN_NAME,
            DmfsOpenTasksContract.TaskLists.COLUMN_COLOR,
            DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME
        )
        return myContentResolver.foldAvailableSources(
            DmfsOpenTasksContract.TaskLists.PROVIDER_URI,
            projection,
            ArrayList()
        ) { eventSources: MutableList<EventSource> ->
            { cursor: Cursor ->
                val indId = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_ID)
                val indTitle = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_NAME)
                val indColor = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_COLOR)
                val indSummary = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME)
                val source = EventSource(
                    type, cursor.getInt(indId), cursor.getStringOrNull(indTitle),
                    cursor.getStringOrNull(indSummary), cursor.getInt(indColor), true
                )
                eventSources.add(source)
                eventSources
            }
        }
    }

    override fun newViewEventIntent(event: WidgetEvent): Intent {
        return IntentUtil.newViewIntent()
            .setData(ContentUris.withAppendedId(DmfsOpenTasksContract.Tasks.PROVIDER_URI, event.eventId))
    }

    override val addEventIntent: Intent
        get() = ADD_TASK_INTENT

    companion object {
        private val ADD_TASK_INTENT = IntentUtil.newIntent(Intent.ACTION_INSERT)
            .setDataAndType(DmfsOpenTasksContract.Tasks.PROVIDER_URI, "vnd.android.cursor.dir/org.dmfs.tasks.tasks")
    }
}
