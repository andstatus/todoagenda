package org.andstatus.todoagenda.prefs

import android.content.Context
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

object MyLocale {
    val APP_DEFAULT_LOCALE: Locale = Locale.US
    private val TAG = MyLocale::class.java.simpleName
    private val localeRef: AtomicReference<Locale?> = AtomicReference()
    private val localePrevCurrent: AtomicReference<Locale?> = AtomicReference()
    private val localePrevDefault: AtomicReference<Locale?> = AtomicReference()
    val locale: Locale
        get() {
            val default = Locale.getDefault()
            var current = localeRef.get()
            if (current == null || localePrevCurrent.get() != current || localePrevDefault.get() != default) {
                // We see unexpected default locale changes sometimes.
                // See https://github.com/andstatus/todoagenda/issues/161
                Log.d(TAG, "current: $current, default: $default")
                if (current == null) {
                    current = default
                    localeRef.compareAndSet(null, current)
                }
                localePrevCurrent.set(current)
                localePrevDefault.set(default)
            }
            return current
        }

    fun setLocale(context: Context, locale: Locale? = null) {
        val default = Locale.getDefault()
        val oldLocale = localeRef.get()
        val newLocale = locale ?: default
        if (oldLocale == newLocale) {
            Log.d(TAG, "same locale: $newLocale by $context, default: $default")
        } else {
            localeRef.set(newLocale)
            Log.d(TAG, "set current to: $newLocale by $context, default: $default")
        }
    }

}
