package org.andstatus.todoagenda.task.astrid;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.ColorRes;

import org.andstatus.todoagenda.BuildConfig;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.FilterMode;
import org.andstatus.todoagenda.prefs.OrderedEventSource;
import org.andstatus.todoagenda.provider.EventProviderType;
import org.andstatus.todoagenda.task.AbstractTaskProvider;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.util.IntentUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.vavr.control.Try;

public class AstridCloneTasksProvider extends AbstractTaskProvider {

    public static final String AUTHORITY = BuildConfig.ORG_TASKS_AUTHORITY;
    public static final String PERMISSION = AUTHORITY + ".permission.READ_TASKS";
    private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    private static final Uri TASKS_URI = Uri.parse(CONTENT_URI_STRING + "/tasks");
    static final Uri TASKS_LISTS_URI = Uri.parse(CONTENT_URI_STRING + "/lists");
    static final Uri GOOGLE_LISTS_URI = Uri.parse(CONTENT_URI_STRING + "/google_lists");
    private static final Uri TODOAGENDA_URI = Uri.parse(CONTENT_URI_STRING + "/todoagenda");

    private static final Intent ADD_TASK_INTENT = IntentUtil.newViewIntent()
            .setData(ContentUris.withAppendedId(TASKS_URI, 0));

    private static final String TASKS_COLUMN_ID = "_id";
    private static final String TASKS_COLUMN_TITLE = "title";
    private static final String TASKS_COLUMN_DUE_DATE = "dueDate";
    private static final String TASKS_COLUMN_START_DATE = "hideUntil";
    private static final String TASKS_COLUMN_IMPORTANCE = "importance";
    private static final String TASKS_COLUMN_COMPLETED = "completed";
    private static final String[] PROJECTION = {TASKS_COLUMN_ID, TASKS_COLUMN_TITLE, TASKS_COLUMN_DUE_DATE,
            TASKS_COLUMN_START_DATE, TASKS_COLUMN_IMPORTANCE, TASKS_COLUMN_COMPLETED};

    private final AstridCloneTaskSource taskSource;

    public static AstridCloneTasksProvider newTasksProvider(EventProviderType type, Context context, int widgetId) {
        return new AstridCloneTasksProvider(type, context, widgetId, AstridCloneTaskSource.ASTRID_TASKS);
    }

    public static AstridCloneTasksProvider newGoogleTasksProvider(EventProviderType type, Context context, int widgetId) {
        return new AstridCloneTasksProvider(type, context, widgetId, AstridCloneTaskSource.GOOGLE_TASKS);
    }

    private AstridCloneTasksProvider(
        EventProviderType type, Context context, int widgetId, AstridCloneTaskSource taskSource) {
        super(type, context, widgetId);
        this.taskSource = taskSource;
    }

    @Override
    public List<TaskEvent> queryTasks() {
        myContentResolver.onQueryEvents();

        String where = getWhereClause();

        return myContentResolver.foldEvents(TODOAGENDA_URI, null, where, null, null,
            new ArrayList<>(), tasks -> cursor -> {
                TaskEvent task = newTask(cursor);
                if (matchedFilter(task)) {
                    tasks.add(task);
                }
                return tasks;
            });
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();

        if (getFilterMode() == FilterMode.NORMAL_FILTER) {
            whereBuilder
                .append(TASKS_COLUMN_COMPLETED)
                .append(EQUALS)
                .append(0)
                .append(AND)
                .append(TASKS_COLUMN_START_DATE)
                .append(LTE)
                .append(mEndOfTimeRange.getMillis());
        }

        Set<String> taskLists = new HashSet<>();
        for (OrderedEventSource orderedSource: getSettings().getActiveEventSources(type)) {
            taskLists.add(Integer.toString(orderedSource.source.getId()));
        }
        if (!taskLists.isEmpty()) {
            if (whereBuilder.length() > 0) {
                whereBuilder.append(AND);
            }
            whereBuilder
                .append(taskSource.getListColumnId())
                .append(IN)
                .append(OPEN_BRACKET)
                .append(TextUtils.join(",", taskLists))
                .append(CLOSING_BRACKET);
        }

        return whereBuilder.toString();
    }

    private TaskEvent newTask(Cursor cursor) {
        OrderedEventSource source = getSettings().getActiveEventSource(
            type, cursor.getInt(cursor.getColumnIndex(taskSource.getListColumnId())));
        TaskEvent task = new TaskEvent(getSettings(), getSettings().clock().getZone());
        task.setEventSource(source);
        task.setId(cursor.getLong(cursor.getColumnIndex(TASKS_COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(TASKS_COLUMN_TITLE)));

        Long startMillis = getPositiveLongOrNull(cursor, TASKS_COLUMN_START_DATE);
        Long dueMillisRaw = getPositiveLongOrNull(cursor, TASKS_COLUMN_DUE_DATE);
        task.setAllDay(taskSource.isAllDay(dueMillisRaw));
        Long dueMillis = taskSource.toDueMillis(dueMillisRaw, getSettings().clock().getZone());
        task.setDates(startMillis, dueMillis);
        int priority = cursor.getInt(cursor.getColumnIndex(TASKS_COLUMN_IMPORTANCE));
        int color = context.getColor(priorityToColor(priority));
        task.setColor(getAsOpaque(color));

        return task;
    }

    @Override
    final public Try<List<EventSource>> fetchAvailableSources() {
        return myContentResolver.foldAvailableSources(
            taskSource.getListUri(), null, new ArrayList<>(), this::loadList);
    }

    private Function<Cursor, List<EventSource>> loadList(List<EventSource> eventSources) {
        return cursor -> {
            int indSummary = cursor.getColumnIndex(taskSource.getListColumnAccount());
            EventSource source = new EventSource(
                type,
                cursor.getInt(cursor.getColumnIndex(taskSource.getListColumnId())),
                cursor.getString(cursor.getColumnIndex(taskSource.getListColumnTitle())),
                indSummary >= 0 ? cursor.getString(indSummary) : null,
                cursor.getInt(cursor.getColumnIndex(taskSource.getListColumnListColor())),
                true);
            eventSources.add(source);
            return eventSources;
        };
    }

    private @ColorRes int priorityToColor(int priority) {
        switch (priority) {
            case 0:
                return R.color.tasks_priority_high;
            case 1:
                return R.color.tasks_priority_medium;
            case 2:
                return R.color.tasks_priority_low;
            default:
                return R.color.tasks_priority_none;
        }
    }

    @Override
    final public Intent newViewEventIntent(TaskEvent event) {
        return IntentUtil.newViewIntent()
                .setData(ContentUris.withAppendedId(TASKS_URI, event.getEventId()));
    }

    @Override
    public Intent getAddEventIntent() {
        return ADD_TASK_INTENT;
    }
}
