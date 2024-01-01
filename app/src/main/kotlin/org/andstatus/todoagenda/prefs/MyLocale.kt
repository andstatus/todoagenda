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
import java.util.Locale
import kotlin.concurrent.Volatile

/**
 * @author yvolk@yurivolkov.com
 */
object MyLocale {
    const val KEY_CUSTOM_LOCALE = "custom_locale"
    private const val CUSTOM_LOCALE_DEFAULT = "default"

    @Volatile
    private var mLocale: Locale? = null

    @Volatile
    private var mDefaultLocale: Locale? = null
    val isEnLocale: Boolean
        get() {
            var locale = mLocale
            if (locale == null) {
                locale = mDefaultLocale
            }
            return locale == null || locale.language.isEmpty() || locale.language.startsWith("en")
        }

    fun setLocale(contextWrapper: ContextWrapper) {
        setLocale(
            contextWrapper,
            ApplicationPreferences.getString(contextWrapper, KEY_CUSTOM_LOCALE, CUSTOM_LOCALE_DEFAULT)
        )
    }

    fun setLocale(contextWrapper: ContextWrapper, strLocale: String?) {
        if (mDefaultLocale == null) {
            mDefaultLocale = contextWrapper.baseContext.resources.configuration.locales[0]
        }
        if (strLocale != CUSTOM_LOCALE_DEFAULT || mLocale != null) {
            mLocale = if (strLocale == CUSTOM_LOCALE_DEFAULT) null else Locale(
                localeToLanguage(strLocale),
                localeToCountry(strLocale)
            )
            val locale = if (mLocale == null) mDefaultLocale else mLocale
            Locale.setDefault(locale)
            updateConfiguration(contextWrapper, locale)
        }
    }

    fun localeToLanguage(locale: String?): String? {
        if (TextUtils.isEmpty(locale)) {
            return ""
        }
        val indHyphen = locale!!.indexOf('-')
        return if (indHyphen < 1) {
            locale
        } else locale.substring(0, indHyphen)
    }

    fun localeToCountry(locale: String?): String {
        if (TextUtils.isEmpty(locale)) {
            return ""
        }
        val indHyphen = locale!!.indexOf("-r")
        return if (indHyphen < 0) {
            ""
        } else locale.substring(indHyphen + 2)
    }

    private fun updateConfiguration(contextWrapper: ContextWrapper, locale: Locale?) {
        val configIn = contextWrapper.baseContext.resources.configuration
        if (configIn.locales[0] != locale) {
            val configCustom = getCustomizeConfiguration(contextWrapper.baseContext, locale)
            contextWrapper.baseContext.resources.updateConfiguration(
                configCustom,
                contextWrapper.baseContext.resources.displayMetrics
            )
        }
    }

    fun onConfigurationChanged(contextWrapper: ContextWrapper, newConfig: Configuration): Configuration {
        if (mLocale == null || mDefaultLocale == null) {
            mDefaultLocale = newConfig.locales[0]
        }
        return if (mLocale == null) newConfig else getCustomizeConfiguration(contextWrapper.baseContext, mLocale)
    }

    // Based on https://stackoverflow.com/questions/39705739/android-n-change-language-programmatically/40849142
    fun wrap(context: Context): Context {
        return if (mLocale == null) context else wrap(context, mLocale)
    }

    private fun wrap(context: Context, newLocale: Locale?): ContextWrapper {
        val configuration = getCustomizeConfiguration(context, newLocale)
        return ContextWrapper(context.createConfigurationContext(configuration))
    }

    private fun getCustomizeConfiguration(context: Context, newLocale: Locale?): Configuration {
        val configuration = context.resources.configuration
        configuration.setLocale(newLocale)
        val localeList = LocaleList(newLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        return configuration
    }
}
