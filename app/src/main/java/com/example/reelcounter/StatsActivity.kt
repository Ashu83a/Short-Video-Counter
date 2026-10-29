package com.example.reelcounter

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
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

        layout.addView(heading("Your reel stats", 24f))

        addPeriod(layout, "Today", 1)
        addPeriod(layout, "Last 7 days", 7)
        addPeriod(layout, "Last 30 days", 30)

        val note = TextView(this)
        note.text = "\nYour full history is kept on this phone and is never erased."
        note.textSize = 14f
        layout.addView(note)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        return scroll
    }

    private fun addPeriod(layout: LinearLayout, title: String, days: Int) {
        layout.addView(heading("\n$title", 20f))

        val reels = Prefs.sumCount(this, days)
        val time = Prefs.formatTime(Prefs.sumSeconds(this, days))
        layout.addView(line("Reels: $reels"))
        layout.addView(line("Time spent: $time"))
        layout.addView(line("Instagram: " + Prefs.sumAppCount(this, "instagram", days)))
        layout.addView(line("Facebook: " + Prefs.sumAppCount(this, "facebook", days)))
        layout.addView(line("YouTube: " + Prefs.sumAppCount(this, "youtube", days)))

        if (days > 1) {
            val average = reels / days
            layout.addView(line("Average per day: $average"))
        }
    }

    private fun heading(text: String, size: Float): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        t.setTypeface(null, Typeface.BOLD)
        return t
    }

    private fun line(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 16f
        return t
    }
}
