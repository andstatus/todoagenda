package org.andstatus.todoagenda.util

/**
 * @author yvolk@yurivolkov.com
 */
object StringUtil {
    fun nonEmpty(value: CharSequence?): Boolean {
        return !isEmpty(value)
    }

    fun isEmpty(value: CharSequence?): Boolean {
        return value == null || value.length == 0
    }

    fun notNull(value: String?): String {
        return value ?: ""
    }
}
