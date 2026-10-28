package com.example.reelcounter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(48, 48, 48, 48)

        val info = TextView(this)
        info.text = "Reel Counter\n\nStep 1: allow display over other apps.\nStep 2: turn on Reel Counter in Accessibility."
        info.textSize = 18f
        layout.addView(info)

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

        setContentView(layout)
    }
}
