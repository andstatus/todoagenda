package org.andstatus.todoagenda.prefs

import android.os.Bundle
import android.test.mock.MockContentProvider

/**
 * @author yvolk@yurivolkov.com
 */
class FakeSettingsProvider : MockContentProvider() {
    override fun call(method: String, request: String?, args: Bundle?): Bundle? {
        return Bundle()
    }
}
