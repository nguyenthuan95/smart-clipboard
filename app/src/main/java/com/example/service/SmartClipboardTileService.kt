package com.example.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.SmartClipboardApp
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.N)
class SmartClipboardTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val queueRepo = SmartClipboardApp.instance.queueRepository
        val nextItem = runBlocking { queueRepo.getNext() }

        if (nextItem != null) {
            runBlocking { queueRepo.advanceNext() }
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            cm?.setPrimaryClip(ClipData.newPlainText("Smart Clipboard", nextItem.text))
            Toast.makeText(this, "✓ Nạp vào Clipboard: ${nextItem.text}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hàng đợi FIFO rỗng", Toast.LENGTH_SHORT).show()
        }
    }
}
