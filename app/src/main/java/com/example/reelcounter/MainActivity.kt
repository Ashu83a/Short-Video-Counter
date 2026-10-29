package com.example.reelcounter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.text.InputType

class MainActivity : Activity() {

    private lateinit var limit1: EditText
    private lateinit var limit2: EditText
    private lateinit var limit3: EditText
    private lateinit var limitVibe1: EditText
    private lateinit var limitVibe2: EditText
    private lateinit var limitVibe3: EditText
    private lateinit var intervalBox: EditText
    private lateinit var intervalVibe: EditText
    private lateinit var transparencyLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(48, 64, 48, 48)

        val title = TextView(this)
        title.text = "Reel Counter"
        title.textSize = 24f
        layout.addView(title)

        // ---- Permissions (same as Version 1) ----
        val overlayButton = Button(this)
        overlayButton.text = "Step 1: Allow bubble on screen"
        overlayButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
        }
        layout.addView(overlayButton)

        val accessButton = Button(this)
        accessButton.text = "Step 2: Turn on Accessibility"
        accessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        layout.addView(accessButton)

        // ---- Limits ----
        val limitsTitle = TextView(this)
        limitsTitle.text = "\nYour three limits (yellow, orange, red)"
        limitsTitle.textSize = 18f
        layout.addView(limitsTitle)

        limit1 = makeNumberBox(Prefs.getLimit(this, 1))
        limit2 = makeNumberBox(Prefs.getLimit(this, 2))
        limit3 = makeNumberBox(Prefs.getLimit(this, 3))
        layout.addView(label("Limit 1 (yellow): reel number"))
        layout.addView(limit1)
        layout.addView(label("Limit 2 (orange): reel number"))
        layout.addView(limit2)
        layout.addView(label("Limit 3 (red): reel number"))
        layout.addView(limit3)

        // ---- Vibration lengths ----
        val vibeTitle = TextView(this)
        vibeTitle.text = "\nVibration lengths in milliseconds (0 = off, 1000 = 1 second)"
        vibeTitle.textSize = 18f
        layout.addView(vibeTitle)

        limitVibe1 = makeNumberBox(TenthVibration.getLimitVibe(this, 1))
        limitVibe2 = makeNumberBox(TenthVibration.getLimitVibe(this, 2))
        limitVibe3 = makeNumberBox(TenthVibration.getLimitVibe(this, 3))
        layout.addView(label("Buzz at limit 1"))
        layout.addView(limitVibe1)
        layout.addView(label("Buzz at limit 2"))
        layout.addView(limitVibe2)
        layout.addView(label("Buzz at limit 3"))
        layout.addView(limitVibe3)

        // ---- Interval ----
        val intervalTitle = TextView(this)
        intervalTitle.text = "\nRegular buzz and bubble growth"
        intervalTitle.textSize = 18f
        layout.addView(intervalTitle)

        intervalBox = makeNumberBox(TenthVibration.getInterval(this))
        intervalVibe = makeNumberBox(TenthVibration.getIntervalVibe(this))
        layout.addView(label("Every how many reels (like 10, 20 or 25)"))
        layout.addView(intervalBox)
        layout.addView(label("Buzz length on those reels in milliseconds (0 = off)"))
        layout.addView(intervalVibe)

        val saveButton = Button(this)
        saveButton.text = "Save limits and vibration"
        saveButton.setOnClickListener { saveLimits() }
        layout.addView(saveButton)

        // ---- Transparency ----
        transparencyLabel = TextView(this)
        transparencyLabel.textSize = 18f
        layout.addView(transparencyLabel)

        val slider = SeekBar(this)
        slider.max = 80
        slider.progress = Prefs.getTransparency(this)
        updateTransparencyLabel(slider.progress)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateTransparencyLabel(progress)
                Prefs.setTransparency(this@MainActivity, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        layout.addView(slider)

        // ---- Stats ----
        val statsButton = Button(this)
        statsButton.text = "Open Stats"
        statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        layout.addView(statsButton)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun label(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 14f
        return t
    }

    private fun makeNumberBox(value: Int): EditText {
        val e = EditText(this)
        e.inputType = InputType.TYPE_CLASS_NUMBER
        e.setText(value.toString())
        return e
    }

    private fun updateTransparencyLabel(progress: Int) {
        transparencyLabel.text = "\nBubble transparency: $progress%"
    }

    private fun saveLimits() {
        val a = limit1.text.toString().toIntOrNull()
        val b = limit2.text.toString().toIntOrNull()
        val c = limit3.text.toString().toIntOrNull()
        if (a == null || b == null || c == null || a < 1) {
            Toast.makeText(this, "Please enter whole numbers", Toast.LENGTH_SHORT).show()
            return
        }
        if (!(a < b && b < c)) {
            Toast.makeText(this, "Limits must go up: limit 1 less than limit 2 less than limit 3", Toast.LENGTH_LONG).show()
            return
        }
        val v1 = limitVibe1.text.toString().toIntOrNull()
        val v2 = limitVibe2.text.toString().toIntOrNull()
        val v3 = limitVibe3.text.toString().toIntOrNull()
        if (v1 == null || v2 == null || v3 == null || v1 !in 0..10000 || v2 !in 0..10000 || v3 !in 0..10000) {
            Toast.makeText(this, "Limit buzz lengths must be numbers from 0 to 10000", Toast.LENGTH_LONG).show()
            return
        }
        val interval = intervalBox.text.toString().toIntOrNull()
        if (interval == null || interval !in 1..1000) {
            Toast.makeText(this, "Interval must be a number from 1 to 1000", Toast.LENGTH_LONG).show()
            return
        }
        val iv = intervalVibe.text.toString().toIntOrNull()
        if (iv == null || iv !in 0..5000) {
            Toast.makeText(this, "Interval buzz length must be a number from 0 to 5000", Toast.LENGTH_LONG).show()
            return
        }
        Prefs.setLimits(this, a, b, c)
        TenthVibration.setLimitVibes(this, v1, v2, v3)
        TenthVibration.setInterval(this, interval)
        TenthVibration.setIntervalVibe(this, iv)
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
    }
}
