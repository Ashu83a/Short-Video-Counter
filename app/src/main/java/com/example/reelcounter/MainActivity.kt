package com.example.reelcounter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var unlocked = false

    private lateinit var limit1: EditText
    private lateinit var limit2: EditText
    private lateinit var limit3: EditText
    private lateinit var limit4: EditText
    private lateinit var limitVibe1: EditText
    private lateinit var limitVibe2: EditText
    private lateinit var limitVibe3: EditText
    private lateinit var intervalBox: EditText
    private lateinit var intervalVibe: EditText
    private lateinit var pauses: EditText
    private lateinit var msg1: EditText
    private lateinit var msg2: EditText
    private lateinit var msg3: EditText
    private lateinit var msg4: EditText
    private lateinit var bedOn: CheckBox
    private lateinit var bedTime1: EditText
    private lateinit var bedTime2: EditText
    private lateinit var bedMsg1: EditText
    private lateinit var bedMsg2: EditText
    private lateinit var relaxOn: CheckBox
    private lateinit var relaxDays: EditText
    private lateinit var relaxStart: EditText
    private lateinit var relaxEnd: EditText
    private lateinit var transparencyLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showCorrectScreen()
    }

    // If a password is set, ask for it before showing the settings.
    private fun showCorrectScreen() {
        if (Prefs.hasPassword(this) && !unlocked) {
            buildPasswordScreen()
        } else {
            buildSettingsScreen()
        }
    }

    private fun buildPasswordScreen() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(48, 48, 48, 48)

        val info = TextView(this)
        info.text = "Settings are locked.\nYour accountability partner can type the password."
        info.textSize = 18f
        layout.addView(info)

        val box = EditText(this)
        box.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        box.hint = "Password"
        layout.addView(box)

        val enter = Button(this)
        enter.text = "Unlock settings"
        enter.setOnClickListener {
            if (Prefs.checkPassword(this, box.text.toString())) {
                unlocked = true
                buildSettingsScreen()
            } else {
                Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(enter)

        // Stats can always be viewed without the password.
        val stats = Button(this)
        stats.text = "Open Stats"
        stats.setOnClickListener { startActivity(Intent(this, StatsActivity::class.java)) }
        layout.addView(stats)

        setContentView(layout)
    }

    private fun buildSettingsScreen() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER_HORIZONTAL
        layout.setPadding(48, 64, 48, 48)

        layout.addView(heading("Reel Counter", 24f))

        // ---- Permissions ----
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

        val statsButton = Button(this)
        statsButton.text = "Open Stats"
        statsButton.setOnClickListener { startActivity(Intent(this, StatsActivity::class.java)) }
        layout.addView(statsButton)

        // ---- Limits ----
        layout.addView(heading("\nYour limits", 18f))
        limit1 = numberBox(Prefs.getLimit(this, 1))
        limit2 = numberBox(Prefs.getLimit(this, 2))
        limit3 = numberBox(Prefs.getLimit(this, 3))
        limit4 = numberBox(Prefs.getLimit(this, 4))
        layout.addView(label("Limit 1: yellow"))
        layout.addView(limit1)
        layout.addView(label("Limit 2: orange"))
        layout.addView(limit2)
        layout.addView(label("Limit 3: red"))
        layout.addView(limit3)
        layout.addView(label("Limit 4: lock screen"))
        layout.addView(limit4)
        layout.addView(heading("\nVibration lengths in milliseconds (0 = off, 1000 = 1 second)", 18f))
        limitVibe1 = numberBox(TenthVibration.getLimitVibe(this, 1))
        limitVibe2 = numberBox(TenthVibration.getLimitVibe(this, 2))
        limitVibe3 = numberBox(TenthVibration.getLimitVibe(this, 3))
        layout.addView(label("Buzz at limit 1"))
        layout.addView(limitVibe1)
        layout.addView(label("Buzz at limit 2"))
        layout.addView(limitVibe2)
        layout.addView(label("Buzz at limit 3"))
        layout.addView(limitVibe3)

        layout.addView(heading("\nRegular buzz and bubble growth", 18f))
        intervalBox = numberBox(TenthVibration.getInterval(this))
        intervalVibe = numberBox(TenthVibration.getIntervalVibe(this))
        layout.addView(label("Every how many reels (like 10, 20 or 25)"))
        layout.addView(intervalBox)
        layout.addView(label("Buzz length on those reels in milliseconds (0 = off)"))
        layout.addView(intervalVibe)

        pauses = numberBox(Prefs.getMaxPauses(this))
        layout.addView(label("Unlock-anyway pauses allowed per day (15 minutes each)"))
        layout.addView(pauses)

        // ---- Messages ----
        layout.addView(heading("\nYour messages (leave blank for none)", 18f))
        msg1 = textBox(Prefs.getMessage(this, 1))
        msg2 = textBox(Prefs.getMessage(this, 2))
        msg3 = textBox(Prefs.getMessage(this, 3))
        msg4 = textBox(Prefs.getMessage(this, 4))
        layout.addView(label("At limit 1"))
        layout.addView(msg1)
        layout.addView(label("At limit 2"))
        layout.addView(msg2)
        layout.addView(label("At limit 3"))
        layout.addView(msg3)
        layout.addView(label("On the lock screen"))
        layout.addView(msg4)

        // ---- Bedtime ----
        layout.addView(heading("\nBedtime mode", 18f))
        bedOn = CheckBox(this)
        bedOn.text = "Bedtime reminders on"
        bedOn.isChecked = Prefs.getBedtimeEnabled(this)
        layout.addView(bedOn)
        bedTime1 = textBox(formatClock(Prefs.getBedtime1(this)))
        bedTime2 = textBox(formatClock(Prefs.getBedtime2(this)))
        layout.addView(label("First reminder time (24 hour, like 22:30)"))
        layout.addView(bedTime1)
        layout.addView(label("Second reminder time (like 23:00)"))
        layout.addView(bedTime2)
        bedMsg1 = textBox(Prefs.getBedMessage(this, 1))
        bedMsg2 = textBox(Prefs.getBedMessage(this, 2))
        layout.addView(label("First reminder message"))
        layout.addView(bedMsg1)
        layout.addView(label("Second reminder message"))
        layout.addView(bedMsg2)

        // ---- Relaxed window ----
        layout.addView(heading("\nRelaxed time (lock is off)", 18f))
        relaxOn = CheckBox(this)
        relaxOn.text = "Turn the lock off during this time"
        relaxOn.isChecked = Prefs.getRelaxEnabled(this)
        layout.addView(relaxOn)
        relaxDays = textBox(Prefs.getRelaxDays(this))
        relaxStart = numberBox(Prefs.getRelaxStartHour(this))
        relaxEnd = numberBox(Prefs.getRelaxEndHour(this))
        layout.addView(label("Days (1 Sunday, 2 Monday ... 7 Saturday, like 1,7)"))
        layout.addView(relaxDays)
        layout.addView(label("From hour (0 to 23, like 18)"))
        layout.addView(relaxStart)
        layout.addView(label("Until hour (like 22)"))
        layout.addView(relaxEnd)

        val save = Button(this)
        save.text = "Save all settings"
        save.setOnClickListener { saveAll() }
        layout.addView(save)

        // ---- Transparency (saves by itself) ----
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

        // ---- Accountability password ----
        layout.addView(heading("\nAccountability partner", 18f))
        val pwButton = Button(this)
        pwButton.text = if (Prefs.hasPassword(this)) "Change or remove password" else "Set a password"
        pwButton.setOnClickListener { askNewPassword() }
        layout.addView(pwButton)

        val scroll = ScrollView(this)
        scroll.addView(layout)
        setContentView(scroll)
    }

    private fun heading(text: String, size: Float): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size
        return t
    }

    private fun label(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = 14f
        return t
    }

    private fun numberBox(value: Int): EditText {
        val e = EditText(this)
        e.inputType = InputType.TYPE_CLASS_NUMBER
        e.setText(value.toString())
        return e
    }

    private fun textBox(value: String): EditText {
        val e = EditText(this)
        e.inputType = InputType.TYPE_CLASS_TEXT
        e.setText(value)
        return e
    }

    private fun updateTransparencyLabel(progress: Int) {
        transparencyLabel.text = "\nBubble transparency: $progress%"
    }

    private fun formatClock(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "%02d:%02d".format(h, m)
    }

    // Turns text like 22:30 into minutes after midnight, or null if it is not valid.
    private fun parseClock(text: String): Int? {
        val parts = text.trim().split(":")
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    private fun saveAll() {
        val a = limit1.text.toString().toIntOrNull()
        val b = limit2.text.toString().toIntOrNull()
        val c = limit3.text.toString().toIntOrNull()
        val d = limit4.text.toString().toIntOrNull()
        val p = pauses.text.toString().toIntOrNull()
        if (a == null || b == null || c == null || d == null || p == null || a < 1) {
            toast("Please enter whole numbers for the limits and pauses")
            return
        }
        if (!(a < b && b < c && c < d)) {
            toast("Limits must go up: 1 less than 2 less than 3 less than 4")
            return
        }
        val t1 = parseClock(bedTime1.text.toString())
        val t2 = parseClock(bedTime2.text.toString())
        if (t1 == null || t2 == null || t1 >= t2) {
            toast("Bedtime times must look like 22:30, and the second must be later")
            return
        }
        val rs = relaxStart.text.toString().toIntOrNull()
        val re = relaxEnd.text.toString().toIntOrNull()
        if (rs == null || re == null || rs !in 0..23 || re !in 0..24 || rs >= re) {
            toast("Relaxed hours must be 0 to 23, and the end later than the start")
            return
        }
        val days = relaxDays.text.toString().replace(" ", "")
        val dayList = days.split(",")
        if (dayList.any { it.toIntOrNull() == null || it.toInt() !in 1..7 }) {
            toast("Days must be numbers 1 to 7 separated by commas, like 1,7")
            return
        }

        val v1 = limitVibe1.text.toString().toIntOrNull()
        val v2 = limitVibe2.text.toString().toIntOrNull()
        val v3 = limitVibe3.text.toString().toIntOrNull()
        if (v1 == null || v2 == null || v3 == null || v1 !in 0..10000 || v2 !in 0..10000 || v3 !in 0..10000) {
            toast("Limit buzz lengths must be numbers from 0 to 10000")
            return
        }
        val interval = intervalBox.text.toString().toIntOrNull()
        if (interval == null || interval !in 1..1000) {
            toast("Interval must be a number from 1 to 1000")
            return
        }
        val iv = intervalVibe.text.toString().toIntOrNull()
        if (iv == null || iv !in 0..5000) {
            toast("Interval buzz length must be a number from 0 to 5000")
            return
        }

        Prefs.setLimits(this, a, b, c)
        Prefs.setLockLimit(this, d)
        TenthVibration.setLimitVibes(this, v1, v2, v3)
        TenthVibration.setInterval(this, interval)
        TenthVibration.setIntervalVibe(this, iv)
        Prefs.setMaxPauses(this, p)
        Prefs.setMessage(this, 1, msg1.text.toString())
        Prefs.setMessage(this, 2, msg2.text.toString())
        Prefs.setMessage(this, 3, msg3.text.toString())
        Prefs.setMessage(this, 4, msg4.text.toString())
        Prefs.setBedtimeEnabled(this, bedOn.isChecked)
        Prefs.setBedtimes(this, t1, t2)
        Prefs.setBedMessage(this, 1, bedMsg1.text.toString())
        Prefs.setBedMessage(this, 2, bedMsg2.text.toString())
        Prefs.setRelaxEnabled(this, relaxOn.isChecked)
        Prefs.setRelaxWindow(this, days, rs, re)
        toast("Settings saved")
    }

    private fun askNewPassword() {
        val box = EditText(this)
        box.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        box.hint = "New password (leave empty to remove)"
        AlertDialog.Builder(this)
            .setTitle("Accountability password")
            .setMessage("Your partner should type this in, so you cannot change settings on impulse. Write it down for them.")
            .setView(box)
            .setPositiveButton("Save") { _, _ ->
                val text = box.text.toString()
                if (text.isEmpty()) {
                    Prefs.clearPassword(this)
                    toast("Password removed")
                } else {
                    Prefs.setPassword(this, text)
                    toast("Password set")
                }
                buildSettingsScreen()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}
