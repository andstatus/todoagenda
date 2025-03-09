/*
 * Copyright (c) 2019 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.andstatus.todoagenda.prefs

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextUtils
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import kotlin.concurrent.Volatile

/**
 * @author yvolk@yurivolkov.com
 */
object TestLocale {
    const val EN_US_LOCALE = "en_US"
    private const val CUSTOM_LOCALE_DEFAULT = "default"

    @Volatile
    private var mLocale: Locale? = null

    @Volatile
    private var mDefaultLocale: Locale? = null

    fun withEnUsLocale(block: () -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as ContextWrapper
        val locale = MyLocale.locale.toLanguageTag()
        try {
            if (locale != EN_US_LOCALE) {
                setLocale(context, EN_US_LOCALE)
            }
            block()
        } finally {
            if (locale != EN_US_LOCALE) {
                setLocale(context, locale)
            }
        }
    }

    fun setLocale(
        contextWrapper: ContextWrapper,
        strLocale: String?,
    ) {
        val defaultLocale =
            mDefaultLocale ?: contextWrapper.baseContext.resources.configuration.locales[0].also {
                mDefaultLocale = it
            }
        if (strLocale != CUSTOM_LOCALE_DEFAULT || mLocale != null) {
            mLocale =
                if (strLocale == CUSTOM_LOCALE_DEFAULT) {
                    null
                } else {
                    Locale(
                        localeToLanguage(strLocale),
                        localeToCountry(strLocale),
                    )
                }
            (mLocale ?: defaultLocale).also { locale ->
                MyLocale.setLocale(contextWrapper, locale)
                Locale.setDefault(locale)
                updateConfiguration(contextWrapper, locale)
            }
        }
    }

    fun localeToLanguage(locale: String?): String {
        if (TextUtils.isEmpty(locale)) {
            return ""
        }
        val indHyphen = locale!!.indexOf('-')
        return if (indHyphen < 1) {
            locale
        } else {
            locale.substring(0, indHyphen)
        }
    }

    fun localeToCountry(locale: String?): String {
        if (TextUtils.isEmpty(locale)) {
            return ""
        }
        val indHyphen = locale!!.indexOf("-r")
        return if (indHyphen < 0) {
            ""
        } else {
            locale.substring(indHyphen + 2)
        }
    }

    private fun updateConfiguration(
        contextWrapper: ContextWrapper,
        locale: Locale?,
    ) {
        val configIn = contextWrapper.baseContext.resources.configuration
        if (configIn.locales[0] != locale) {
            val configCustom = getCustomizeConfiguration(contextWrapper.baseContext, locale)
            contextWrapper.baseContext.resources.updateConfiguration(
                configCustom,
                contextWrapper.baseContext.resources.displayMetrics,
            )
        }
    }

    private fun getCustomizeConfiguration(
        context: Context,
        newLocale: Locale?,
    ): Configuration {
        val configuration = context.resources.configuration
        configuration.setLocale(newLocale)
        val localeList = LocaleList(newLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        return configuration
    }
}
