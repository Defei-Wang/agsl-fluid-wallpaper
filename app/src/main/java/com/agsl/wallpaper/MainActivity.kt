package com.agsl.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
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

    inner class OilFilmView(context: Context) : View(context) {
        private var renderShader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val startTime = System.nanoTime()

        private var p1Id = -1
        private var curP1X = 0f; private var curP1Y = 0f
        private var targetP1Down = 0f; private var curP1Down = 0f
        private var p1Speed = 0f; private var lastTouchX1 = 0f; private var lastTouchY1 = 0f

        private var p2Id = -1
        private var curP2X = 0f; private var curP2Y = 0f
        private var targetP2Down = 0f; private var curP2Down = 0f
        private var p2Speed = 0f; private var lastTouchX2 = 0f; private var lastTouchY2 = 0f

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
            val actionIndex = e.actionIndex
            val pointerId = e.getPointerId(actionIndex)

            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    val x = e.getX(actionIndex)
                    val y = e.getY(actionIndex)
                    if (p1Id == -1) {
                        p1Id = pointerId
                        curP1X = x; curP1Y = y
                        lastTouchX1 = x; lastTouchY1 = y
                        targetP1Down = 1f
                        p1Speed = 0.05f
                    } else if (p2Id == -1) {
                        p2Id = pointerId
                        curP2X = x; curP2Y = y
                        lastTouchX2 = x; lastTouchY2 = y
                        targetP2Down = 1f
                        p2Speed = 0.05f
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until e.pointerCount) {
                        val pid = e.getPointerId(i)
                        val x = e.getX(i); val y = e.getY(i)
                        if (pid == p1Id) {
                            val dist = hypot((x - lastTouchX1).toDouble(), (y - lastTouchY1).toDouble()).toFloat()
                            val instantSpeed = (dist / 35f).coerceIn(0f, 1f) // 速度直接映射
                            p1Speed = p1Speed * 0.6f + instantSpeed * 0.4f
                            curP1X = x; curP1Y = y
                            lastTouchX1 = x; lastTouchY1 = y
                        } else if (pid == p2Id) {
                            val dist = hypot((x - lastTouchX2).toDouble(), (y - lastTouchY2).toDouble()).toFloat()
                            val instantSpeed = (dist / 35f).coerceIn(0f, 1f)
                            p2Speed = p2Speed * 0.6f + instantSpeed * 0.4f
                            curP2X = x; curP2Y = y
                            lastTouchX2 = x; lastTouchY2 = y
                        }
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    if (pointerId == p1Id) { p1Id = -1; targetP1Down = 0f }
                    else if (pointerId == p2Id) { p2Id = -1; targetP2Down = 0f }
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
        val preview = OilFilmView(this)
        root.addView(preview, ViewGroup.LayoutParams(-1, -1))

        val hintText = TextView(this).apply {
            text = "Abyssal Oil Film (Speed-Dependent Puncture)"
            setTextColor(Color.parseColor("#475569"))
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 0)
        }
        root.addView(hintText, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))

        val btnApply = Button(this).apply {
            text = "设为动态壁纸 (Apply Wallpaper)"
            setTextColor(Color.parseColor("#CBD5E1"))
            textSize = 14f
            val shape = GradientDrawable().apply {
                setColor(Color.parseColor("#060913"))
                setStroke(2, Color.parseColor("#1E293B"))
                cornerRadius = 28f
            }
            background = shape
            setOnClickListener {
                try {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(this@MainActivity, AgslWallpaperService::class.java)
                        )
                    }
                    startActivity(intent)
                } catch (e: Throwable) {
                    startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
                }
            }
        }
        val btnParams = FrameLayout.LayoutParams(-1, 130, Gravity.BOTTOM).apply {
            setMargins(56, 0, 56, 64)
        }
        root.addView(btnApply, btnParams)

        setContentView(root)
    }
}
