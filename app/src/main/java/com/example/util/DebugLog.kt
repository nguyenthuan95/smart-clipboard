package com.example.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLog {
    private val fmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines

    fun d(step: String, msg: String) {
        Log.d("SmartClipboardQueue", "[$step] $msg")
        val line = "${fmt.format(Date())} [$step] $msg"
        _lines.update { (it + line).takeLast(200) }
    }

    fun e(step: String, msg: String, tr: Throwable? = null) {
        Log.e("SmartClipboardQueue", "[$step] $msg", tr)
        val line = "${fmt.format(Date())} [ERR-$step] $msg ${tr?.message.orEmpty()}".trim()
        _lines.update { (it + line).takeLast(200) }
    }

    fun clear() {
        _lines.value = emptyList()
    }

    fun asText(): String = _lines.value.joinToString("\n")
}
