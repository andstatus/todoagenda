package org.andstatus.todoagenda

import android.util.Log
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.provider.FakeCalendarContentProvider
import org.junit.runner.Description
import org.junit.runner.notification.RunListener

/**
 * @author yvolk@yurivolkov.com
 */
class TestRunListener : RunListener() {
    init {
        Log.i(TAG, "TestRunListener created")
        EnvironmentChangedReceiver.sleep(5000)
    }

    @Throws(Exception::class)
    override fun testSuiteFinished(description: Description) {
        super.testSuiteFinished(description)
        Log.i(TAG, "Test Suite finished: $description")
        if (description.toString() == "null") restoreApp()
    }

    private fun restoreApp() {
        Log.i(TAG, "On restore app")
        EnvironmentChangedReceiver.sleep(2000)
        FakeCalendarContentProvider.Companion.tearDown()
        AllSettings.forget()
        EventProviderType.forget()
        EnvironmentChangedReceiver.forget()
        Log.i(TAG, "App restored")
    }

    companion object {
        private const val TAG = "testSuite"
    }
}
