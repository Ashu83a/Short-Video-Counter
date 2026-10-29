package com.example.reelcounter

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Prefs {
    private const val FILE = "reels_v2"
    val APPS = listOf("instagram", "facebook", "youtube")

    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun dayString(cal: Calendar = Calendar.getInstance()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

    // ---- Settings ----
    fun getLimit(c: Context, n: Int): Int = when (n) {
        1 -> p(c).getInt("limit1", 100)
        2 -> p(c).getInt("limit2", 150)
        else -> p(c).getInt("limit3", 200)
    }

    fun setLimits(c: Context, l1: Int, l2: Int, l3: Int) {
        p(c).edit().putInt("limit1", l1).putInt("limit2", l2).putInt("limit3", l3).apply()
    }

    // Transparency from 0 (solid) to 80 (very faint)
    fun getTransparency(c: Context): Int = p(c).getInt("transparency", 20)
    fun setTransparency(c: Context, v: Int) {
        p(c).edit().putInt("transparency", v).apply()
    }

    // ---- Daily numbers (kept forever, one entry per day) ----
    fun getCount(c: Context, day: String = dayString()): Int = p(c).getInt("count_$day", 0)
    fun getSeconds(c: Context, day: String = dayString()): Int = p(c).getInt("secs_$day", 0)
    fun getAppCount(c: Context, app: String, day: String = dayString()): Int =
        p(c).getInt("count_${app}_$day", 0)

    fun addReel(c: Context, app: String): Int {
        val day = dayString()
        val total = getCount(c, day) + 1
        p(c).edit()
            .putInt("count_$day", total)
            .putInt("count_${app}_$day", getAppCount(c, app, day) + 1)
            .apply()
        return total
    }

    fun addSeconds(c: Context, secs: Int) {
        if (secs <= 0) return
        val day = dayString()
        p(c).edit().putInt("secs_$day", getSeconds(c, day) + secs).apply()
    }

    // ---- Totals for the stats screen ----
    // Adds up the last [days] days, today included.
    fun sumCount(c: Context, days: Int): Int {
        var sum = 0
        val cal = Calendar.getInstance()
        for (i in 0 until days) {
            sum += getCount(c, dayString(cal))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sum
    }

    fun sumSeconds(c: Context, days: Int): Int {
        var sum = 0
        val cal = Calendar.getInstance()
        for (i in 0 until days) {
            sum += getSeconds(c, dayString(cal))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sum
    }

    fun sumAppCount(c: Context, app: String, days: Int): Int {
        var sum = 0
        val cal = Calendar.getInstance()
        for (i in 0 until days) {
            sum += getAppCount(c, app, dayString(cal))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return sum
    }

    // 12 minutes 5 seconds -> "12m", 3725 seconds -> "1h 2m"
    fun formatTime(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
