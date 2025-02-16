package org.andstatus.todoagenda.prefs

import android.graphics.Color
import android.util.Log
import org.andstatus.todoagenda.provider.EventProviderType
import org.json.JSONException
import org.json.JSONObject

class EventSource(
    val providerType: EventProviderType,
    val id: Int,
    title: String?,
    summary: String?,
    val color: Int,
    val isAvailable: Boolean,
) {
    val title: String
    val summary: String

    fun toAvailable(): EventSource {
        if (this === EMPTY || isAvailable) return this
        for (orderedSource in EventProviderType.availableSources) {
            val source = orderedSource.source
            if (source.providerType === providerType && source.id == id && source.title == title && source.summary == summary) {
                return source
            }
        }
        for (orderedSource in EventProviderType.availableSources) {
            val source = orderedSource.source
            if (source.providerType === providerType && source.id == id && source.title == title) {
                return source
            }
        }
        for (orderedSource in EventProviderType.availableSources) {
            val source = orderedSource.source
            if (source.providerType === providerType && source.title == title && source.summary == summary) {
                return source
            }
        }
        for (orderedSource in EventProviderType.availableSources) {
            val source = orderedSource.source
            if (source.providerType === providerType && source.title == title) {
                return source
            }
        }
        for (orderedSource in EventProviderType.availableSources) {
            val source = orderedSource.source
            if (source.providerType === providerType && source.id == id) {
                return source
            }
        }
        Log.i(TAG, "Unavailable source $this")
        return this
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        try {
            json.put(KEY_PROVIDER_TYPE, providerType.id)
            json.put(KEY_ID, id)
            json.put(KEY_TITLE, title)
            json.put(KEY_SUMMARY, summary)
            json.put(KEY_COLOR, color)
            json.put(KEY_IS_AVAILABLE, isAvailable)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }

    override fun toString(): String =
        if (this === EMPTY) {
            "(Empty)"
        } else {
            providerType.name + " " + title + ", " + summary + ", id:" + id +
                if (isAvailable) "" else ", unavailable"
        }

    fun toStoredString(): String = providerType.id.toString() + STORE_SEPARATOR + id

    init {
        this.title = title ?: ""
        this.summary = summary ?: ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val source = other as EventSource
        return if (id != source.id) false else providerType === source.providerType
    }

    override fun hashCode(): Int {
        var result = providerType.hashCode()
        result = 31 * result + id
        return result
    }

    companion object {
        private val TAG = EventSource::class.java.simpleName
        val EMPTY = EventSource(EventProviderType.EMPTY, 0, "Empty", "", 0, false)
        val DAY_HEADER = EventSource(EventProviderType.DAY_HEADER, 1, "Day header", "", 0, false)
        val LAST_ENTRY = EventSource(EventProviderType.LAST_ENTRY, 1, "Last entry", "", 0, false)
        val CURRENT_TIME = EventSource(EventProviderType.CURRENT_TIME, 1, "Current time", "", 0, false)
        const val STORE_SEPARATOR = ","
        private const val KEY_PROVIDER_TYPE = "providerType"
        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_SUMMARY = "summary"
        private const val KEY_COLOR = "color"
        private const val KEY_IS_AVAILABLE = "isAvailable"

        fun fromStoredString(stored: String?): EventSource {
            if (stored == null) return EMPTY
            val values = stored.split(STORE_SEPARATOR.toRegex(), limit = 2).toTypedArray()
            return when (values.size) {
                1 ->
                    fromTypeAndId(
                        EventProviderType.CALENDAR,
                        ApplicationPreferences.parseIntSafe(values[0]),
                    )

                2 ->
                    fromTypeAndId(
                        EventProviderType.fromId(ApplicationPreferences.parseIntSafe(values[0])),
                        ApplicationPreferences.parseIntSafe(values[1]),
                    )

                else -> EMPTY
            }
        }

        private fun fromTypeAndId(
            providerType: EventProviderType,
            id: Int,
        ): EventSource {
            if (providerType === EventProviderType.EMPTY || id == 0) {
                return EMPTY
            }
            for (orderedSource in EventProviderType.availableSources) {
                val source = orderedSource.source
                if (source.providerType === providerType && source.id == id) {
                    return source
                }
            }
            Log.w(TAG, "Unavailable source $providerType, id:$id")
            return EventSource(providerType, id, "(id:$id)", "", Color.RED, false)
        }

        fun fromJson(json: JSONObject?): EventSource {
            if (json == null || !json.has(KEY_PROVIDER_TYPE)) return EMPTY
            val providerType: EventProviderType = EventProviderType.fromId(json.optInt(KEY_PROVIDER_TYPE))
            val id = json.optInt(KEY_ID)
            val title = json.optString(KEY_TITLE)
            val summary = json.optString(KEY_SUMMARY)
            val color = json.optInt(KEY_COLOR)
            return if (providerType === EventProviderType.EMPTY || id == 0) {
                EMPTY
            } else {
                EventSource(providerType, id, title, summary, color, false)
            }
        }
    }
}
