package org.andstatus.todoagenda.task

import android.content.Context
import android.content.Intent
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.TaskScheduling
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.widget.WidgetEvent
import org.joda.time.DateTime

abstract class AbstractTaskProvider(type: EventProviderType, context: Context, widgetId: Int) :
    EventProvider(type, context, widgetId) {
    protected var now: DateTime? = null
    override fun initialiseParameters() {
        super.initialiseParameters()
        now = settings.clock.now()
    }

    fun queryEvents(): List<TaskEvent> {
        initialiseParameters()
        return if (myContentResolver.isPermissionNeeded(type) ||
            settings.getActiveEventSources(type).isEmpty()
        ) {
            emptyList()
        } else queryTasks()
    }

    abstract fun queryTasks(): List<TaskEvent>

    /**
     * @return true - include the event in the result
     */
    protected fun matchedFilter(task: TaskEvent): Boolean {
        if (filterMode == FilterMode.NO_FILTERING) return true
        if (filterMode == FilterMode.DEBUG_FILTER) {
            if (task.status == TaskStatus.COMPLETED) return false
            if (task.startDate?.isAfter(settings.endOfTimeRange) == true) return false
        }
        if (settings.taskScheduling == TaskScheduling.DATE_DUE) {
            if (!task.hasStartDate()) {
                if (task.dueDate?.isAfter(settings.endOfTimeRange) == true) return false
            }
        }
        return if (hideBasedOnKeywordsFilter!!.matched(task.title)) {
            false
        } else showBasedOnKeywordsFilter!!.matched(task.title)
    }

    abstract fun newViewEventIntent(event: WidgetEvent): Intent
}
