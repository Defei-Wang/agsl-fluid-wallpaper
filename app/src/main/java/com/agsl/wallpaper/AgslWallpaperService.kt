package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.service.wallpaper.WallpaperService
import android.view.Choreographer
import android.view.SurfaceHolder

class AgslWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = AgslEngine()

    inner class AgslEngine : Engine(), Choreographer.FrameCallback {
        private var shader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var isVisible = false
        private var startTime = 0L
        private var width = 0f
        private var height = 0f

        private val agslCode = """
            uniform float2 uResolution;
            uniform float uTime;
            float hash(float2 p) {
                return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
            }
            half4 main(float2 fragCoord) {
                float minRes = min(uResolution.x, uResolution.y);
                if (minRes <= 0.0) return half4(0.0, 0.0, 0.0, 1.0);
                float2 uv = (fragCoord - 0.5 * uResolution) / minRes;
                float r = length(uv) * 2.2;
                float a = atan(uv.y, uv.x);
                float fold = sin(a * 2.0);
                float wave1 = sin(r * 4.0 - uTime * 1.5 + fold * 3.0);
                float wave2 = cos(a * 3.0 + sin(r * 8.0 - uTime * 0.8));
                float q = 0.85 + 0.35 * sin(wave1 + wave2 + sin(r * 12.0 - uTime * 0.5));
                float dist = abs(r - q) + 0.04 * abs(sin(a * 32.0 + r * 20.0 - uTime * 2.0));
                float intensity = 0.0062 / (dist + 0.0014) * smoothstep(2.6, 0.1, r);
                half3 color = clamp(half3(0.95, 0.82, 1.35) * half(intensity), 0.0, 1.0);
                float ambient = smoothstep(0.98, 1.0, hash(uv + uTime * 0.05)) * 0.04;
                color += half3(ambient * 0.5, ambient * 0.6, ambient + 0.02);
                return half4(clamp(color, 0.0, 1.0), 1.0);
            }
        """.trimIndent()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(false)
            try {
                shader = RuntimeShader(agslCode)
                paint.shader = shader
                startTime = System.nanoTime()
            } catch (_: Throwable) { shader = null }
        }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            width = w.toFloat(); height = h.toFloat()
            shader?.setFloatUniform("uResolution", width, height)
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
            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }
        private fun drawFrame() {
            if (width <= 0f) return
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockHardwareCanvas()
                if (canvas != null && shader != null) {
                    shader?.setFloatUniform("uTime", (System.nanoTime() - startTime) / 1e9f)
                    canvas.drawPaint(paint)
                }
            } finally { canvas?.let { holder.unlockCanvasAndPost(it) } }
        }
        override fun onDestroy() {
            super.onDestroy()
            Choreographer.getInstance().removeFrameCallback(this)
            shader = null
        }
    }
}
