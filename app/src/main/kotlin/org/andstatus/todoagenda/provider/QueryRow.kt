package org.andstatus.todoagenda.provider

import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.database.getStringOrNull
import org.andstatus.todoagenda.provider.QueryRow.TypedValue.CursorFieldType
import org.json.JSONException
import org.json.JSONObject

/**
 * Useful for logging and Mocking CalendarContentProvider
 *
 * @author yvolk@yurivolkov.com
 */
class QueryRow {
    private class TypedValue {
        val type: CursorFieldType
        val value: Any?
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as TypedValue
            if (type !== CursorFieldType.UNKNOWN && that.type !== CursorFieldType.UNKNOWN) {
                if (type !== that.type) return false
            }
            return !if (value != null) value.toString() != that.value.toString() else that.value != null
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + (value?.toString()?.hashCode() ?: 0)
            return result
        }

        enum class CursorFieldType(val code: Int) {
            UNKNOWN(-1),
            STRING(Cursor.FIELD_TYPE_STRING) {
                override fun columnToObject(cursor: Cursor, columnIndex: Int): Any? {
                    return cursor.getStringOrNull(columnIndex)
                }
            },
            INTEGER(Cursor.FIELD_TYPE_INTEGER) {
                override fun columnToObject(cursor: Cursor, columnIndex: Int): Any {
                    return cursor.getLong(columnIndex)
                }
            },
            BLOB(Cursor.FIELD_TYPE_BLOB) {
                override fun columnToObject(cursor: Cursor, columnIndex: Int): Any? {
                    return cursor.getBlob(columnIndex)
                }
            },
            FLOAT(Cursor.FIELD_TYPE_FLOAT) {
                override fun columnToObject(cursor: Cursor, columnIndex: Int): Any {
                    return cursor.getDouble(columnIndex)
                }
            },
            NULL(Cursor.FIELD_TYPE_NULL);

            open fun columnToObject(cursor: Cursor, columnIndex: Int): Any? {
                return null
            }

            companion object {
                fun fromColumnType(cursorColumnType: Int): CursorFieldType {
                    for (`val` in entries) {
                        if (`val`.code == cursorColumnType) {
                            return `val`
                        }
                    }
                    return UNKNOWN
                }
            }
        }

        constructor(cursor: Cursor, columnIndex: Int) {
            type = CursorFieldType.fromColumnType(cursor.getType(columnIndex))
            value = type.columnToObject(cursor, columnIndex)
        }

        constructor(any: Any?) : this(CursorFieldType.UNKNOWN, any)
        constructor(type: CursorFieldType, any: Any?) {
            this.type = type
            value = any
        }

        @Throws(JSONException::class)
        fun toJson(): JSONObject {
            val json = JSONObject()
            json.put(KEY_TYPE, type.code)
            json.put(KEY_VALUE, value)
            return json
        }

        companion object {
            private const val KEY_TYPE = "type"
            private const val KEY_VALUE = "value"
            fun fromJson(json: JSONObject): TypedValue {
                var type = CursorFieldType.UNKNOWN
                if (json.has(KEY_TYPE)) {
                    type = CursorFieldType.fromColumnType(json.optInt(KEY_TYPE))
                }
                return TypedValue(type, json.opt(KEY_VALUE))
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as QueryRow
        if (mRow.size != that.mRow.size) {
            return false
        }
        for ((key, value) in mRow) {
            if (!that.mRow.containsKey(key)) {
                return false
            }
            if (value != that.mRow[key]) {
                return false
            }
        }
        return mRow == that.mRow
    }

    override fun hashCode(): Int {
        var result = 0
        for ((_, value) in mRow) {
            result += 31 * value.hashCode()
        }
        return result
    }

    private val mRow: MutableMap<String, TypedValue> = HashMap()
    private fun setColumn(columnName: String, columnValue: Any?): QueryRow {
        mRow[columnName] = TypedValue(columnValue)
        return this
    }

    fun setCalendarId(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.CALENDAR_ID, obj)
    }

    fun setEventId(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.EVENT_ID, obj)
    }

    fun setTitle(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.TITLE, obj)
    }

    fun setBegin(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.BEGIN, obj)
    }

    fun setEnd(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.END, obj)
    }

    fun setAllDay(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.ALL_DAY, obj)
    }

    fun setEventLocation(obj: Any?): QueryRow {
        return setColumn(CalendarContract.Instances.EVENT_LOCATION, obj)
    }

    fun setDescription(obj: Any?): QueryRow {
        return setColumn(CalendarContract.Instances.DESCRIPTION, obj)
    }

    fun setHasAlarm(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.HAS_ALARM, obj)
    }

    fun setRRule(obj: Any?): QueryRow {
        return setColumn(CalendarContract.Instances.RRULE, obj)
    }

    override fun toString(): String {
        return try {
            toJson().toString(2)
        } catch (e: JSONException) {
            (TAG + " Error converting to Json "
                + e.message + "; " + mRow.toString())
        }
    }

    fun setDisplayColor(obj: Any): QueryRow {
        return setColumn(CalendarContract.Instances.DISPLAY_COLOR, obj)
    }

    val columnNames: Array<String>
        get() = mRow.keys.toTypedArray<String>()

    fun getArray(projection: Array<String>): Array<Any?> =
        Array(projection.size) { ind ->
            get(projection[ind])
        }

    private operator fun get(columnName: String?): Any? {
        return if (mRow.containsKey(columnName)) {
            mRow[columnName]!!.value
        } else null
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        val json = JSONObject()
        for ((key, value) in mRow) {
            json.put(key, value.toJson())
        }
        return json
    }

    fun dropNullColumns() {
        val it: MutableIterator<Map.Entry<String?, TypedValue>> = mRow.entries.iterator()
        while (it.hasNext()) {
            val (_, value) = it.next()
            if (value.type === CursorFieldType.NULL
                || value.value == null
            ) {
                it.remove()
            }
        }
    }

    companion object {
        private val TAG = QueryRow::class.java.simpleName
        fun fromCursor(cursor: Cursor?): QueryRow {
            val row = QueryRow()
            if (cursor != null && !cursor.isClosed) {
                for (ind in 0 until cursor.columnCount) {
                    row.mRow[cursor.getColumnName(ind)] = TypedValue(cursor, ind)
                }
            }
            return row
        }

        @Throws(JSONException::class)
        fun fromJson(json: JSONObject?): QueryRow {
            val row = QueryRow()
            if (json != null) {
                val it = json.keys()
                while (it.hasNext()) {
                    val columnName = it.next()
                    row.mRow[columnName] = TypedValue.fromJson(json.getJSONObject(columnName))
                }
            }
            return row
        }
    }
}
