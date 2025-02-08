package org.andstatus.todoagenda.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.util.DateUtil
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Arrays
import kotlin.math.min

/**
 * Useful for logging and mocking ContentProviders
 *
 * @author yvolk@yurivolkov.com
 */
class QueryResult internal constructor(
    val providerType: EventProviderType,
    val widgetId: Int,
    val executedAt: DateTime?
) {
    var uri = Uri.EMPTY
        private set
    private var projection: Array<String> = arrayOf()
    var selection: String = ""
        private set
    private var selectionArgs: Array<String>? = arrayOf()
    private var sortOrder: String? = ""
    val rows: MutableList<QueryRow> = ArrayList()

    constructor(
        providerType: EventProviderType, settings: InstanceSettings, uri: Uri?, projection: Array<String>?,
        selection: String, selectionArgs: Array<String>?, sortOrder: String?
    ) : this(providerType, settings.widgetId, settings.clock.now()) {
        this.uri = uri
        projection?.let { this.projection = it }
        this.selection = selection
        this.selectionArgs = selectionArgs
        this.sortOrder = sortOrder
    }

    fun query(projectionIn: Array<String>?): Cursor {
        val currentProjection = projectionIn ?: projection
        val cursor = MatrixCursor(currentProjection)
        for (row in rows) {
            cursor.addRow(row.getArray(currentProjection))
        }
        return cursor
    }

    fun querySource(projection: Array<String>?): Cursor? {
        Log.i(TAG, "query for source: $providerType")
        val cursor = MatrixCursor(projection)
        when (providerType) {
            EventProviderType.CALENDAR -> {
                cursor.addRow(arrayOf<Any>(1L, TAG, 0x00FF00, "my.test@example.com"))
                return cursor
            }

            EventProviderType.DMFS_OPEN_TASKS -> {
                cursor.addRow(
                    arrayOf<Any>(
                        2L, TAG + ".open.task" + 2L, 0x0FF0000,
                        "my.task@example.com"
                    )
                )
                return cursor
            }

            EventProviderType.SAMSUNG_TASKS -> {
                cursor.addRow(arrayOf<Any>(3L, TAG + "samsung.task" + 3L, 0x0FF0000))
                return cursor
            }

            else -> {
                return null
            }
        }
    }

    fun addRow(cursor: Cursor?) {
        addRow(QueryRow.fromCursor(cursor))
    }

    fun addRow(row: QueryRow) {
        if (projection.isEmpty()) {
            projection = row.columnNames
        }
        rows.add(row)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as QueryResult
        if (uri != that.uri) return false
        if (!Arrays.equals(projection, that.projection)) return false
        if (selection != that.selection) return false
        if (!Arrays.equals(selectionArgs, that.selectionArgs)) return false
        if (sortOrder != that.sortOrder) return false
        if (rows.size != that.rows.size) return false
        for (ind in rows.indices) {
            if (rows[ind] != that.rows[ind]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + Arrays.hashCode(projection)
        result = 31 * result + selection.hashCode()
        result = 31 * result + if (selectionArgs != null) Arrays.hashCode(selectionArgs) else 0
        result = 31 * result + sortOrder.hashCode()
        for (ind in rows.indices) {
            result = 31 * result + rows[ind].hashCode()
        }
        return result
    }

    override fun toString(): String {
        return try {
            toJson().toString(2)
        } catch (e: JSONException) {
            (TAG + " Error converting to Json "
                + e.message + "; " + rows.toString())
        }
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put(KEY_PROVIDER_TYPE, providerType.id)
        json.put(KEY_EXECUTED_AT, executedAt!!.millis)
        val zone = executedAt.zone
        json.put(KEY_TIME_ZONE_ID, zone.id)
        json.put(KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getOffset(executedAt))
        json.put(KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getStandardOffset(executedAt.millis))
        json.put(KEY_URI, if (uri != null) uri.toString() else "")
        json.put(KEY_PROJECTION, arrayOfStingsToJson(projection))
        json.put(KEY_SELECTION, if (selection != null) selection else "")
        json.put(KEY_SELECTION_ARGS, arrayOfStingsToJson(selectionArgs))
        json.put(KEY_SORT_ORDER, if (sortOrder != null) sortOrder else "")
        val jsonArray = JSONArray()
        rows.take(rowsToTake("toJson", rows.size)).forEach { row ->
            jsonArray.put(row.toJson())
        }
        json.put(KEY_ROWS, jsonArray)
        return json
    }

    fun dropNullColumns(): QueryResult {
        for (row in rows) {
            row.dropNullColumns()
        }
        return this
    }

    companion object {
        private val TAG = QueryResult::class.java.simpleName
        const val MAX_ROWS_TO_STORE = 2000
        private const val KEY_ROWS = "rows"
        private const val KEY_PROVIDER_TYPE = "providerType"
        private const val KEY_EXECUTED_AT = "executedAt"
        private const val KEY_TIME_ZONE_ID = "timeZoneId"
        private const val KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "millisOffsetUtcToLocal"
        private const val KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "standardMillisOffsetUtcToLocal"
        private const val KEY_URI = "uri"
        private const val KEY_PROJECTION = "projection"
        private const val KEY_SELECTION = "selection"
        private const val KEY_SELECTION_ARGS = "selectionArgs"
        private const val KEY_SORT_ORDER = "sortOrder"

        fun rowsToTake(message: String, rowsCount: Int): Int {
            val rowsTotake = min(MAX_ROWS_TO_STORE, rowsCount)
            if (rowsTotake < rowsCount) {
                Log.i(TAG, "$message, Too many rows: $rowsCount. taking: $rowsTotake")
            }
            return rowsTotake
        }

        @Throws(JSONException::class)
        fun fromJson(json: JSONObject, widgetId: Int): QueryResult {
            val result = QueryResult(
                EventProviderType.fromId(json.getInt(KEY_PROVIDER_TYPE)),
                widgetId,
                DateTime(json.getLong(KEY_EXECUTED_AT), dateTimeZoneFromJson(json))
            )
            result.uri = Uri.parse(json.getString(KEY_URI))
            result.projection = jsonToArrayOfStrings(json.getJSONArray(KEY_PROJECTION))
            result.selection = json.getString(KEY_SELECTION)
            result.selectionArgs = jsonToArrayOfStrings(json.getJSONArray(KEY_SELECTION_ARGS))
            result.sortOrder = json.getString(KEY_SORT_ORDER)
            val jsonArray = json.getJSONArray(KEY_ROWS)
            for (ind in 0 until rowsToTake("fromJson", jsonArray.length())) {
                result.addRow(QueryRow.fromJson(jsonArray.getJSONObject(ind)))
            }
            return result
        }

        private fun dateTimeZoneFromJson(json: JSONObject): DateTimeZone {
            val zoneId = DateUtil.validatedTimeZoneId(json.optString(KEY_TIME_ZONE_ID))
            return DateTimeZone.forID(if (TextUtils.isEmpty(zoneId)) "UTC" else zoneId)
        }

        @Throws(JSONException::class)
        private fun jsonToArrayOfStrings(jsonArray: JSONArray): Array<String> {
            return Array<String>(jsonArray.length()) { ind ->
                jsonArray.getString(ind)
            }
        }

        private fun arrayOfStingsToJson(array: Array<String>?): JSONArray {
            val jsonArray = JSONArray()
            if (array != null) {
                for (item in array) {
                    jsonArray.put(item)
                }
            }
            return jsonArray
        }
    }
}
