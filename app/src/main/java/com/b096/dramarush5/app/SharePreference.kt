package com.b096.dramarush5.app

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreference(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    private val gson by lazy { Gson() }

    fun clear() = prefs.edit { clear() }

    // ============ Property Delegates ============

    fun boolean(key: String, default: Boolean = false) = delegate(
        get = { prefs.getBoolean(key, default) },
        set = { prefs.edit { putBoolean(key, it) } }
    )

    fun string(key: String, default: String = "") = delegate(
        get = { prefs.getString(key, default) ?: default },
        set = { prefs.edit { putString(key, it) } }
    )

    fun int(key: String, default: Int = 0) = delegate(
        get = { prefs.getInt(key, default) },
        set = { prefs.edit { putInt(key, it) } }
    )

    fun long(key: String, default: Long = 0L) = delegate(
        get = { prefs.getLong(key, default) },
        set = { prefs.edit { putLong(key, it) } }
    )

    fun float(key: String, default: Float = 0f) = delegate(
        get = { prefs.getFloat(key, default) },
        set = { prefs.edit { putFloat(key, it) } }
    )

    internal inline fun <reified T : Enum<T>> enum(key: String, default: T) = delegate(
        get = {
            val name = prefs.getString(key, null)
            name?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
        },
        set = { prefs.edit { putString(key, it.name) } }
    )

    internal inline fun <reified T> json(key: String, default: T) = delegate(
        get = {
            val json = prefs.getString(key, null)
            json?.let { runCatching { gson.fromJson(it, T::class.java) }.getOrNull() } ?: default
        },
        set = { prefs.edit { putString(key, gson.toJson(it)) } }
    )

    // ============ Observable Property (with LiveData) ============

    fun <T : Any> observable(key: String, default: T): ObservablePreference<T> {
        return ObservablePreference(prefs, key, default)
    }

    // ============ Generic Read/Write (for RemoteConfig) ============

    @Suppress("UNCHECKED_CAST")
    internal inline fun <reified T : Any> readAny(key: String, default: T): T = when (default) {
        is Boolean -> prefs.getBoolean(key, default) as T
        is String -> (prefs.getString(key, default) ?: default) as T
        is Int -> prefs.getInt(key, default) as T
        is Long -> prefs.getLong(key, default) as T
        is Float -> prefs.getFloat(key, default) as T
        is Double -> (prefs.getString(key, null)?.toDoubleOrNull() ?: default) as T
        else -> {
            val json = prefs.getString(key, null)
            val type = object : TypeToken<T>() {}.type
            json?.let { runCatching { gson.fromJson<T>(it, type) }.getOrNull() } ?: default
        }
    }

    internal inline fun <reified T> writeAny(key: String, value: T) {
        prefs.edit {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Double -> putString(key, value.toString())
                else -> putString(key, gson.toJson(value))
            }
        }
    }

    // ============ Internal ============

    private fun <T> delegate(
          get: () -> T,
          set: (T) -> Unit
    ): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = get()
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)
    }

    /**
     * Observable preference with LiveData and Flow support.
     */
    class ObservablePreference<T : Any>(
          private val prefs: SharedPreferences,
          private val key: String,
          private val default: T
    ) {
        var value: T
            get() = getDataValue()
            set(value) = setDataValue(value)

        val liveData: LiveData<T> = object : LiveData<T>() {
            private val listener = OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) value = getDataValue()
            }

            override fun onActive() {
                super.onActive()
                value = getDataValue()
                prefs.registerOnSharedPreferenceChangeListener(listener)
            }

            override fun onInactive() {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
                super.onInactive()
            }
        }

        val flow: Flow<T> = callbackFlow {
            trySend(getDataValue())
            val listener = OnSharedPreferenceChangeListener { _, changedKey ->
                if (changedKey == key) trySend(getDataValue())
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }.distinctUntilChanged()

        @Suppress("UNCHECKED_CAST")
        private fun getDataValue(): T = when (default) {
            is Boolean -> prefs.getBoolean(key, default) as T
            is String -> (prefs.getString(key, default) ?: default) as T
            is Int -> prefs.getInt(key, default) as T
            is Long -> prefs.getLong(key, default) as T
            is Float -> prefs.getFloat(key, default) as T
            else -> default
        }

        private fun setDataValue(value: T) {
            prefs.edit {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
    }
}
