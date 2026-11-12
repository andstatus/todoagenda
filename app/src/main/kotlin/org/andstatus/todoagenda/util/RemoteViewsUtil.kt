package org.andstatus.todoagenda.util

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.annotation.AttrRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.FontFamily
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.colors.TextColorPref

object RemoteViewsUtil {
    private val TAG = RemoteViewsUtil::class.java.simpleName
    private const val METHOD_SET_TEXT_SIZE = "setTextSize"
    private const val METHOD_SET_TEXT_SCALE_X = "setTextScaleX"
    private const val METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor"
    private const val METHOD_SET_BACKGROUND_RESOURCE = "setBackgroundResource"
    private const val METHOD_SET_SINGLE_LINE = "setSingleLine"
    private const val METHOD_SET_ALPHA = "setAlpha"
    private const val METHOD_SET_COLOR_FILTER = "setColorFilter"
    private const val METHOD_SET_WIDTH = "setWidth"
    private const val METHOD_SET_MIN_WIDTH = "setMinWidth"
    private const val METHOD_SET_HEIGHT = "setHeight"
    private const val METHOD_SET_PAINT_FLAGS = "setPaintFlags"

    fun setEntrySpacing(
        settings: InstanceSettings,
        rv: RemoteViews,
    ) {
        val spacing = settings.entrySpacing
        if (spacing.isCompact) {
            setPadding(
                settings = settings,
                rv = rv,
                viewId = R.id.event_entry,
                leftDimenId = R.dimen.zero,
                topDimenId = R.dimen.zero,
                rightDimenId = R.dimen.zero,
                bottomDimenId = R.dimen.zero,
            )
        } else {
            val horizontalPadding = getScaledValueInPixels(settings, R.dimen.calender_padding)
            val verticalPadding =
                Math.round(
                    getScaledValueInPixels(settings, R.dimen.entry_top_bottom_padding) *
                        spacing.verticalPaddingMultiplier,
                )
            rv.setViewPadding(
                R.id.event_entry,
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                verticalPadding,
            )
        }
    }

    /**
     * Sets a text of a widget's view, applying the [FontFamily] and the [org.andstatus.todoagenda.prefs.FontWidth]
     * from the Widget's settings.
     *
     * Font family is applied with [TypefaceSpan], because [RemoteViews] has no method to set a typeface.
     * The span is parceled to the launcher, so only font families, available on the device, can be used.
     */
    fun setText(
        settings: InstanceSettings,
        rv: RemoteViews,
        viewId: Int,
        text: CharSequence,
    ) {
        rv.setTextViewText(viewId, withFontFamily(settings.fontFamily, text))
        rv.setFloat(viewId, METHOD_SET_TEXT_SCALE_X, settings.fontWidth.scaleX)
    }

    fun withFontFamily(fontFamily: FontFamily, text: CharSequence): CharSequence {
        val family = fontFamily.family ?: return text
        val spannable = SpannableString(text)
        spannable.setSpan(
            TypefaceSpan(family),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return spannable
    }

    fun setPadding(
        settings: InstanceSettings,
        rv: RemoteViews,
        @IdRes viewId: Int,
        @DimenRes leftDimenId: Int,
        @DimenRes topDimenId: Int,
        @DimenRes rightDimenId: Int,
        @DimenRes bottomDimenId: Int,
    ) {
        val leftPadding = getScaledValueInPixels(settings, leftDimenId)
        val topPadding = getScaledValueInPixels(settings, topDimenId)
        val rightPadding = getScaledValueInPixels(settings, rightDimenId)
        val bottomPadding = getScaledValueInPixels(settings, bottomDimenId)
        rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding)
    }

    fun setAlpha(
        rv: RemoteViews,
        viewId: Int,
        alpha: Int,
    ) {
        rv.setInt(viewId, METHOD_SET_ALPHA, alpha)
    }

    fun setColorFilter(rv: RemoteViews, viewId: Int, color: Int) {
        rv.setInt(viewId, METHOD_SET_COLOR_FILTER, color)
    }

