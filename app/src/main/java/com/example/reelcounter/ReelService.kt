package com.example.reelcounter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class ReelService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var background: GradientDrawable? = null
    private val handler = Handler(Looper.getMainLooper())

    private var lastSignature: String = ""
    private var lastCountTime: Long = 0L
    private var lastEventTime: Long = 0L
    private var lastPackage: String = ""

    override fun onServiceConnected() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        if (!isShortVideoScreen(pkg, root)) {
            lastEventTime = 0L
            hideBubble()
            return
        }

        val now = System.currentTimeMillis()

        // Stopwatch: add the time since the previous event on a reel screen.
        // A gap longer than 15 seconds adds only 15 seconds, so a long pause
        // with no screen changes cannot inflate the total too much.
        if (lastEventTime != 0L && pkg == lastPackage) {
            val gapSeconds = ((now - lastEventTime) / 1000).toInt()
            Prefs.addSeconds(this, gapSeconds.coerceAtMost(15))
        }
        lastEventTime = now
        lastPackage = pkg

        showBubble()

        val signature = buildSignature(root)
        if (signature.isNotEmpty() && signature != lastSignature && now - lastCountTime > 1200) {
            lastSignature = signature
            lastCountTime = now
            val total = Prefs.addReel(this, appKey(pkg))
            onNewReel(total)
        }
        refreshBubble()
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        hideBubble()
        super.onDestroy()
    }

    private fun appKey(pkg: String): String = when (pkg) {
        "com.instagram.android" -> "instagram"
        "com.google.android.youtube" -> "youtube"
        else -> "facebook"
    }

    // ---- Detecting a Reels or Shorts screen (same rule as Version 1) ----
    private fun isShortVideoScreen(pkg: String, root: AccessibilityNodeInfo): Boolean {
        return when (pkg) {
            "com.instagram.android" -> searchId(root, "clips_viewer", 0) || searchId(root, "clips_video_container", 0)
            "com.google.android.youtube" -> searchId(root, "reel_player_page_container", 0) || searchId(root, "reel_recycler", 0)
            "com.facebook.katana", "com.facebook.lite" -> findTextContains(root, "Reels")
            else -> false
        }
    }

    private fun searchId(node: AccessibilityNodeInfo?, idPart: String, depth: Int): Boolean {
        if (node == null || depth > 25) return false
        val id = node.viewIdResourceName
        if (id != null && id.contains(idPart)) return true
        for (i in 0 until node.childCount) {
            if (searchId(node.getChild(i), idPart, depth + 1)) return true
        }
        return false
    }

    private fun findTextContains(root: AccessibilityNodeInfo, word: String): Boolean {
        val found = root.findAccessibilityNodeInfosByText(word)
        return found != null && found.isNotEmpty()
    }

    private fun buildSignature(root: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        collectText(root, sb, 0)
        return sb.toString().take(300)
    }

    private fun collectText(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 20 || sb.length > 300) return
        val d = node.contentDescription?.toString()
        if (!d.isNullOrEmpty() && d.length > 12) sb.append(d.take(40)).append('|')
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), sb, depth + 1)
        }
    }

    // ---- What happens on each new reel ----
    private fun onNewReel(total: Int) {
        val l1 = Prefs.getLimit(this, 1)
        val l2 = Prefs.getLimit(this, 2)
        val l3 = Prefs.getLimit(this, 3)

        // Vibrate exactly once when a limit is reached.
        when (total) {
            l1 -> vibrate(1000)
            l2 -> vibrate(3000)
            l3 -> vibrate(5000)
        }

        // Pulse: double size on every 50th, grow by half on every 10th.
        if (total % 50 == 0) {
            pulse(2.0f)
        } else if (total % 10 == 0) {
            pulse(1.5f)
        }
    }

    private fun vibrate(millis: Long) {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(millis)
            }
        } catch (e: Exception) {
        }
    }

    private fun pulse(scale: Float) {
        val b = bubble ?: return
        b.pivotX = b.width.toFloat()
        b.pivotY = 0f
        b.scaleX = scale
        b.scaleY = scale
        handler.postDelayed({
            bubble?.scaleX = 1f
            bubble?.scaleY = 1f
        }, 2000)
    }

    // ---- The bubble ----
    private fun colorFor(total: Int): Int {
        return when {
            total >= Prefs.getLimit(this, 3) -> Color.rgb(211, 47, 47)
            total >= Prefs.getLimit(this, 2) -> Color.rgb(245, 124, 0)
            total >= Prefs.getLimit(this, 1) -> Color.rgb(249, 168, 37)
            else -> Color.rgb(51, 51, 51)
        }
    }

    private fun showBubble() {
        if (bubble != null) return
        val tv = TextView(this)
        tv.setTextColor(Color.WHITE)
        tv.textSize = 16f
        tv.gravity = Gravity.CENTER
        tv.setPadding(36, 16, 36, 16)
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.RECTANGLE
        bg.cornerRadius = 60f
        tv.background = bg
        background = bg

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 24
        params.y = 220
        try {
            windowManager?.addView(tv, params)
            bubble = tv
            refreshBubble()
        } catch (e: Exception) {
            bubble = null
        }
    }

    private fun refreshBubble() {
        val b = bubble ?: return
        val total = Prefs.getCount(this)
        b.text = "$total reels\n" + Prefs.formatTime(Prefs.getSeconds(this))
        // Transparency setting: 0 is solid, 80 is very faint.
        val alpha = ((100 - Prefs.getTransparency(this)) * 255) / 100
        background?.setColor(colorFor(total))
        background?.alpha = alpha.coerceIn(40, 255)
    }

    private fun hideBubble() {
        handler.removeCallbacksAndMessages(null)
        bubble?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
            }
        }
        bubble = null
        background = null
    }
}
