package org.andstatus.todoagenda.prefs

import android.content.Context
import android.text.TextUtils
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.Writer
import java.nio.charset.Charset
import java.util.Arrays

/**
 * @author yvolk@yurivolkov.com
 */
object SettingsStorage {
    private val TAG = SettingsStorage::class.java.simpleName
    private const val BUFFER_LENGTH = 4 * 1024
    @Throws(IOException::class)
    fun saveJson(context: Context?, key: String, json: JSONObject) {
        writeStringToFile(json.toString(), jsonFile(context, key))
    }

    @Throws(IOException::class)
    fun loadJsonFromFile(context: Context?, key: String): JSONObject {
        return getJSONObject(jsonFile(context, key))
    }

    fun delete(context: Context?, key: String) {
        val file = jsonFile(context, key)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun jsonFile(context: Context?, key: String): File {
        return File(getExistingPreferencesDirectory(context), "$key.json")
    }

    private fun getExistingPreferencesDirectory(context: Context?): File {
        val dir = File(context!!.applicationInfo.dataDir, "shared_prefs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    @Throws(IOException::class)
    private fun writeStringToFile(string: String, file: File) {
        var fileOutputStream: FileOutputStream? = null
        var out: Writer? = null
        try {
            fileOutputStream = FileOutputStream(file.absolutePath, false)
            out = BufferedWriter(OutputStreamWriter(fileOutputStream, "UTF-8"))
            out.write(string)
        } finally {
            closeSilently(out)
            closeSilently(fileOutputStream)
        }
    }

    @Throws(IOException::class)
    private fun getJSONObject(file: File): JSONObject {
        if (file.exists()) {
            val fileString = utf8File2String(file)
            if (!TextUtils.isEmpty(fileString)) {
                try {
                    return JSONObject(fileString)
                } catch (e: JSONException) {
                    Log.w("getJSONObject", file.absolutePath, e)
                }
            }
        } else {
            Log.w(TAG, "The settings file doesn't exist: " + file.absolutePath)
        }
        return JSONObject()
    }

    @Throws(IOException::class)
    private fun utf8File2String(file: File): String {
        return String(getBytes(file), Charset.forName("UTF-8"))
    }

    /**
     * Reads the whole file
     */
    @Throws(IOException::class)
    private fun getBytes(file: File?): ByteArray {
        return if (file != null) {
            getBytes(FileInputStream(file))
        } else ByteArray(0)
    }

    /**
     * Read the stream into an array and close the stream
     */
    @Throws(IOException::class)
    private fun getBytes(`is`: InputStream?): ByteArray {
        val bout = ByteArrayOutputStream()
        if (`is` != null) {
            val readBuffer = ByteArray(BUFFER_LENGTH)
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
                closeSilently(`is`)
            }
        }
        return ByteArray(0)
    }

    /**
     * Reads up to 'size' bytes, starting from 'offset'
     */
    @Throws(IOException::class)
    private fun getBytes(file: File?, offset: Int, size: Int): ByteArray {
        if (file != null) {
            val `is`: InputStream = FileInputStream(file)
            val readBuffer = ByteArray(size)
            try {
                val bytesSkipped = `is`.skip(offset.toLong())
                if (bytesSkipped < offset) {
                    throw FileNotFoundException(
                        "Skipped only " + bytesSkipped
                            + " of " + offset + " bytes in file='" + file.absolutePath + "'"
                    )
                }
                val bytesRead = `is`.read(readBuffer, 0, size)
                if (bytesRead == readBuffer.size) {
                    return readBuffer
                } else if (bytesRead > 0) {
                    return Arrays.copyOf(readBuffer, bytesRead)
                }
            } finally {
                closeSilently(`is`)
            }
        }
        return ByteArray(0)
    }

    private fun closeSilently(closeable: Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                // Ignored
            }
        }
    }
}
