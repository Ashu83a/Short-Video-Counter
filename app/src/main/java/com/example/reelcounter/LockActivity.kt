package com.example.reelcounter

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class LockActivity : Activity() {

    private val suggestions = listOf(
        "Take a short walk.",
        "Drink a glass of water.",
        "Call someone you like.",
        "Stretch for two minutes.",
        "Look out of the window for a minute."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(64, 64, 64, 64)
        layout.setBackgroundColor(Color.rgb(150, 30, 30))

        val face = TextView(this)
        face.text = ":'("
        face.textSize = 64f
        face.setTextColor(Color.WHITE)
        face.gravity = Gravity.CENTER
        layout.addView(face)

        val total = Prefs.getCount(this)
        val title = TextView(this)
        title.text = "$total reels today\n" + Prefs.formatTime(Prefs.getSeconds(this)) + " spent"
        title.textSize = 26f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        layout.addView(title)

        val own = Prefs.getMessage(this, 4)
        val message = TextView(this)
        message.text = "\n" + (if (own.isNotEmpty()) own else "You have gone past your limit.") + "\n\n" + suggestions.random()
        message.textSize = 20f
        message.setTextColor(Color.WHITE)
        message.gravity = Gravity.CENTER
        layout.addView(message)

        val left = Prefs.getMaxPauses(this) - Prefs.getPausesUsedToday(this)
        val unlock = Button(this)
        unlock.text = if (left > 0) "Unlock anyway (15 minutes, $left left today)" else "No unlocks left today"
        unlock.isEnabled = left > 0
        unlock.setOnClickListener {
            Prefs.startPause(this)
            finish()
        }
        layout.addView(unlock)

        val home = Button(this)
        home.text = "Go to home screen"
        home.setOnClickListener { goHome() }
        layout.addView(home)

        setContentView(layout)
    }

    // Back button also leaves to the home screen, so the lock is never a trap.
    override fun onBackPressed() {
        goHome()
    }

    private fun goHome() {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
        finish()
    }
}
