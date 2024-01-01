package org.andstatus.todoagenda.task.samsung

import android.net.Uri

object SamsungTasksContract {
    const val INTENT_EXTRA_TASK = "task"
    const val INTENT_EXTRA_SELECTED = "selected"
    const val INTENT_EXTRA_ACTION_VIEW_FOCUS = "action_view_focus"
    const val INTENT_EXTRA_DETAIL_MODE = "DetailMode"
    const val INTENT_EXTRA_LAUNCH_FROM_WIDGET = "launch_from_widget"

    object Tasks {
        val PROVIDER_URI = Uri.parse("content://com.android.calendar/syncTasks")
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "subject"
        const val COLUMN_DUE_DATE = "utc_due_date"
        const val COLUMN_COLOR = "secAccountColor"
        const val COLUMN_COMPLETE = "complete"
        const val COLUMN_DELETED = "deleted"
        const val COLUMN_LIST_ID = "accountKey"
    }

    object TaskLists {
        val PROVIDER_URI = Uri.parse("content://com.android.calendar/TasksAccounts")
        const val COLUMN_ID = "_sync_account_key"
        const val COLUMN_NAME = "displayName"
        const val COLUMN_COLOR = "secAccountColor"
    }
}
