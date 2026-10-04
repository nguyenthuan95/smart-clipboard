package com.example.ui.activity

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.SmartClipboardApp
import kotlinx.coroutines.runBlocking

class ProcessTextActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
        val queueRepo = SmartClipboardApp.instance.queueRepository
        val nextItem = runBlocking { queueRepo.getNext() }

        if (nextItem != null) {
            runBlocking { queueRepo.advanceNext() }

            if (!isReadOnly) {
                // Return text to the calling app via Android's native text replacement
                val resultIntent = Intent().apply {
                    putExtra(Intent.EXTRA_PROCESS_TEXT, nextItem.text)
                }
                setResult(RESULT_OK, resultIntent)
            } else {
                // If the target field is read-only, copy to system clipboard
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                cm?.setPrimaryClip(ClipData.newPlainText("Smart Clipboard", nextItem.text))
                Toast.makeText(this, "✓ Đã sao chép: ${nextItem.text}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Hàng đợi FIFO rỗng", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}
