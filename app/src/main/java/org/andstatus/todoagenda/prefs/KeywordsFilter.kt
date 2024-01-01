package org.andstatus.todoagenda.prefs

import android.text.TextUtils

/**
 * Filter String by keywords or phrases, enclosed in single or double quotes
 *
 * @author yvolk@yurivolkov.com
 */
class KeywordsFilter(private val matchOnNoKeywords: Boolean, text: String?) {
    val keywords: MutableList<String> = ArrayList()

    init {
        if (!TextUtils.isEmpty(text)) {
            var inQuote = false
            var quote = ' '
            var atPos = 0
            while (atPos < text!!.length) {
                val separatorInd = if (inQuote) nextQuote(text, quote, atPos) else nextSeparatorInd(text, atPos)
                if (atPos > separatorInd) {
                    break
                }
                val item = text.substring(atPos, separatorInd)
                if (!TextUtils.isEmpty(item) && !keywords.contains(item)) {
                    keywords.add(item)
                }
                if (separatorInd < text.length && isQuote(text, separatorInd)) {
                    inQuote = !inQuote
                    quote = text[separatorInd]
                }
                atPos = separatorInd + 1
            }
        }
    }

    private fun isQuote(text: String?, index: Int): Boolean {
        return when (text!![index]) {
            DOUBLE_QUOTE, SINGLE_QUOTE -> true
            else -> false
        }
    }

    private fun nextQuote(text: String?, quote: Char, atPos: Int): Int {
        for (ind in atPos until text!!.length) {
            if (quote == text[ind]) {
                return ind
            }
        }
        return text.length
    }

    private fun nextSeparatorInd(text: String?, atPos: Int): Int {
        val SEPARATORS = ", " + DOUBLE_QUOTE + SINGLE_QUOTE
        for (ind in atPos until text!!.length) {
            if (SEPARATORS.indexOf(text[ind]) >= 0) {
                return ind
            }
        }
        return text.length
    }

    fun matched(s: String?): Boolean {
        if (keywords.isEmpty()) {
            return matchOnNoKeywords
        }
        if (TextUtils.isEmpty(s)) {
            return false
        }
        for (keyword in keywords) {
            if (s!!.contains(keyword)) {
                return true
            }
        }
        return false
    }

    val isEmpty: Boolean
        get() = keywords.isEmpty()

    override fun toString(): String {
        val builder = StringBuilder()
        for (keyword in keywords) {
            if (builder.length > 0) {
                builder.append(", ")
            }
            val quote = if (keyword.contains(DOUBLE_QUOTE.toString())) SINGLE_QUOTE else DOUBLE_QUOTE
            builder.append(quote.toString() + keyword + quote)
        }
        return builder.toString()
    }

    companion object {
        private const val DOUBLE_QUOTE = '"'
        private const val SINGLE_QUOTE = '\''
    }
}
