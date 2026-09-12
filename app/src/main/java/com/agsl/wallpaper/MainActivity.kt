package com.agsl.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
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
import kotlin.math.atan2
import kotlin.math.hypot

class MainActivity : Activity() {

    inner class PhysicalFluidView(context: Context) : View(context) {
        private var renderShader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val startTime = System.nanoTime()
        private var lastFrameTime = startTime

        private val vortexManager = VortexHistoryManager(16)
        private var turbulenceEnergy = 0f

        private var p1Id = -1
        private var curP1X = 0f; private var curP1Y = 0f
        private var targetP1Down = 0f; private var curP1Down = 0f
        private var p1Speed = 0f; private var lastX1 = 0f; private var lastY1 = 0f
        private var velX1 = 0f; private var velY1 = 0f
        private var lastTime1 = 0L
        private var pivotX1 = 0f; private var pivotY1 = 0f

        private var p2Id = -1
        private var curP2X = 0f; private var curP2Y = 0f
        private var targetP2Down = 0f; private var curP2Down = 0f
        private var p2Speed = 0f; private var lastX2 = 0f; private var lastY2 = 0f
        private var velX2 = 0f; private var velY2 = 0f
        private var lastTime2 = 0L
        private var pivotX2 = 0f; private var pivotY2 = 0f

        init {
            try {
                renderShader = RuntimeShader(Shaders.RENDER)
                paint.shader = renderShader
            } catch (_: Throwable) {}
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val now = System.nanoTime()
            val dt = ((now - lastFrameTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
            lastFrameTime = now

            vortexManager.update(dt)
            turbulenceEnergy *= (1.0f - 0.75f * dt)

            curP1Down += (targetP1Down - curP1Down) * 0.20f
            curP2Down += (targetP2Down - curP2Down) * 0.20f
            p1Speed *= 0.88f
            p2Speed *= 0.88f
            velX1 *= 0.82f; velY1 *= 0.82f
            velX2 *= 0.82f; velY2 *= 0.82f

            val s = renderShader
            if (s != null && width > 0 && height > 0) {
                val t = (now - startTime) / 1_000_000_000.0f
                s.setFloatUniform("uResolution", width.toFloat(), height.toFloat())
                s.setFloatUniform("uTime", t)
                s.setFloatUniform("uPointer1", curP1X, curP1Y, curP1Down, p1Speed)
                s.setFloatUniform("uPointerVel1", velX1, velY1, 0f, 0f)
                s.setFloatUniform("uPointer2", curP2X, curP2Y, curP2Down, p2Speed)
                s.setFloatUniform("uPointerVel2", velX2, velY2, 0f, 0f)
                s.setFloatUniform("uTurbulence", turbulenceEnergy)

                val (vortexData, count) = vortexManager.getUniformData()
                s.setFloatUniform("uVortices", vortexData)
                s.setFloatUniform("uVortexCount", count)

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
                        pivotX1 = x; pivotY1 = y
                        lastTime1 = e.eventTime; targetP1Down = 1f; p1Speed = 0f
                    } else if (p2Id == -1) {
                        p2Id = pid; curP2X = x; curP2Y = y; lastX2 = x; lastY2 = y
                        pivotX2 = x; pivotY2 = y
                        lastTime2 = e.eventTime; targetP2Down = 1f; p2Speed = 0f
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until e.pointerCount) {
                        val id = e.getPointerId(i)
                        val curX = e.getX(i); val curY = e.getY(i)
                        if (id == p1Id) {
                            val dtMs = (e.eventTime - lastTime1).coerceAtLeast(1L)
                            val dt = dtMs / 1000f
                            val dx = curX - lastX1
                            val dy = curY - lastY1
                            val dist = hypot(dx, dy)
                            val instSpeed = dist / dt

                            velX1 = velX1 * 0.35f + (dx / dt) * 0.65f
                            velY1 = velY1 * 0.35f + (dy / dt) * 0.65f
                            p1Speed = (p1Speed * 0.4f + (instSpeed / 1800f).coerceIn(0f, 1f) * 0.6f)

                            val a1 = atan2(lastY1 - pivotY1, lastX1 - pivotX1)
                            val a2 = atan2(curY - pivotY1, curX - pivotX1)
                            var da = a2 - a1
                            if (da > Math.PI) da -= (2 * Math.PI).toFloat()
                            if (da < -Math.PI) da += (2 * Math.PI).toFloat()

                            if (kotlin.math.abs(da) > 0.035f) {
                                val swirlStrength = da * (1.6f + p1Speed * 2.5f)
                                vortexManager.injectAngularMomentum(curX, curY, swirlStrength)
                                pivotX1 = pivotX1 * 0.90f + curX * 0.10f
                                pivotY1 = pivotY1 * 0.90f + curY * 0.10f
                            } else if (dist > 35f) {
                                pivotX1 = pivotX1 * 0.80f + curX * 0.20f
                                pivotY1 = pivotY1 * 0.80f + curY * 0.20f
                            }

                            if (instSpeed > 2400f) {
                                turbulenceEnergy = (turbulenceEnergy + 0.35f).coerceAtMost(1.0f)
                            }

                            curP1X = curX; curP1Y = curY
                            lastX1 = curX; lastY1 = curY
                            lastTime1 = e.eventTime
                        } else if (id == p2Id) {
                            val dtMs = (e.eventTime - lastTime2).coerceAtLeast(1L)
                            val dt = dtMs / 1000f
                            val dx = curX - lastX2
                            val dy = curY - lastY2
                            val dist = hypot(dx, dy)
                            val instSpeed = dist / dt

                            velX2 = velX2 * 0.35f + (dx / dt) * 0.65f
                            velY2 = velY2 * 0.35f + (dy / dt) * 0.65f
                            p2Speed = (p2Speed * 0.4f + (instSpeed / 1800f).coerceIn(0f, 1f) * 0.6f)

                            val a1 = atan2(lastY2 - pivotY2, lastX2 - pivotX2)
                            val a2 = atan2(curY - pivotY2, curX - pivotX2)
                            var da = a2 - a1
                            if (da > Math.PI) da -= (2 * Math.PI).toFloat()
                            if (da < -Math.PI) da += (2 * Math.PI).toFloat()

                            if (kotlin.math.abs(da) > 0.035f) {
                                val swirlStrength = da * (1.6f + p2Speed * 2.5f)
                                vortexManager.injectAngularMomentum(curX, curY, swirlStrength)
                                pivotX2 = pivotX2 * 0.90f + curX * 0.10f
                                pivotY2 = pivotY2 * 0.90f + curY * 0.10f
                            } else if (dist > 35f) {
                                pivotX2 = pivotX2 * 0.80f + curX * 0.20f
                                pivotY2 = pivotY2 * 0.80f + curY * 0.20f
                            }

                            if (instSpeed > 2400f) {
                                turbulenceEnergy = (turbulenceEnergy + 0.35f).coerceAtMost(1.0f)
                            }

                            curP2X = curX; curP2Y = curY
                            lastX2 = curX; lastY2 = curY
                            lastTime2 = e.eventTime
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
        val preview = PhysicalFluidView(this)
        root.addView(preview, ViewGroup.LayoutParams(-1, -1))

        val hint = TextView(this).apply {
            text = "Physical Hydrodynamic Manifold (Tap: Potential Push | Stir: Vorticity)"
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
