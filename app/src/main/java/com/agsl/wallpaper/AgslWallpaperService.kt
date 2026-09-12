package com.agsl.wallpaper

import android.graphics.Canvas
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder

class AgslWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = DynamicEcologyEngine()

    inner class DynamicEcologyEngine : Engine(), Choreographer.FrameCallback {
        private val ecology = JellyfishEcology()
        private var isVisible = false
        private var width = 0f
        private var height = 0f

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setTouchEventsEnabled(true)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w.toFloat(); height = h.toFloat()
            ecology.ensureInit(width, height)
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                Choreographer.getInstance().postFrameCallback(this)
            } else {
                Choreographer.getInstance().removeFrameCallback(this)
            }
        }

        override fun doFrame(frameTimeNanos: Long) {
            if (!isVisible || width <= 0f || height <= 0f) return
            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }

        private fun drawFrame() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockHardwareCanvas()
                if (canvas != null) {
                    ecology.render(canvas, width, height)
                }
            } finally {
                canvas?.let { try { surfaceHolder.unlockCanvasAndPost(it) } catch (_: Throwable) {} }
            }
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            ecology.onTouchEvent(event, width, height)
        }

        override fun onDestroy() {
            super.onDestroy()
            Choreographer.getInstance().removeFrameCallback(this)
        }
    }
}