    fun setHeaderButtonSize(
        settings: InstanceSettings,
        rv: RemoteViews,
        viewId: Int,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val valuePixels = getDimension(settings.context, R.dimen.widget_header_button_size)
            // density
            val scaledValue = valuePixels * settings.widgetHeaderButtonsScale.scaleValue
            rv.setViewLayoutWidth(viewId, scaledValue, TypedValue.COMPLEX_UNIT_DIP)
            rv.setViewLayoutHeight(viewId, scaledValue, TypedValue.COMPLEX_UNIT_DIP)
        }
    }

    fun setViewWidth(
        settings: InstanceSettings,
        rv: RemoteViews,
        viewId: Int,
        dimenId: Int,
    ) {
        rv.setInt(viewId, METHOD_SET_WIDTH, getScaledValueInPixels(settings, dimenId))
    }

    fun setViewMinWidth(
        settings: InstanceSettings,
        rv: RemoteViews,
        viewId: Int,
        dimenId: Int,
    ) {
        rv.setInt(viewId, METHOD_SET_MIN_WIDTH, getScaledValueInPixels(settings, dimenId))
    }

    fun setViewHeight(settings: InstanceSettings, rv: RemoteViews, viewId: Int, dimenId: Int) {
        rv.setInt(viewId, METHOD_SET_HEIGHT, getScaledValueInPixels(settings, dimenId))
    }

    fun setTextSize(settings: InstanceSettings, rv: RemoteViews, viewId: Int, dimenId: Int) {
        rv.setFloat(viewId, METHOD_SET_TEXT_SIZE, getScaledValueInScaledPixels(settings, dimenId))
    }

    fun setTextColor(
        settings: InstanceSettings,
        textColorPref: TextColorPref,
        rv: RemoteViews,
        viewId: Int,
        colorAttrId: Int
    ) {
        val color = settings.colors().getTextColor(settings.context, textColorPref, colorAttrId)
        rv.setTextColor(viewId, color)
    }

    fun setTextStrikethrough(rv: RemoteViews, viewID: Int, isStrikethrough: Boolean) {
        if (isStrikethrough) {
            rv.setInt(
                viewID,
                METHOD_SET_PAINT_FLAGS,
                Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
            )
        } else {
            rv.setInt(viewID, METHOD_SET_PAINT_FLAGS, Paint.ANTI_ALIAS_FLAG)
        }
    }

    fun setBackgroundColorFromAttr(
        context: Context?,
        rv: RemoteViews,
        viewId: Int,
        colorAttrId: Int
    ) {
        setBackgroundColor(rv, viewId, getColorValue(context, colorAttrId))
    }

    fun setBackgroundColor(rv: RemoteViews, viewId: Int, color: Int) {
        rv.setInt(viewId, METHOD_SET_BACKGROUND_COLOR, color)
    }

    fun setBackgroundResource(rv: RemoteViews, viewId: Int, @DrawableRes drawable: Int) {
        rv.setInt(viewId, METHOD_SET_BACKGROUND_RESOURCE, drawable)
    }

    private fun getScaledValueInPixels(settings: InstanceSettings, dimenId: Int): Int {
        val resValue = getDimension(settings.context, dimenId)
        return Math.round(resValue * settings.textSizeScale.scaleValue)
    }

    private fun getScaledValueInScaledPixels(settings: InstanceSettings, dimenId: Int): Float {
        val resValue = getDimension(settings.context, dimenId)
        val density = settings.context.resources.displayMetrics.density
        return resValue * settings.textSizeScale.scaleValue / density
    }

    fun getColorValue(context: Context?, @AttrRes attrId: Int): Int =
        try {
            val theme = context!!.theme
            val outValue = TypedValue()
            if (theme.resolveAttribute(attrId, outValue, true)) {
                val colorResourceId = outValue.resourceId
                try {
                    context.resources.getColor(colorResourceId, theme)
                } catch (e: Exception) {
                    Log.w(
                        TAG,
                        "context.getResources() failed to resolve color for resource Id:$colorResourceId" +
                                " derived from attribute Id:$attrId",
                        e,
                    )
                    Color.GRAY
                }
            } else {
                Log.w(TAG, "getColorValue failed to resolve color for attribute Id:$attrId")
                Color.GRAY
            }
        } catch (e: Exception) {
            Log.w(TAG, "getColorValue failed to resolve color for attribute Id:$attrId", e)
            Color.GRAY
        }

    private fun getDimension(context: Context?, dimensionResourceId: Int): Float =
        try {
            context!!.resources.getDimension(dimensionResourceId)
        } catch (_: NotFoundException) {
            Log.w(TAG, "getDimension failed for dimension resource Id:$dimensionResourceId")
            0f
        }

    /**
     * Note: Looks like "setEllipsize" is not supported for RemoteViews
     * */
    fun setMaxLines(rv: RemoteViews, viewId: Int, maxLines: Int) {
        rv.setInt(viewId, "setMaxLines", maxLines)
    }

    fun setMultiline(rv: RemoteViews, viewId: Int, multiLine: Boolean) {
        rv.setBoolean(viewId, METHOD_SET_SINGLE_LINE, !multiLine)
    }

    fun setImageFromAttr(context: Context?, rv: RemoteViews, viewId: Int, attrResId: Int) {
        val outValue = TypedValue()
        if (context!!.theme.resolveAttribute(attrResId, outValue, true)) {
            setImage(rv, viewId, outValue.resourceId)
        } else {
            Log.w(
                TAG,
                "setImageFromAttr: not found; attrResId:" + attrResId + ", resourceId:" + outValue.resourceId +
                        ", out:" + outValue + ", context:" + context,
            )
        }
    }

    fun setImage(rv: RemoteViews, viewId: Int, resId: Int) {
        rv.setImageViewResource(viewId, resId)
    }
}
