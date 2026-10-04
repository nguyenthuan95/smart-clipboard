package com.example.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.SmartClipboardApp
import kotlinx.coroutines.runBlocking

class SaveClipActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var textToSave: String? = null

        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            textToSave = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        } else if (intent.action == Intent.ACTION_SEND) {
            textToSave = intent.getStringExtra(Intent.EXTRA_TEXT)
        }

        if (!textToSave.isNullOrBlank()) {
            val app = SmartClipboardApp.instance
            runBlocking {
                val clipId = app.repository.saveCopiedText(textToSave)
                if (app.preferences.queueModeEnabled.value) {
                    app.queueRepository.enqueue(textToSave, clipboardId = if (clipId > 0) clipId else null)
                }
            }
            Toast.makeText(this, "✓ Đã lưu vào Smart Clipboard", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
