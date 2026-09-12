package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.hypot

class AgslWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = AgslEngine()

    inner class AgslEngine : Engine(), Choreographer.FrameCallback {
        private var renderShader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var isVisible = false
        private var startTime = 0L
        private var width = 0f
        private var height = 0f

        private var p1Id = -1
        private var curP1X = 0f; private var curP1Y = 0f
        private var targetP1Down = 0f; private var curP1Down = 0f
        private var p1Speed = 0f; private var lastTouchX1 = 0f; private var lastTouchY1 = 0f

        private var p2Id = -1
        private var curP2X = 0f; private var curP2Y = 0f
        private var targetP2Down = 0f; private var curP2Down = 0f
        private var p2Speed = 0f; private var lastTouchX2 = 0f; private var lastTouchY2 = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            startTime = System.nanoTime()
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
            if (visible) Choreographer.getInstance().postFrameCallback(this)
            else Choreographer.getInstance().removeFrameCallback(this)
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!isVisible) return
            curP1Down += (targetP1Down - curP1Down) * 0.2f
            curP2Down += (targetP2Down - curP2Down) * 0.2f
            p1Speed *= 0.85f
            p2Speed *= 0.85f
            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }

        private fun drawFrame() {
            if (width <= 0f || height <= 0f) return
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockHardwareCanvas()
                if (canvas != null && renderShader != null) {
                    val t = (System.nanoTime() - startTime) / 1_000_000_000.0f
                    renderShader?.setFloatUniform("uTime", t)
                    renderShader?.setFloatUniform("uPointer1", curP1X, curP1Y, curP1Down, p1Speed)
                    renderShader?.setFloatUniform("uPointer2", curP2X, curP2Y, curP2Down, p2Speed)
                    canvas.drawPaint(paint)
                }
            } finally {
                canvas?.let { try { holder.unlockCanvasAndPost(it) } catch (_: Throwable) {} }
            }
        }

        override fun onTouchEvent(e: MotionEvent) {
            super.onTouchEvent(e)
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
                            val instantSpeed = (dist / 35f).coerceIn(0f, 1f)
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
        }

        override fun onDestroy() {
            super.onDestroy()
            Choreographer.getInstance().removeFrameCallback(this)
            renderShader = null
        }
    }
}
