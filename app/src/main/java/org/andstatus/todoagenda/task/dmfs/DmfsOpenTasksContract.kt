package org.andstatus.todoagenda.task.dmfs

import android.net.Uri

object DmfsOpenTasksContract {
    const val AUTHORITY = "org.dmfs.tasks"
    const val PERMISSION = "org.dmfs.permission.READ_TASKS"

    object Tasks {
        val PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/tasks")
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DUE_DATE = "due"
        const val COLUMN_IS_ALLDAY = "is_allday"
        const val COLUMN_START_DATE = "dtstart"
        const val COLUMN_COLOR = "list_color"
        const val COLUMN_STATUS = "status"
        const val COLUMN_LIST_ID = "list_id"
        const val STATUS_COMPLETED = 2
    }

    object TaskLists {
        val PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/tasklists")
        const val COLUMN_ID = "_id"
        const val COLUMN_NAME = "list_name"
        const val COLUMN_COLOR = "list_color"
        const val COLUMN_ACCOUNT_NAME = "account_name"
    }
}
