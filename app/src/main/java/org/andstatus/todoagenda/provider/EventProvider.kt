package org.andstatus.todoagenda.provider

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import io.vavr.control.Try
import org.andstatus.todoagenda.MainActivity
import org.andstatus.todoagenda.prefs.EventSource
import org.andstatus.todoagenda.prefs.FilterMode
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.KeywordsFilter
import org.joda.time.DateTime
import java.util.Optional

/** Implementation of Empty event provider  */
open class EventProvider(val type: EventProviderType, val context: Context, val widgetId: Int) {
    protected val myContentResolver: MyContentResolver = MyContentResolver(type, context, widgetId)

    // Below are parameters, which may change in settings
    protected var hideBasedOnKeywordsFilter: KeywordsFilter? = null
    protected var showBasedOnKeywordsFilter: KeywordsFilter? = null
    protected var mStartOfTimeRange: DateTime? = null
    protected lateinit var mEndOfTimeRange: DateTime

    protected open fun initialiseParameters() {
        hideBasedOnKeywordsFilter = KeywordsFilter(false, settings.hideBasedOnKeywords)
        showBasedOnKeywordsFilter = KeywordsFilter(true, settings.showBasedOnKeywords)
        mStartOfTimeRange = settings.startOfTimeRange
        mEndOfTimeRange = settings.endOfTimeRange
    }

    val settings: InstanceSettings
        get() = myContentResolver.settings

    protected fun getAsOpaque(color: Int): Int {
        return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
    }

    open fun fetchAvailableSources(): Try<MutableList<EventSource>> {
        return Try.success(mutableListOf())
    }

    protected val filterMode: FilterMode
        get() = settings.filterMode
    open val addEventIntent: Intent?
        get() = MainActivity.intentToConfigure(context, widgetId)

    companion object {
        const val AND_BRACKET = " AND ("
        const val OPEN_BRACKET = "( "
        const val CLOSING_BRACKET = " )"
        const val AND = " AND "
        const val OR = " OR "
        const val IN = " IN "
        const val EQUALS = " = "
        const val NOT_EQUALS = " != "
        const val LTE = " <= "
        const val IS_NULL = " IS NULL"
        fun getPositiveLongOrNull(cursor: Cursor, columnName: String?): Long? {
            return getColumnIndex(cursor, columnName)
                .map { columnIndex: Int? -> cursor.getLong(columnIndex!!) }
                .filter { value: Long? -> value!! > 0 }
                .orElse(null)
        }

        fun getColumnIndex(cursor: Cursor, columnName: String?): Optional<Int> {
            return Optional.of(cursor.getColumnIndex(columnName))
                .filter { ind: Int -> ind >= 0 }
                .filter { ind: Int? -> cursor.getType(ind!!) != Cursor.FIELD_TYPE_NULL }
        }
    }
}
