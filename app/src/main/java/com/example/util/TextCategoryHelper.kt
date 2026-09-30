package com.example.util

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

enum class ClipType {
    PHONE,
    LINK,
    ADDRESS,
    TEXT
}

object TextCategoryHelper {

    private val VIETNAMESE_PHONE_REGEX = Pattern.compile("^(?:\\+?84|0)(?:3|5|7|8|9)\\d{8}$")
    private val GENERIC_PHONE_REGEX = Pattern.compile("^[+]?[0-9\\s\\-\\(\\)]{8,15}$")
    private val ADDRESS_KEYWORDS = listOf("đường", "quận", "huyện", "phường", "tp", "thành phố", "tỉnh", "street", "st.", "avenue", "ave", "road", "nguyễn", "trần", "lê", "ngõ", "hẻm", "số ")

    fun detectType(text: String): ClipType {
        val trimmed = text.trim()

        // URL check
        if (Patterns.WEB_URL.matcher(trimmed).matches() || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return ClipType.LINK
        }

        // Phone check
        val digitsOnly = trimmed.replace(Regex("[\\s\\-\\(\\)]"), "")
        if (digitsOnly.length in 9..15 && (VIETNAMESE_PHONE_REGEX.matcher(digitsOnly).matches() || GENERIC_PHONE_REGEX.matcher(trimmed).matches())) {
            return ClipType.PHONE
        }

        // Address check (simple keyword heuristic)
        val lower = trimmed.lowercase(Locale.ROOT)
        if (ADDRESS_KEYWORDS.any { lower.contains(it) } && trimmed.length in 10..150) {
            return ClipType.ADDRESS
        }

        return ClipType.TEXT
    }

    fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(date)

        return when {
            diff < 60_000L -> "Vừa xong"
            diff < 24 * 3600_000L -> formattedTime
            diff < 48 * 3600_000L -> "Hôm qua $formattedTime"
            else -> {
                val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    }
}
