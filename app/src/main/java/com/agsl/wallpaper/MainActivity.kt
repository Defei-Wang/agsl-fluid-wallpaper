package com.agsl.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView

class MainActivity : Activity() {

    inner class DynamicEcologyView(context: Context) : View(context) {
        private val ecology = JellyfishEcology()

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0) {
                ecology.ensureInit(w.toFloat(), h.toFloat())
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (width <= 0 || height <= 0) return
            ecology.render(canvas, width.toFloat(), height.toFloat())
            postInvalidateOnAnimation()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            ecology.onTouchEvent(event, width.toFloat(), height.toFloat())
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val preview = DynamicEcologyView(this)
        root.addView(preview, ViewGroup.LayoutParams(-1, -1))

        val hint = TextView(this).apply {
            text = "Ethereal Abyssal Fauna (7 Species | Fluid Shock Reaction)"
            setTextColor(Color.parseColor("#475569"))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        root.addView(hint, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))

        val btn = Button(this).apply {
            text = "设为动态壁纸 (Apply Wallpaper)"
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 14f
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#060913"))
                setStroke(2, Color.parseColor("#1E293B"))
                cornerRadius = 28f
            }
            setOnClickListener {
                try {
                    startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@MainActivity, AgslWallpaperService::class.java))
                    })
                } catch (_: Throwable) {
                    startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
                }
            }
        }
        root.addView(btn, FrameLayout.LayoutParams(-1, 130, Gravity.BOTTOM).apply { setMargins(56, 0, 56, 64) })
        setContentView(root)
    }
}
