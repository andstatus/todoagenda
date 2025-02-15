package org.andstatus.todoagenda.task.samsung

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.core.database.getStringOrNull
import io.vavr.control.Try
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.task.AbstractTaskProvider
import org.andstatus.todoagenda.task.TaskEvent
import org.andstatus.todoagenda.util.IntentUtil
import org.andstatus.todoagenda.widget.WidgetEvent
import java.util.function.Function

class SamsungTasksProvider(
    type: EventProviderType,
    context: Context,
    widgetId: Int,
) : AbstractTaskProvider(type, context, widgetId) {
    override fun queryTasks(): List<TaskEvent> {
        myContentResolver.onQueryEvents()
        val uri = SamsungTasksContract.Tasks.PROVIDER_URI
        val projection =
            arrayOf<String>(
                SamsungTasksContract.Tasks.COLUMN_ID,
                SamsungTasksContract.Tasks.COLUMN_TITLE,
                SamsungTasksContract.Tasks.COLUMN_DUE_DATE,
                SamsungTasksContract.Tasks.COLUMN_COLOR,
                SamsungTasksContract.Tasks.COLUMN_LIST_ID,
            )
        val where = whereClause
        return myContentResolver.foldEvents<ArrayList<TaskEvent>>(
            uri!!,
            projection,
            where,
            null,
            null,
            ArrayList(),
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
            whereBuilder
                .append(SamsungTasksContract.Tasks.COLUMN_DELETED)
                .append(EQUALS)
                .append("0")
            if (filterMode == FilterMode.NORMAL_FILTER) {
                whereBuilder
                    .append(AND)
                    .append(SamsungTasksContract.Tasks.COLUMN_COMPLETE)
                    .append(
                        EQUALS,
                    ).append("0")
            }
            val taskLists: MutableSet<String> = HashSet()
            for (orderedSource in settings.getActiveEventSources(type)) {
                taskLists.add(Integer.toString(orderedSource.source.id))
            }
            if (!taskLists.isEmpty()) {
                whereBuilder.append(AND)
                whereBuilder.append(SamsungTasksContract.Tasks.COLUMN_LIST_ID)
                whereBuilder.append(" IN ( ")
                whereBuilder.append(TextUtils.join(",", taskLists))
                whereBuilder.append(CLOSING_BRACKET)
            }
            return whereBuilder.toString()
        }

    @SuppressLint("Range")
    private fun newTask(cursor: Cursor): TaskEvent {
        val source =
            settings
                .getActiveEventSource(
                    type,
                    cursor.getInt(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_LIST_ID)),
                )
        val task = TaskEvent(settings)
        task.setEventSource(source)
        task.setId(cursor.getLong(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_ID)))
        task.title =
            cursor.getStringOrNull(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_TITLE)) ?: ""
        val dueMillis: Long? =
            getPositiveLongOrNull(cursor, SamsungTasksContract.Tasks.COLUMN_DUE_DATE)
        task.setDates(null, dueMillis)
        task.color =
            getColor(
                cursor,
                cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_COLOR),
                cursor.getInt(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_LIST_ID)),
            )
        return task
    }

    override fun fetchAvailableSources(): Try<MutableList<EventSource>> {
        val projection =
            arrayOf<String>(
                SamsungTasksContract.TaskLists.COLUMN_ID,
                SamsungTasksContract.TaskLists.COLUMN_NAME,
                SamsungTasksContract.TaskLists.COLUMN_COLOR,
            )
        val taskListName = context.resources.getString(R.string.task_source_samsung)
        return myContentResolver.foldAvailableSources<MutableList<EventSource>>(
            SamsungTasksContract.TaskLists.PROVIDER_URI,
            projection,
            ArrayList<EventSource>(),
            { eventSources: MutableList<EventSource> ->
                { cursor: Cursor ->
                    val indId = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_ID)
                    val indSummary = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_NAME)
                    val indColor = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_COLOR)
                    val id = cursor.getInt(indId)
                    val source =
                        EventSource(
                            type,
                            id,
                            taskListName,
                            cursor.getStringOrNull(indSummary),
                            getColor(cursor, indColor, id),
                            true,
                        )
                    eventSources.add(source)
                    eventSources
                }
            },
        )
    }

    override fun newViewEventIntent(event: WidgetEvent): Intent =
        IntentUtil
            .newViewIntent()
            .setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId))
            .putExtra(SamsungTasksContract.INTENT_EXTRA_TASK, true)
            .putExtra(SamsungTasksContract.INTENT_EXTRA_SELECTED, event.eventId)
            .putExtra(SamsungTasksContract.INTENT_EXTRA_ACTION_VIEW_FOCUS, 0)
            .putExtra(SamsungTasksContract.INTENT_EXTRA_DETAIL_MODE, true)
            .putExtra(SamsungTasksContract.INTENT_EXTRA_LAUNCH_FROM_WIDGET, true)

    private fun getColor(
        cursor: Cursor,
        colorIdx: Int,
        accountId: Int,
    ): Int =
        if (!cursor.isNull(colorIdx)) {
            getAsOpaque(cursor.getInt(colorIdx))
        } else {
            val fixedColors = context.resources.getIntArray(R.array.task_list_colors)
            val arrayIdx = accountId % fixedColors.size
            fixedColors[arrayIdx]
        }

    override val addEventIntent: Intent
        get() = ADD_TASK_INTENT

    companion object {
        private val TAG = SamsungTasksProvider::class.java.simpleName

        // TODO: Check if the below Intent is correct
        private val ADD_TASK_INTENT =
            IntentUtil
                .newViewIntent()
                .setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, 0))
    }
}
