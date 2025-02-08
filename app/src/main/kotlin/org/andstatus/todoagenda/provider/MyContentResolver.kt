package org.andstatus.todoagenda.provider

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import io.vavr.control.Try
import org.andstatus.todoagenda.prefs.AllSettings
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.provider.QueryResult.Companion.rowsToTake
import java.util.Arrays
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

/**
 * Testing and mocking Calendar and Tasks Providers
 *
 * @author yvolk@yurivolkov.com
 */
class MyContentResolver(val type: EventProviderType, val context: Context, val widgetId: Int) {
    private val requestsCounter = AtomicInteger()
    val settings: InstanceSettings by lazy {
        AllSettings.instanceFromId(
            context, widgetId
        )
    }

    fun isPermissionNeeded(providerType: EventProviderType): Boolean =
        settings.isLiveMode && providerType.isPermissionNeeded

    fun <R> foldAvailableSources(
        uri: Uri, projection: Array<String>?,
        identity: R, foldingFunction: (R) -> (Cursor) -> R
    ): Try<R> {
        var folded = identity
        try {
            queryAvailableSources(uri, projection).use { cursor ->
                if (cursor != null) {
                    for (i in 0 until cursor.count) {
                        cursor.moveToPosition(i)
                        folded = foldingFunction(folded)(cursor)
                    }
                }
            }
        } catch (e: SecurityException) {
            return Try.failure(e)
        } catch (e: IllegalArgumentException) {
            Log.d(type.name, widgetId.toString() + " " + e.message)
        } catch (e: Exception) {
            Log.w(
                type.name, widgetId.toString() + " Failed to fetch available sources" +
                    " uri:" + uri +
                    ", projection:" + Arrays.toString(projection), e
            )
        }
        return Try.success(folded)
    }

    private fun queryAvailableSources(uri: Uri, projection: Array<String>?): Cursor? {
        val cursor: Cursor? =
            if (widgetId != 0 && settings.isSnapshotMode) {
                settings.resultsStorage!!
                    .getResult(type, requestsCounter.incrementAndGet() - 1)
                    .map { r: QueryResult? -> r!!.querySource(projection) }
                    .orElse(null)
            } else {
                context.contentResolver
                    .query(uri, projection, null, null, null)
            }
        Log.d(
            "queryAvailableSources", "URI:" + uri + ", projection:" + Arrays.toString(projection) +
                ", result:" + cursor
        )
        return cursor
    }

    fun onQueryEvents() {
        requestsCounter.set(0)
    }

    fun <R> foldEvents(
        uri: Uri, projection: Array<String>?, selection: String,
        selectionArgs: Array<String>?, sortOrder: String?,
        identity: R, foldingFunction: Function<R, Function<Cursor, R>>
    ): R {
        var folded = identity
        val needToStoreResults: Boolean = QueryResultsStorage.getNeedToStoreResults(widgetId)
        val result =
            if (needToStoreResults) QueryResult(type, settings, uri, projection, selection, null, sortOrder) else null
        try {
            queryForEvents(uri, projection, selection, selectionArgs, sortOrder).use { cursor ->
                if (cursor != null) {
                    for (i in 0 until rowsToTake("queryForEvents", cursor.count)) {
                        cursor.moveToPosition(i)
                        if (needToStoreResults) result!!.addRow(cursor)
                        folded = foldingFunction.apply(folded).apply(cursor)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(
                type.name, widgetId.toString() + " Failed to query events" +
                    " uri:" + uri +
                    ", projection:" + projection.contentToString() +
                    ", selection:" + selection +
                    ", args:" + selectionArgs.contentToString() +
                    ", sort:" + sortOrder, e
            )
        }
        if (needToStoreResults) QueryResultsStorage.store(result!!.dropNullColumns())
        return folded
    }

    private fun queryForEvents(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        return if (widgetId != 0 && settings.isSnapshotMode) settings.resultsStorage!!.getResult(
            type,
            requestsCounter.incrementAndGet() - 1
        )
            .map { r: QueryResult? -> r!!.query(projection) }
            .orElse(null) else context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
    }
}
