package org.andstatus.todoagenda.provider

import android.content.Context
import android.content.Intent
import android.util.Log
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.RemoteViewsFactory
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.provider.QueryResult.Companion.rowsToTake
import org.andstatus.todoagenda.util.DateUtil
import org.joda.time.DateTime
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Optional
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.Volatile

/**
 * @author yvolk@yurivolkov.com
 */
class QueryResultsStorage {
    val results: MutableList<QueryResult> = CopyOnWriteArrayList()
    val executedAt = AtomicReference<DateTime>(null)

    fun addResults(newResults: QueryResultsStorage) {
        for (result in newResults.results) {
            addResult(result)
        }
        setExecutedAt(newResults.getExecutedAt())
    }

    fun addResult(result: QueryResult) {
        executedAt.compareAndSet(null, result.executedAt)
        results.add(result)
    }

    fun getResults(type: EventProviderType, widgetId: Int): List<QueryResult> {
        return results
            .filter { result: QueryResult -> type === EventProviderType.EMPTY || result.providerType === type }
            .filter { result: QueryResult -> widgetId == 0 || result.widgetId == widgetId }
            .let {
                it.take(rowsToTake("$TAG:getResults", it.size))
            }
    }

    fun findLast(type: EventProviderType): Optional<QueryResult> {
        for (index in results.indices.reversed()) {
            val result = results[index]
            if (type !== EventProviderType.EMPTY && result.providerType !== type) continue
            return Optional.of(result)
        }
        return Optional.empty()
    }

    fun getResult(type: EventProviderType, index: Int): Optional<QueryResult> {
        var foundIndex = -1
        for (result in results) {
            if (type !== EventProviderType.EMPTY && result.providerType !== type) continue
            foundIndex++
            if (foundIndex == index) return Optional.of(result)
        }
        return Optional.empty()
    }

    private fun toJsonString(context: Context?, widgetId: Int): String {
        return try {
            toJson(context, widgetId, true).toString(2)
        } catch (e: JSONException) {
            "Error while formatting data $e"
        }
    }

    @Throws(JSONException::class)
    fun toJson(context: Context?, widgetId: Int, withSettings: Boolean): JSONObject {
        val resultsArray = JSONArray()
        results.take(rowsToTake("$TAG:toJson", results.size)).forEach { result ->
            if (result.widgetId == widgetId) {
                resultsArray.put(result.toJson())
            }
        }
        val widgetData: WidgetData =
            if (context == null || widgetId == 0) WidgetData.EMPTY else WidgetData.fromSettings(
                context,
                if (withSettings) AllSettings.instanceFromId(context, widgetId) else null
            )
        val json = widgetData.toJson()
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION)
        json.put(KEY_RESULTS, resultsArray)
        return json
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val results = other as QueryResultsStorage
        if (this.results.size != results.results.size) {
            return false
        }
        for (ind in this.results.indices) {
            if (this.results[ind] != results.results[ind]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 0
        for (ind in results.indices) {
            result = 31 * result + results[ind].hashCode()
        }
        return result
    }

    override fun toString(): String {
        return "$TAG:$results"
    }

    fun clear() {
        results.clear()
        executedAt.set(null)
    }

    fun setExecutedAt(date: DateTime?) {
        executedAt.set(date)
    }

    fun getExecutedAt(): DateTime? {
        return executedAt.get()
    }

    companion object {
        private val TAG = QueryResultsStorage::class.java.simpleName
        private const val KEY_RESULTS_VERSION = "resultsVersion"
        private const val RESULTS_VERSION = 3
        private const val KEY_RESULTS = "results"
        const val KEY_SETTINGS = "settings"

        @Volatile
        var storage: QueryResultsStorage? = null
            private set

        @Volatile
        private var widgetIdResultsToStore = 0
        fun store(result: QueryResult): Boolean {
            val storage = storage
            if (storage != null) {
                storage.addResult(result)
                return storage === Companion.storage
            }
            return false
        }

        fun shareEventsForDebugging(context: Context, widgetId: Int) {
            val method = "shareEventsForDebugging"
            Log.i(TAG, "$method started")
            val settings = AllSettings.instanceFromId(context, widgetId)
            val storage: QueryResultsStorage? =
                if (settings.isSnapshotMode) settings.resultsStorage else getNewResults(context, widgetId)
            if (storage == null || !storage.hasResults()) {
                Log.w(TAG, "$method; Nothing to share")
                return
            }
            val results = storage.toJsonString(context, widgetId)
            val fileName = (settings.widgetInstanceName + "-" + context.getText(R.string.app_name))
                .replace("\\W+".toRegex(), "-") +
                "-shareEvents-" + DateUtil.formatLogDateTime(System.currentTimeMillis()) +
                ".json"
            Log.d(TAG, "$method; Sharing ${results.length} bytes to $fileName")
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("application/json")
            intent.putExtra(Intent.EXTRA_SUBJECT, fileName)
            intent.putExtra(Intent.EXTRA_TEXT, results)
            context.startActivity(
                Intent.createChooser(intent, context.getText(R.string.share_events_for_debugging_title))
            )
            Log.i(TAG, "$method; Shared $results")
        }

        fun getNewResults(context: Context, widgetId: Int): QueryResultsStorage? {
            val resultsStorage: QueryResultsStorage?
            resultsStorage = try {
                setNeedToStoreResults(true, widgetId)
                val factory: RemoteViewsFactory = RemoteViewsFactory.factories.computeIfAbsent(widgetId,
                    { id: Int -> RemoteViewsFactory(context, id, false) })
                factory.onDataSetChanged()
                storage
            } finally {
                setNeedToStoreResults(false, widgetId)
            }
            return resultsStorage
        }

        fun getNeedToStoreResults(widgetId: Int): Boolean {
            return storage != null && (widgetId == 0 || widgetId == widgetIdResultsToStore)
        }

        fun setNeedToStoreResults(needToStoreResults: Boolean, widgetId: Int) {
            widgetIdResultsToStore = widgetId
            storage = if (needToStoreResults) QueryResultsStorage() else null
        }

        fun fromJson(widgetId: Int, jsonStorage: JSONObject): QueryResultsStorage {
            val resultsStorage = QueryResultsStorage()
            if (jsonStorage.has(KEY_RESULTS)) {
                try {
                    val jsonResults = jsonStorage.getJSONArray(KEY_RESULTS)
                    for (ind in 0 until jsonResults.length()) {
                        resultsStorage.addResult(
                            QueryResult.fromJson(
                                jsonResults.getJSONObject(ind),
                                widgetId
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading results", e)
                }
            }
            return resultsStorage
        }
    }
}

fun QueryResultsStorage?.hasResults(): Boolean = this?.results?.isNotEmpty() == true
