package org.andstatus.todoagenda.util

import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.prefs.MyLocale
import org.andstatus.todoagenda.prefs.MyLocale.APP_DEFAULT_LOCALE
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Formatter
import java.util.function.Supplier

object DateUtil {
    private const val TWELVE = "12"
    private const val AUTO = "auto"
    const val EMPTY_STRING = ""

    fun formatTime(
        settingsSupplier: Supplier<InstanceSettings>,
        time: DateTime?,
    ): String {
        if (time == null || !MyClock.isDateDefined(time)) return EMPTY_STRING
        val settings = settingsSupplier.get()
        val timeFormat = settings.timeFormat
        return if (!DateFormat.is24HourFormat(settings.context) && timeFormat == AUTO || timeFormat == TWELVE) {
            formatDateTime(
                settings,
                time,
                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_12HOUR,
            )
        } else {
            formatDateTime(
                settings,
                time,
                DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_24HOUR,
            )
        }
    }

    private fun formatDateTime(
        settings: InstanceSettings,
        dateTime: DateTime,
        flags: Int,
    ): String =
        DateUtils
            .formatDateRange(
                settings.context,
                Formatter(StringBuilder(50), MyLocale.locale),
                dateTime.millis,
                dateTime.millis,
                flags,
                settings.timeZone.id,
            ).toString()

    /**
     * Returns an empty string in a case supplied ID is not a valid Time Zone ID
     */
    fun validatedTimeZoneId(timeZoneId: String?): String {
        if (!TextUtils.isEmpty(timeZoneId)) {
            try {
                return DateTimeZone.forID(timeZoneId).id
            } catch (e: IllegalArgumentException) {
                Log.w("validatedTimeZoneId", "The time zone is not recognized: '$timeZoneId'")
            }
        }
        return ""
    }

    fun formatLogDateTime(time: Long): String {
        for (ind in 0..1) {
            // see http://stackoverflow.com/questions/16763968/android-text-format-dateformat-hh-is-not-recognized-like-with-java-text-simple
            val formatString = if (ind == 0) "yyyy-MM-dd-HH-mm-ss-SSS" else "yyyy-MM-dd-kk-mm-ss-SSS"
            val format = SimpleDateFormat(formatString, APP_DEFAULT_LOCALE)
            val buffer = StringBuffer()
            format.format(Date(time), buffer, FieldPosition(0))
            val strTime = buffer.toString()
            if (!strTime.contains("HH")) {
                return strTime
            }
        }
        return java.lang.Long.toString(time) // Fallback if above doesn't work
    }

    fun isSameDate(
        date: DateTime?,
        other: DateTime?,
    ): Boolean {
        if (date == null && other == null) return true
        return if (date == null || other == null) false else date == other
    }

    fun isSameDay(
        date: DateTime?,
        other: DateTime?,
    ): Boolean {
        if (date == null && other == null) return true
        return if (date == null || other == null) false else date.year() == other.year() && date.dayOfYear() == other.dayOfYear()
    }

    fun exactMinutesPlusMinutes(
        nowIn: DateTime,
        periodMinutes: Int,
    ): DateTime {
        val now = nowIn.plusMinutes(1)
        return DateTime(
            now.year,
            now.monthOfYear,
            now.dayOfMonth,
            now.hourOfDay,
            now.minuteOfHour,
            now.zone,
        ).plusMinutes(periodMinutes)
    }
}
