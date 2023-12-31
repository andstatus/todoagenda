/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.andstatus.todoagenda.testcompat

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import java.io.File

/**
 * A mock context which prevents its users from talking to the rest of the device while
 * stubbing enough methods to satify code that tries to talk to other packages.
 *
 */
@Deprecated("New tests should be written using the Android Testing Support Library</a>.")
class IsolatedContext(
    private val mResolver: ContentResolver, targetContext: Context?
) : ContextWrapper(targetContext) {
    private var mBroadcastIntents: MutableList<Intent> = ArrayList()
    val andClearBroadcastIntents: List<Intent>
        /** Returns the list of intents that were broadcast since the last call to this method.  */
        get() {
            val intents: List<Intent> = mBroadcastIntents
            mBroadcastIntents = ArrayList()
            return intents
        }

    override fun getContentResolver(): ContentResolver {
        // We need to return the real resolver so that MailEngine.makeRight can get to the
        // subscribed feeds provider. TODO: mock out subscribed feeds too.
        return mResolver
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        return false
    }

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? {
        return null
    }

    override fun unregisterReceiver(receiver: BroadcastReceiver) {
        // Ignore
    }

    override fun sendBroadcast(intent: Intent) {
        mBroadcastIntents.add(intent)
    }

    override fun sendOrderedBroadcast(intent: Intent, receiverPermission: String?) {
        mBroadcastIntents.add(intent)
    }

    override fun checkUriPermission(
        uri: Uri?, readPermission: String?, writePermission: String?, pid: Int,
        uid: Int, modeFlags: Int
    ): Int {
        return PackageManager.PERMISSION_GRANTED
    }

    override fun checkUriPermission(uri: Uri, pid: Int, uid: Int, modeFlags: Int): Int {
        return PackageManager.PERMISSION_GRANTED
    }

    override fun getSystemService(name: String): Any? {
        // No other services exist in this context.
        return null
    }

    override fun getFilesDir(): File {
        return File("/dev/null")
    }
}
