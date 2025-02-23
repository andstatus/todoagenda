package org.andstatus.todoagenda.prefs

import androidx.annotation.StringRes
import org.andstatus.todoagenda.R

/**
 * @author yvolk@yurivolkov.com
 */
enum class SnapshotMode(
    val value: String,
    @field:StringRes val valueResId: Int,
) {
    LIVE_DATA("live_data", R.string.snapshot_mode_live_data),
    SNAPSHOT_TIME("snapshot_time", R.string.snapshot_mode_time),
    SNAPSHOT_NOW("snapshot_now", R.string.snapshot_mode_now),
    ;

    val isSnapshotMode: Boolean
        get() = this == SNAPSHOT_NOW || this == SNAPSHOT_TIME
    val isLiveMode: Boolean
        get() = this == LIVE_DATA

    companion object {
        val defaultValue = LIVE_DATA

        fun fromValue(value: String?): SnapshotMode {
            for (item in entries) {
                if (item.value == value) {
                    return item
                }
            }
            return defaultValue
        }
    }
}
