package org.andstatus.todoagenda.prefs

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.andstatus.todoagenda.R
import org.junit.Test

/**
 * @author yvolk@yurivolkov.com
 */
class GenerateLocalizedDescriptions {
    @Test
    fun localizedDescriptions() {
        val context1 = InstrumentationRegistry.getInstrumentation().targetContext
        val languages = context1.resources.getStringArray(R.array.custom_locale_entries)
        val locales = context1.resources.getStringArray(R.array.custom_locale_values)
        for (ind in locales.indices) {
            val language = languages[ind]
            val locale = locales[ind]
            val context = context1.applicationContext as ContextWrapper
            MyLocale.setLocale(context, locale)
            val builder = StringBuilder("---- $language, $locale\n")
            builder.append(context.getText(R.string.app_description_80_chars_max).toString() + "\n\n")
            builder.append(context.getText(R.string.app_description_4000_chars_max_part01).toString() + "\n")
            builder.append(context.getText(R.string.app_description_4000_chars_max_part02).toString() + ":\n")
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part15)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part03)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part04)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part05)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part06)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part07)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part08)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part09)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part10)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part11)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part12)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part13)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part16)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part17)
            addBulleted(builder, context, R.string.app_description_4000_chars_max_part14)
            Log.i("todoagenda", builder.toString())
        }
    }

    private fun addBulleted(builder: StringBuilder, context: Context, resId: Int) {
        builder.append("* " + context.getText(resId) + "\n")
    }
}
