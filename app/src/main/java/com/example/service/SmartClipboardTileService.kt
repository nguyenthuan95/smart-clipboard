package com.example.service

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.MainActivity
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
        }

        // Open MainActivity and collapse notification drawer
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        try {
            if (Build.VERSION.SDK_INT >= 34) {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(launchIntent)
            }
        } catch (_: Exception) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            } catch (_: Exception) {}
        }
    }
}
