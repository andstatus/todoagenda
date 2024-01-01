package org.andstatus.todoagenda.util

import android.content.Intent

object IntentUtil {
    fun newViewIntent(): Intent {
        return newIntent(Intent.ACTION_VIEW)
    }

    fun newIntent(action: String?): Intent {
        return Intent(action)
            .setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
    }

    fun copyStringExtra(intentFrom: Intent?, intentTo: Intent?, extra: String?) {
        if (intentFrom != null && intentTo != null) {
            val value = intentFrom.getStringExtra(extra)
            if (StringUtil.nonEmpty(value)) {
                intentTo.putExtra(extra, value)
            }
        }
    }
}
