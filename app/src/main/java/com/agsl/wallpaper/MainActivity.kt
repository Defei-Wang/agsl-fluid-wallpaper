package com.agsl.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import kotlin.math.hypot

class MainActivity : Activity() {

    inner class CleanOilFilmView(context: android.content.Context) : View(context) {
        private var renderShader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val startTime = System.nanoTime()

        private var p1Id = -1
        private var curP1X = 0f; private var curP1Y = 0f
        private var targetP1Down = 0f; private var curP1Down = 0f
        private var p1Speed = 0f; private var lastX1 = 0f; private var lastY1 = 0f

        private var p2Id = -1
        private var curP2X = 0f; private var curP2Y = 0f
        private var targetP2Down = 0f; private var curP2Down = 0f
        private var p2Speed = 0f; private var lastX2 = 0f; private var lastY2 = 0f

        init {
            try {
                renderShader = RuntimeShader(Shaders.RENDER)
                paint.shader = renderShader
            } catch (_: Throwable) {}
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            if (w > 0 && h > 0) {
                renderShader?.setFloatUniform("uResolution", w.toFloat(), h.toFloat())
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val s = renderShader
            if (s != null && width > 0 && height > 0) {
                val t = (System.nanoTime() - startTime) / 1_000_000_000.0f
                curP1Down += (targetP1Down - curP1Down) * 0.2f
                curP2Down += (targetP2Down - curP2Down) * 0.2f
                p1Speed *= 0.85f
                p2Speed *= 0.85f

                s.setFloatUniform("uTime", t)
                s.setFloatUniform("uPointer1", curP1X, curP1Y, curP1Down, p1Speed)
                s.setFloatUniform("uPointer2", curP2X, curP2Y, curP2Down, p2Speed)
                canvas.drawPaint(paint)
            }
            postInvalidateOnAnimation()
        }

        override fun onTouchEvent(e: MotionEvent): Boolean {
            val idx = e.actionIndex
            val pid = e.getPointerId(idx)
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val x = e.getX(idx); val y = e.getY(idx)
                    if (p1Id == -1) {
                        p1Id = pid; curP1X = x; curP1Y = y; lastX1 = x; lastY1 = y
                        targetP1Down = 1f; p1Speed = 0.05f
                    } else if (p2Id == -1) {
                        p2Id = pid; curP2X = x; curP2Y = y; lastX2 = x; lastY2 = y
                        targetP2Down = 1f; p2Speed = 0.05f
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until e.pointerCount) {
                        val id = e.getPointerId(i)
                        val x = e.getX(i); val y = e.getY(i)
                        if (id == p1Id) {
                            val dist = hypot((x - lastX1).toDouble(), (y - lastX1).toDouble()).toFloat()
                            p1Speed = (p1Speed * 0.6f + (dist / 35f).coerceIn(0f, 1f) * 0.4f)
                            curP1X = x; curP1Y = y; lastX1 = x; lastY1 = y
                        } else if (id == p2Id) {
                            val dist = hypot((x - lastX2).toDouble(), (y - lastX2).toDouble()).toFloat()
                            p2Speed = (p2Speed * 0.6f + (dist / 35f).coerceIn(0f, 1f) * 0.4f)
                            curP2X = x; curP2Y = y; lastX2 = x; lastY2 = y
                        }
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    if (pid == p1Id) { p1Id = -1; targetP1Down = 0f }
                    else if (pid == p2Id) { p2Id = -1; targetP2Down = 0f }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    p1Id = -1; targetP1Down = 0f
                    p2Id = -1; targetP2Down = 0f
                }
            }
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = FrameLayout(this)
        val preview = CleanOilFilmView(this)
        root.addView(preview, ViewGroup.LayoutParams(-1, -1))

        val hint = TextView(this).apply {
            text = "Pure Abyssal Oil Film (Zero Glare)"
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
