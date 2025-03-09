package org.andstatus.todoagenda.util

import org.andstatus.todoagenda.R
import org.andstatus.todoagenda.prefs.InstanceSettings
import org.andstatus.todoagenda.util.DateUtil.EMPTY_STRING

data class TimeUntil(
    val days: Int? = null,
    val hours: Int? = null,
    val minutes: Int? = null,
) {
    init {
        if ((hours == null).xor(minutes == null)) throw IllegalArgumentException("hours and minutes should be null simultaneously")
        if ((days != null).xor(minutes == null)) throw IllegalArgumentException("days or hours/minutes should be null")
    }

    val isZero: Boolean get() = minutes == 0 && hours == 0

    fun format(settings: InstanceSettings): String =
        when {
            isZero -> EMPTY_STRING
            days == 1 -> settings.context.getText(R.string.tomorrow).toString()
            days != null ->
                String.format(
                    settings.context.getText(R.string.in_N_days).toString(),
                    days,
                )

            else ->
                String.format(
                    settings.context.getText(R.string.in_HH_mm).toString(),
                    hours,
                    minutes,
                )
        }
}
