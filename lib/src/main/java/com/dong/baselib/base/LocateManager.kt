package com.dong.baselib.base

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import androidx.core.content.edit

object LocateManager {
    private const val PREF_NAME = "data"
    private const val KEY_LANGUAGE = "KEY_LANGUAGE"

   private var cacheLocateInit = ""
    fun saveLocale(context: Context, lang: String) {
        context.getSharedPreferences(context.packageName + PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(
                    KEY_LANGUAGE,
                    lang
                )
            }
    }
    fun initDeviceLocate(){
        cacheLocateInit = Resources.getSystem().configuration.locales[0].language
    }
    fun getDeviceLanguageCode(): String  = cacheLocateInit

    fun getPreLanguage(mContext: Context): String =
        mContext.getSharedPreferences(mContext.packageName + PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "")?: cacheLocateInit
}

private fun String.toLocaleOrNull(): Locale? {
    return try {
        val l = Locale.forLanguageTag(this)
        if (l.language.isNullOrBlank()) null else l
    } catch (_: Throwable) {
        null
    }
}

private fun LocaleList.getOrNull(index: Int): Locale? =
    if (index in 0 until this.size()) this.get(index) else null
