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
        layout.addView(label("Limit 1 (yellow, vibrates 1 second)"))
        layout.addView(limit1)
        layout.addView(label("Limit 2 (orange, vibrates 3 seconds)"))
        layout.addView(limit2)
        layout.addView(label("Limit 3 (red, vibrates 5 seconds)"))
        layout.addView(limit3)

        val saveButton = Button(this)
        saveButton.text = "Save limits"
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
        Prefs.setLimits(this, a, b, c)
        Toast.makeText(this, "Limits saved", Toast.LENGTH_SHORT).show()
    }
}
