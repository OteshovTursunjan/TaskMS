package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object DateUtils {
    private const val ONE_DAY_MILLIS = 86_400_000L

    fun startOfLocalDayMillis(epochMillis: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = epochMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun utcPickerMillisToLocalDayStart(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val localCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
            set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    fun localDayToUtcPickerMillis(localMillis: Long): Long {
        val localCal = Calendar.getInstance().apply {
            timeInMillis = localMillis
        }
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, localCal.get(Calendar.YEAR))
            set(Calendar.MONTH, localCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return utcCal.timeInMillis
    }

    fun addDays(baseMillis: Long, days: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = startOfLocalDayMillis(baseMillis)
            add(Calendar.DAY_OF_YEAR, days)
        }
        return cal.timeInMillis
    }

    fun daysFromToday(targetMillis: Long): Int {
        val today = startOfLocalDayMillis()
        val target = startOfLocalDayMillis(targetMillis)
        return ((target - today).toDouble() / ONE_DAY_MILLIS).roundToInt()
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("d MMM", Locale("ru"))
        return sdf.format(Date(millis))
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatCountdown(totalSeconds: Int): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val minutes = safe / 60
        val seconds = safe % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun isToday(millis: Long): Boolean {
        return startOfLocalDayMillis(millis) == startOfLocalDayMillis()
    }
}
