package com.example.ui.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {
    private val rupeeFormatter = DecimalFormat("##,##,##0.00")
    private val compactFormatter = DecimalFormat("##,##,##0")

    fun formatRupee(amount: Double): String {
        return "₹${rupeeFormatter.format(amount)}"
    }

    fun formatCompactRupee(amount: Double): String {
        return "₹${compactFormatter.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}

enum class LanguageMode {
    HINGLISH, // e.g. आज की Sales / Today's Sales
    HINDI,    // e.g. आज की कुल बिक्री
    ENGLISH   // e.g. Today's Total Sales
}
