package org.andstatus.todoagenda;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.andstatus.todoagenda.util.IntentUtil;

/**
 * @author yvolk@yurivolkov.com
 */
public class ErrorReportActivity extends AppCompatActivity {
    public static final String EXTRA_APP_MESSAGE = RemoteViewsFactory.PACKAGE + ".extra.APP_MESSAGE";
    private static final String TAG = ErrorReportActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_report);

        EditText appMessage = findViewById(R.id.appMessage);
        if (appMessage != null) {
            appMessage.setText(getIntent().getStringExtra(EXTRA_APP_MESSAGE));
        }
    }

    public void onOkButtonClick(View view) {
        finish();
    }

    public static void showMessage(Context context, String message, @NonNull Exception exceptionToReport) {
        String msgLog = message + "\n\n" +
                "Caused by: " + exceptionToReport.getClass() + ",\n" + exceptionToReport.getMessage();
        Intent intent = IntentUtil.newViewIntent()
            .setClass(context, ErrorReportActivity.class)
            .putExtra(EXTRA_APP_MESSAGE, msgLog);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            msgLog += "\n\nFailed to start " + TAG + ".";
            Log.e(TAG, msgLog, e);
            throw new RuntimeException(msgLog, e);
        }
    }
}
