package org.andstatus.todoagenda.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.provider.EventProviderType
import java.util.concurrent.ConcurrentSkipListSet

/**
 * @author yvolk@yurivolkov.com
 */
object PermissionsUtil {
    fun arePermissionsGranted(context: Context): Boolean {
        AllSettings.ensureLoadedFromFiles(context, false)
        for (permission in EventProviderType.neededPermissions) {
            if (isPermissionNeeded(context, permission)) {
                return false
            }
        }
        return true
    }

    private val grantedPermissions: MutableSet<String> = ConcurrentSkipListSet()
    fun isPermissionNeeded(context: Context?, permission: String?): Boolean {
        if (isTestMode || grantedPermissions.contains(permission)) return false
        val granted = ContextCompat.checkSelfPermission(context!!, permission!!) == PackageManager.PERMISSION_GRANTED
        if (granted) grantedPermissions.add(permission)
        return !granted
    }

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
