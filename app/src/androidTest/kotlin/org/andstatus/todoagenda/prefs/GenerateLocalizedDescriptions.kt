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
        val errors: MutableList<String> = mutableListOf()
        for (ind in locales.indices) {
            val language = languages[ind]
            val locale = locales[ind]
            val context = context1.applicationContext as ContextWrapper
            TestLocale.setLocale(context, locale)
            val builder80 = StringBuilder()
            builder80.append(context.getText(R.string.app_description_80_chars_max))
            val builder4000 = StringBuilder()
            builder4000.append(context.getText(R.string.app_description_4000_chars_max_part01).toString() + "\n")
            builder4000.append(context.getText(R.string.app_description_4000_chars_max_part02).toString() + ":\n")
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part15)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part03)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part04)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part05)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part06)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part07)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part08)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part09)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part10)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part11)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part12)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part13)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part16)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part17)
            addBulleted(builder4000, context, R.string.app_description_4000_chars_max_part14)
            val string80 = builder80.toString()
            val string4000 = builder4000.toString()
            val languageStats = "$language, $locale, length80: ${string80.length}, length4000: ${string4000.length}"
            val error =
                if (string80.length > 80 || string4000.length > 4000) {
                    errors.add(languageStats)
                    "ERROR: "
                } else {
                    null
                }
            val builder =
                StringBuilder("---- ${error ?: ""}$languageStats\n")
                    .append(string80)
                    .append("\n\n")
                    .append(string4000)
            Log.i("todoagenda", builder.toString())
        }

        // TODO: fix errors and then add an assert
        if (errors.isNotEmpty()) {
            Log.e("todoagenda", "Errors found:${errors.mapIndexed { ind, str -> "\n${ind + 1}. $str" }}")
        }
    }

    private fun addBulleted(
        builder: StringBuilder,
        context: Context,
        resId: Int,
    ) {
        builder.append("* " + context.getText(resId) + "\n")
    }
}
