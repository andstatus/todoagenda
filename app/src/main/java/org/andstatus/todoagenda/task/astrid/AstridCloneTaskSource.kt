package org.andstatus.todoagenda.task.astrid

import android.net.Uri
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

internal interface AstridCloneTaskSource {
    val listUri: Uri
    val listColumnId: String
    val listColumnTitle: String
    val listColumnListColor: String
    val listColumnAccount: String
    fun isAllDay(dueMillisRaw: Long?): Boolean {
        return false
    }

    fun toDueMillis(dueMillisRaw: Long?, zone: DateTimeZone?): Long? {
        return dueMillisRaw
    }

    companion object {
        val GOOGLE_TASKS: AstridCloneTaskSource = object : AstridCloneTaskSource {
            override val listUri: Uri
                get() = AstridCloneTasksProvider.Companion.GOOGLE_LISTS_URI
            override val listColumnId: String
                get() = "gtl_id"
            override val listColumnTitle: String
                get() = "gtl_title"
            override val listColumnListColor: String
                get() = "gtl_color"
            override val listColumnAccount: String
                get() = "gtl_account"
        }
        val ASTRID_TASKS: AstridCloneTaskSource = object : AstridCloneTaskSource {
            override val listUri: Uri
                get() = AstridCloneTasksProvider.Companion.TASKS_LISTS_URI
            override val listColumnId: String
                get() = "cdl_id"
            override val listColumnTitle: String
                get() = "cdl_name"
            override val listColumnListColor: String
                get() = "cdl_color"
            override val listColumnAccount: String
                get() = "cda_name"

            override fun isAllDay(dueMillisRaw: Long?): Boolean {
                return dueMillisRaw != null && dueMillisRaw % 60000 <= 0
            }

            override fun toDueMillis(dueMillisRaw: Long?, zone: DateTimeZone?): Long? {
                return if (!isAllDay(dueMillisRaw)) dueMillisRaw else DateTime(dueMillisRaw, zone)
                    .withTimeAtStartOfDay().millis

                // Astrid tasks without due times are assigned a time of 12:00:00
                // see https://github.com/andstatus/todoagenda/issues/2#issuecomment-688866280
            }
        }
    }
}
