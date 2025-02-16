package org.andstatus.todoagenda.prefs

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * @author yvolk@yurivolkov.com
 */
class OrderedEventSource(
    val source: EventSource,
    val order: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as OrderedEventSource
        return source == that.source
    }

    override fun hashCode(): Int = source.hashCode()

    override fun toString(): String = "order:$order, $source"

    companion object {
        private val TAG = OrderedEventSource::class.java.simpleName
        val EMPTY = OrderedEventSource(EventSource.EMPTY, 0)
        val DAY_HEADER = OrderedEventSource(EventSource.DAY_HEADER, 0)
        val LAST_ENTRY = OrderedEventSource(EventSource.LAST_ENTRY, 0)
        val CURRENT_TIME = OrderedEventSource(EventSource.CURRENT_TIME, 0)

        fun fromJsonString(sources: String?): MutableList<OrderedEventSource> =
            if (sources.isNullOrBlank()) {
                mutableListOf()
            } else {
                try {
                    fromJsonArray(JSONArray(sources))
                } catch (e: JSONException) {
                    Log.w(TAG, "Failed to parse event sources: $sources", e)
                    mutableListOf()
                }
            }

        fun fromJsonArray(jsonArray: JSONArray): MutableList<OrderedEventSource> {
            val list: MutableList<OrderedEventSource> = ArrayList()
            for (index in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.optJSONObject(index)
                val source: EventSource =
                    if (jsonObject == null) {
                        EventSource.fromStoredString(jsonArray.optString(index))
                    } else {
                        EventSource
                            .fromJson(jsonObject)
                            .toAvailable()
                    }
                if (source !== EventSource.EMPTY) {
                    add(list, source)
                }
            }
            return list
        }

        fun fromSources(sources: List<EventSource>): MutableList<OrderedEventSource> = addAll(ArrayList(), sources)

        fun addAll(
            list: MutableList<OrderedEventSource>,
            sources: List<EventSource>,
        ): MutableList<OrderedEventSource> {
            for (source in sources) {
                add(list, source)
            }
            return list
        }

        private fun add(
            list: MutableList<OrderedEventSource>,
            source: EventSource,
        ) {
            if (source !== EventSource.EMPTY) {
                list.add(OrderedEventSource(source, list.size + 1))
            }
        }

        fun toJsonString(eventSources: List<OrderedEventSource>?): String = toJsonArray(eventSources).toString()

        fun toJsonArray(sources: List<OrderedEventSource>?): JSONArray {
            val jsonObjects: MutableList<JSONObject> = ArrayList()
            for (source in sources!!) {
                jsonObjects.add(source.source.toJson())
            }
            return JSONArray(jsonObjects)
        }
    }
}
