package org.andstatus.todoagenda.task

import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.OrderedEventSource
import org.andstatus.todoagenda.util.StringUtil
import org.andstatus.todoagenda.widget.WidgetEvent
import org.joda.time.DateTime

class TaskEvent(private val settings: InstanceSettings) : WidgetEvent {
    override lateinit var eventSource: OrderedEventSource
        private set
    override var eventId: Long = 0
        private set
    var title: String = ""
        set(title) {
            field = StringUtil.notNull(title)
        }
    var startDate: DateTime? = null
        private set
    var isAllDay = false
    var dueDate: DateTime? = null
        private set
    var color = 0
    var status = TaskStatus.UNKNOWN

    fun setEventSource(eventSource: OrderedEventSource): TaskEvent {
        this.eventSource = eventSource
        return this
    }

    fun setId(id: Long) {
        eventId = id
    }

    fun setDates(startMillis: Long?, dueMillis: Long?) {
        startDate = toStartDate(startMillis, dueMillis)
        dueDate = toDueDate(startMillis, dueMillis)
    }

    private fun toStartDate(startMillis: Long?, dueMillis: Long?): DateTime? {
        return if (startMillis == null) null else DateTime(startMillis, settings.timeZone)
    }

    private fun toDueDate(startMillis: Long?, dueMillis: Long?): DateTime? {
        return if (dueMillis == null) null else DateTime(dueMillis, settings.timeZone)
    }

    fun hasStartDate(): Boolean {
        return startDate != null
    }

    fun hasDueDate(): Boolean {
        return dueDate != null
    }
}
