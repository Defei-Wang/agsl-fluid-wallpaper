package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.atan2
import kotlin.math.hypot

class AgslWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = AgslEngine()

    inner class AgslEngine : Engine(), Choreographer.FrameCallback {
        private var renderShader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var isVisible = false
        private var startTime = 0L
        private var lastFrameTime = 0L
        private var width = 0f
        private var height = 0f

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

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setTouchEventsEnabled(true)
            startTime = System.nanoTime()
            lastFrameTime = startTime
            try {
                renderShader = RuntimeShader(Shaders.RENDER)
                paint.shader = renderShader
            } catch (_: Throwable) {}
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w.toFloat(); height = h.toFloat()
            renderShader?.setFloatUniform("uResolution", width, height)
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                lastFrameTime = System.nanoTime()
                Choreographer.getInstance().postFrameCallback(this)
            } else {
                Choreographer.getInstance().removeFrameCallback(this)
            }
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!isVisible) return
            val dt = ((frameTimeNanos - lastFrameTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
            lastFrameTime = frameTimeNanos

            vortexManager.update(dt)
            turbulenceEnergy *= (1.0f - 0.75f * dt) // 紊流能量黏性衰减

            curP1Down += (targetP1Down - curP1Down) * 0.20f
            curP2Down += (targetP2Down - curP2Down) * 0.20f
            p1Speed *= 0.88f
            p2Speed *= 0.88f
            velX1 *= 0.82f; velY1 *= 0.82f
            velX2 *= 0.82f; velY2 *= 0.82f

            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }

        private fun drawFrame() {
            if (width <= 0f || height <= 0f) return
            val shader = renderShader ?: return
            var canvas: Canvas? = null
            try {
                val t = (System.nanoTime() - startTime) / 1_000_000_000.0f
                shader.setFloatUniform("uResolution", width, height)
                shader.setFloatUniform("uTime", t)
                shader.setFloatUniform("uPointer1", curP1X, curP1Y, curP1Down, p1Speed)
                shader.setFloatUniform("uPointerVel1", velX1, velY1, 0f, 0f)
                shader.setFloatUniform("uPointer2", curP2X, curP2Y, curP2Down, p2Speed)
                shader.setFloatUniform("uPointerVel2", velX2, velY2, 0f, 0f)
                shader.setFloatUniform("uTurbulence", turbulenceEnergy)

                val (vortexData, count) = vortexManager.getUniformData()
                shader.setFloatUniform("uVortices", vortexData)
                shader.setFloatUniform("uVortexCount", count)

                canvas = surfaceHolder.lockHardwareCanvas()
                if (canvas != null) {
                    canvas.drawPaint(paint)
                }
            } finally {
                canvas?.let { try { surfaceHolder.unlockCanvasAndPost(it) } catch (_: Throwable) {} }
            }
        }

        override fun onTouchEvent(e: MotionEvent) {
            super.onTouchEvent(e)
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

                            // 角动量判定：仅在围绕旋转中心产生角位移时才注入旋量
                            val a1 = atan2(lastY1 - pivotY1, lastX1 - pivotX1)
                            val a2 = atan2(curY - pivotY1, curX - pivotX1)
                            var da = a2 - a1
                            if (da > Math.PI) da -= (2 * Math.PI).toFloat()
                            if (da < -Math.PI) da += (2 * Math.PI).toFloat()

                            // 仅当出现明显圆周转角时注入有旋涡度
                            if (kotlin.math.abs(da) > 0.035f) {
                                val swirlStrength = da * (1.6f + p1Speed * 2.5f)
                                vortexManager.injectAngularMomentum(curX, curY, swirlStrength)
                                pivotX1 = pivotX1 * 0.90f + curX * 0.10f
                                pivotY1 = pivotY1 * 0.90f + curY * 0.10f
                            } else if (dist > 35f) {
                                // 若为长距离直线拖动，缓慢移动旋量枢轴点，不累加自旋
                                pivotX1 = pivotX1 * 0.80f + curX * 0.20f
                                pivotY1 = pivotY1 * 0.80f + curY * 0.20f
                            }

                            // 剧烈搅和引发微观紊流
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
        }

        override fun onDestroy() {
            super.onDestroy()
            Choreographer.getInstance().removeFrameCallback(this)
            renderShader = null
        }
    }
}
