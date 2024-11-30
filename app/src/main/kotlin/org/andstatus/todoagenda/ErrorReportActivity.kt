package org.andstatus.todoagenda

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import org.andstatus.todoagenda.util.IntentUtil

/**
 * @author yvolk@yurivolkov.com
 */
class ErrorReportActivity : MyActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error_report)
        setTitle(R.string.app_name)
        val appMessage = findViewById<EditText>(R.id.appMessage)
        appMessage?.setText(intent.getStringExtra(EXTRA_APP_MESSAGE))
    }

    fun onOkButtonClick(view: View?) {
        finish()
    }

    companion object {
        val EXTRA_APP_MESSAGE: String = RemoteViewsFactory.Companion.PACKAGE + ".extra.APP_MESSAGE"
        private val TAG = ErrorReportActivity::class.java.simpleName
        fun showMessage(context: Context, message: String, exceptionToReport: Exception) {
            var msgLog = """
                 $message
                 
                 Caused by: ${exceptionToReport.javaClass},
                 ${exceptionToReport.message}
                 """.trimIndent()
            val intent = IntentUtil.newViewIntent()
                .setClass(context, ErrorReportActivity::class.java)
                .putExtra(EXTRA_APP_MESSAGE, msgLog)
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                msgLog += """
                    
                    
                    Failed to start $TAG.
                    """.trimIndent()
                Log.e(TAG, msgLog, e)
                throw RuntimeException(msgLog, e)
            }
        }
    }
}
