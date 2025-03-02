package org.andstatus.todoagenda.widget

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.layout.WidgetLayout
import org.andstatus.todoagenda.prefs.MyLocale
import org.andstatus.todoagenda.prefs.colors.TextColorPref
import org.andstatus.todoagenda.provider.EventProvider
import org.andstatus.todoagenda.provider.EventProviderType
import org.andstatus.todoagenda.util.MyClock
import org.andstatus.todoagenda.util.RemoteViewsUtil

class DayHeaderVisualizer(
    context: Context,
    widgetId: Int,
) : WidgetEntryVisualizer(EventProvider(EventProviderType.DAY_HEADER, context, widgetId)) {
    private val alignment: Alignment
    private val horizontalLineBelowDayHeader: Boolean

    init {
        alignment = Alignment.valueOf(settings.dayHeaderAlignment)
        horizontalLineBelowDayHeader = settings.horizontalLineBelowDayHeader
    }

    override fun getRemoteViews(
        eventEntry: WidgetEntry,
        position: Int,
    ): RemoteViews {
        val entry = eventEntry as DayHeader
        val rv =
            RemoteViews(
                context.packageName,
                if (horizontalLineBelowDayHeader) {
                    WidgetLayout.DAY_HEADER_SEPARATOR_BELOW.shadowed(settings.textShadow)
                } else {
                    WidgetLayout.DAY_HEADER_SEPARATOR_ABOVE.shadowed(settings.textShadow)
                },
            )
        rv.setInt(R.id.day_header_title_wrapper, "setGravity", alignment.gravity)
        val textColorPref: TextColorPref = TextColorPref.forDayHeader(entry)
        val themeContext = settings.colors().getThemeContext(textColorPref)
        RemoteViewsUtil.setBackgroundColor(rv, R.id.event_entry, settings.colors().getEntryBackgroundColor(entry))
        RemoteViewsUtil.setCompact(settings, rv)
        setTitle(position, entry, rv, textColorPref)
        setDayHeaderSeparator(position, rv, themeContext)
        return rv
    }

    private fun setTitle(
        position: Int,
        entry: DayHeader,
        rv: RemoteViews,
        textColorPref: TextColorPref,
    ) {
        val dateString = getTitleString(entry).toString().uppercase(MyLocale.locale)
        rv.setTextViewText(R.id.day_header_title, dateString)
        RemoteViewsUtil.setTextSize(settings, rv, R.id.day_header_title, R.dimen.day_header_title)
        RemoteViewsUtil.setTextColor(settings, textColorPref, rv, R.id.day_header_title, R.attr.dayHeaderTitle)
        if (settings.isCompactLayout) {
            RemoteViewsUtil.setPadding(
                settings,
                rv,
                R.id.day_header_title,
                R.dimen.zero,
                R.dimen.zero,
                R.dimen.zero,
                R.dimen.zero,
            )
        } else {
            val paddingTopId =
                if (horizontalLineBelowDayHeader) {
                    R.dimen.day_header_padding_bottom
                } else if (position ==
                    0
                ) {
                    R.dimen.day_header_padding_top_first
                } else {
                    R.dimen.day_header_padding_top
                }
            val paddingBottomId =
                if (horizontalLineBelowDayHeader) R.dimen.day_header_padding_top else R.dimen.day_header_padding_bottom
            RemoteViewsUtil.setPadding(
                settings,
                rv,
                R.id.day_header_title,
                R.dimen.day_header_padding_left,
                paddingTopId,
                R.dimen.day_header_padding_right,
                paddingBottomId,
            )
        }
    }

    fun getTitleString(entry: DayHeader): CharSequence =
        when (entry.entryPosition) {
            WidgetEntryPosition.PAST_AND_DUE_HEADER -> context.getString(R.string.past_header)
            WidgetEntryPosition.END_OF_LIST_HEADER -> context.getString(R.string.end_of_list_header)
            else ->
                if (MyClock.isDateDefined(entry.entryDate)) {
                    settings
                        .dayHeaderDateFormatter()
                        .formatDate(entry.entryDate)
                } else {
                    "??? " + entry.entryPosition
                }
        }

    private fun setDayHeaderSeparator(
        position: Int,
        rv: RemoteViews,
        shadingContext: ContextThemeWrapper?,
    ) {
        val viewId = R.id.day_header_separator
        if (horizontalLineBelowDayHeader) {
            RemoteViewsUtil.setBackgroundColorFromAttr(shadingContext, rv, viewId, R.attr.dayHeaderSeparator)
        } else {
            if (position == 0) {
                rv.setViewVisibility(viewId, View.GONE)
            } else {
                rv.setViewVisibility(viewId, View.VISIBLE)
                RemoteViewsUtil.setBackgroundColorFromAttr(shadingContext, rv, viewId, R.attr.dayHeaderSeparator)
            }
        }
    }
}
