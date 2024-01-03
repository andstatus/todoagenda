package org.andstatus.todoagenda.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.andstatus.todoagenda.prefs.ApplicationPreferences
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.provider.EventProviderType.Companion.neededPermissions

/**
 * @author yvolk@yurivolkov.com
 */
object PermissionsUtil {
    fun mustRequestPermissions(context: Context): Boolean {
        if (!ApplicationPreferences.isAskForPermissions(context) && EventProviderType.availableSources.isNotEmpty()) {
            return false
        }
        return !isTestMode && neededPermissions.isNotEmpty()
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean =
        isTestMode ||
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    val isTestMode: Boolean by lazy {
        /*
         * Based on
         * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
         */
        try {
            Class.forName("org.andstatus.todoagenda.provider.FakeCalendarContentProvider")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
