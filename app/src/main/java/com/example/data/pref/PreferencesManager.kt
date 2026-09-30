package com.example.data.pref

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("smart_clipboard_prefs", Context.MODE_PRIVATE)

    private val _historyEnabled = MutableStateFlow(prefs.getBoolean(KEY_HISTORY_ENABLED, true))
    val historyEnabled: StateFlow<Boolean> = _historyEnabled.asStateFlow()

    private val _vibrateOnPaste = MutableStateFlow(prefs.getBoolean(KEY_VIBRATE_ON_PASTE, true))
    val vibrateOnPaste: StateFlow<Boolean> = _vibrateOnPaste.asStateFlow()

    private val _enableAnimations = MutableStateFlow(prefs.getBoolean(KEY_ANIMATIONS, true))
    val enableAnimations: StateFlow<Boolean> = _enableAnimations.asStateFlow()

    private val _darkModePreference = MutableStateFlow(prefs.getString(KEY_DARK_MODE, "dark") ?: "dark")
    val darkModePreference: StateFlow<String> = _darkModePreference.asStateFlow()

    private val _queueModeEnabled = MutableStateFlow(prefs.getBoolean(KEY_QUEUE_MODE, false))
    val queueModeEnabled: StateFlow<Boolean> = _queueModeEnabled.asStateFlow()

    fun setQueueModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUEUE_MODE, enabled).apply()
        _queueModeEnabled.value = enabled
    }

    fun setHistoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HISTORY_ENABLED, enabled).apply()
        _historyEnabled.value = enabled
    }

    fun setVibrateOnPaste(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATE_ON_PASTE, enabled).apply()
        _vibrateOnPaste.value = enabled
    }

    fun setEnableAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _enableAnimations.value = enabled
    }

    fun setDarkModePreference(mode: String) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply()
        _darkModePreference.value = mode
    }

    fun isAutoDeleteEnabled(): Boolean = true
    fun getAutoDeleteDurationMinutes(): Int = 30

    companion object {
        private const val KEY_HISTORY_ENABLED = "key_history_enabled"
        private const val KEY_VIBRATE_ON_PASTE = "key_vibrate_on_paste"
        private const val KEY_ANIMATIONS = "key_animations"
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_QUEUE_MODE = "key_queue_mode"

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context).also { INSTANCE = it }
            }
        }
    }
}
