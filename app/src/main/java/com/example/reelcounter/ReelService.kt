package com.example.reelcounter

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.util.Calendar

class ReelService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var lastSignature: String = ""
    private var lastCountTime: Long = 0L

    override fun onServiceConnected() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        val root = rootInActiveWindow ?: return

        if (!isShortVideoScreen(pkg, root)) {
            hideBubble()
            return
        }

        showBubble()

        val signature = buildSignature(root)
        val now = System.currentTimeMillis()
        if (signature.isNotEmpty() && signature != lastSignature && now - lastCountTime > 1200) {
            lastSignature = signature
            lastCountTime = now
            addOne()
        }
        updateBubbleText()
    }

    override fun onInterrupt() {}

    // True only when the screen is a Reels or Shorts player.
    private fun isShortVideoScreen(pkg: String, root: AccessibilityNodeInfo): Boolean {
        return when (pkg) {
            "com.instagram.android" -> hasViewId(root, "clips_viewer") || hasViewId(root, "clips_video_container")
            "com.google.android.youtube" -> hasViewId(root, "reel_player_page_container") || hasViewId(root, "reel_recycler")
            "com.facebook.katana", "com.facebook.lite" -> findTextContains(root, "Reels")
            else -> false
        }
    }

    private fun hasViewId(root: AccessibilityNodeInfo, idPart: String): Boolean {
        val list = root.findAccessibilityNodeInfosByViewId("")
        list?.forEach { it.recycle() }
        return searchId(root, idPart, 0)
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

    // A short fingerprint of the visible texts. When it changes, a new video is on screen.
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

    // ---- Counting, resets at midnight ----
    private fun todayKey(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    private fun getCount(): Int {
        val prefs = getSharedPreferences("reels", Context.MODE_PRIVATE)
        if (prefs.getInt("day", -1) != todayKey()) {
            prefs.edit().putInt("day", todayKey()).putInt("count", 0).apply()
        }
        return prefs.getInt("count", 0)
    }

    private fun addOne() {
        val prefs = getSharedPreferences("reels", Context.MODE_PRIVATE)
        val next = getCount() + 1
        prefs.edit().putInt("count", next).apply()
    }

    // ---- The bubble ----
    private fun showBubble() {
        if (bubble != null) return
        val tv = TextView(this)
        tv.setTextColor(Color.WHITE)
        tv.textSize = 18f
        tv.gravity = Gravity.CENTER
        tv.setPadding(36, 18, 36, 18)
        val bg = GradientDrawable()
        bg.shape = GradientDrawable.RECTANGLE
        bg.cornerRadius = 80f
        bg.setColor(Color.parseColor("#CC333333"))
        tv.background = bg
        tv.text = getCount().toString()

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
        windowManager?.addView(tv, params)
        bubble = tv
    }

    private fun updateBubbleText() {
        bubble?.text = getCount().toString()
    }

    private fun hideBubble() {
        bubble?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
            }
        }
        bubble = null
    }
}
