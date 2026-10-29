package com.example.reelcounter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class StatsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildScreen())
    }

    // Refresh the numbers each time the screen comes back.
    override fun onResume() {
        super.onResume()
        setContentView(buildScreen())
    }

    private fun buildScreen(): ScrollView {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 64, 48, 48)

        layout.addView(heading("Your stats", 24f))

        addStreak(layout)
        addPeriod(layout, "Today", 1)
        addPeriod(layout, "Last 7 days", 7)
        addPeriod(layout, "Last 30 days", 30)

        val export = Button(this)
        export.text = "Export full history (CSV)"
        export.setOnClickListener { exportCsv() }
        layout.addView(export)

        val note = TextView(this)
        note.text = "\nYour full history is kept on this phone and is never erased. Per-app time and long videos are counted from the day Version 3 was installed. The long video count is a best guess."
        note.textSize = 14f
        layout.addView(note)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        return scroll
    }

    private fun addStreak(layout: LinearLayout) {
        val streak = Prefs.currentStreak(this)
        layout.addView(heading("\nStreak", 20f))
        layout.addView(line("Days in a row under your first limit: $streak"))

        val badges = mutableListOf<String>()
        if (streak >= 3) badges.add("3 days")
        if (streak >= 7) badges.add("7 days")
        if (streak >= 14) badges.add("14 days")
        if (streak >= 30) badges.add("30 days")
        if (streak >= 60) badges.add("60 days")
        if (streak >= 100) badges.add("100 days")
        if (badges.isEmpty()) {
            layout.addView(line("Badges: none yet. The first one comes at 3 days."))
        } else {
            layout.addView(line("Badges earned: " + badges.joinToString(", ")))
        }
    }

    private fun addPeriod(layout: LinearLayout, title: String, days: Int) {
        layout.addView(heading("\n$title", 20f))

        layout.addView(line("Reels in total: " + Prefs.sumCount(this, days)))
        layout.addView(line("Reel time in total: " + Prefs.formatTime(Prefs.sumSeconds(this, days))))
        if (days > 1) {
            layout.addView(line("Average reels per day: " + Prefs.sumCount(this, days) / days))
        }

        layout.addView(subheading("Reels by app"))
        for (app in Prefs.APPS) {
            val reels = Prefs.sumAppCount(this, app, days)
            val time = Prefs.formatTime(Prefs.sumAppSeconds(this, app, days))
            layout.addView(line(nice(app) + ": $reels reels, $time"))
        }

        layout.addView(subheading("Long videos (best guess)"))
        for (app in Prefs.LONG_APPS) {
            val videos = Prefs.sumLongCount(this, app, days)
            val time = Prefs.formatTime(Prefs.sumLongSeconds(this, app, days))
            layout.addView(line(nice(app) + ": $videos videos, $time"))
        }
    }

    private fun nice(app: String): String = when (app) {
        "instagram" -> "Instagram"
        "facebook" -> "Facebook"
        else -> "YouTube"
    }

    // Builds the CSV text and hands it to the Android share menu.
    private fun exportCsv() {
        val csv = buildFullCsv()
        val send = Intent(Intent.ACTION_SEND)
        send.type = "text/plain"
        send.putExtra(Intent.EXTRA_SUBJECT, "Reel Counter history")
        send.putExtra(Intent.EXTRA_TEXT, csv)
        startActivity(Intent.createChooser(send, "Save or send your history"))
    }

    // Reel history from Prefs, plus a second section for long videos.
    private fun buildFullCsv(): String {
        val sb = StringBuilder()
        sb.append(Prefs.buildCsv(this))
        sb.append("\nLong videos\n")
        sb.append("date,youtube_videos,youtube_seconds,facebook_videos,facebook_seconds\n")
        val cal = java.util.Calendar.getInstance()
        val rows = mutableListOf<String>()
        for (i in 0 until 365) {
            val d = Prefs.dayString(cal)
            val yc = Prefs.getLongCount(this, "youtube", d)
            val ys = Prefs.getLongSeconds(this, "youtube", d)
            val fc = Prefs.getLongCount(this, "facebook", d)
            val fs = Prefs.getLongSeconds(this, "facebook", d)
            if (yc + ys + fc + fs > 0) {
                rows.add("$d,$yc,$ys,$fc,$fs")
            }
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        rows.reverse()
        for (r in rows) sb.append(r).append('\n')
        return sb.toString()
    }

    private fun heading(text: String, size: Float): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        t.setTypeface(null, Typeface.BOLD)
        return t
    }

    private fun subheading(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 16f
        t.setTypeface(null, Typeface.ITALIC)
        return t
    }

    private fun line(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 16f
        return t
    }
}
