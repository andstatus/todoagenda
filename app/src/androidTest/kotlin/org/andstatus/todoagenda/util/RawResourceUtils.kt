/*
 * Based on the example: 
 * http://stackoverflow.com/questions/4087674/android-read-text-raw-resource-file
 */
package org.andstatus.todoagenda.util

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

object RawResourceUtils {
    @Throws(IOException::class)
    fun getString(context: Context, id: Int): String {
        return String(getBytes(id, context), Charset.forName("UTF-8"))
    }

    /**
     * reads resources regardless of their size
     */
    @Throws(IOException::class)
    private fun getBytes(id: Int, context: Context): ByteArray {
        val resources = context.resources
        val `is` = resources.openRawResource(id)
        val bout = ByteArrayOutputStream()
        val readBuffer = ByteArray(4 * 1024)
        return try {
            var read: Int
            do {
                read = `is`.read(readBuffer, 0, readBuffer.size)
                if (read == -1) {
                    break
                }
                bout.write(readBuffer, 0, read)
            } while (true)
            bout.toByteArray()
        } finally {
            `is`.close()
        }
    }
}
