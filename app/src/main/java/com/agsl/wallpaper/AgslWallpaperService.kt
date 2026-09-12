package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
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
        private var shader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var isVisible = false
        private var startTime = 0L
        private var width = 0f
        private var height = 0f

        private val MAX_RIPPLES = 8
        private val ripples = Array(MAX_RIPPLES) { FloatArray(4) }
        private var rippleIndex = 0
        private var targetTouchX = -1f; private var targetTouchY = -1f
        private var currentTouchX = -1f; private var currentTouchY = -1f
        private var currentSpeed = 0f; private var targetAlpha = 0f; private var currentAlpha = 0f
        private var lastSpawnTimeSec = 0f; private var lastSpawnX = -1f; private var lastSpawnY = -1f

        private val agslCode = """
            uniform float2 uResolution;
            uniform float uTime;
            uniform float4 uCursor;
            ${(0 until MAX_RIPPLES).joinToString("\n") { "uniform float4 uR$it;" }}
            
            float hash(float2 p) { return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453); }

            half3 getRichRipple(float2 uv, float4 r) {
                float age = max(0.0, uTime - r.z);
                if (r.w < 0.5 || age > 3.5) return half3(0.0);
                float seed = fract(r.z * 13.513);
                float lobes = floor(2.0 + fract(seed * 41.2) * 3.0); 
                float speed = 0.85 + fract(seed * 17.5) * 0.35;
                half3 rippleColor = mix(half3(0.3, 0.85, 1.5), half3(1.1, 0.45, 1.35), fract(seed * 93.1));
                float minRes = min(uResolution.x, uResolution.y);
                float2 delta = uv - (r.xy - 0.5 * uResolution) / minRes;
                float dist = length(delta) * 2.2;
                float a = atan(delta.y, delta.x) + seed * 6.283;
                float fold = sin(a * lobes);
                float w0 = sin(dist * 4.0 - age * 2.2 + fold * 2.5);
                float q0 = age * speed + 0.16 * sin(w0 + cos(a * 3.0 + dist * 6.0 - age * 1.2));
                float noise = 0.06 * fract(hash(uv * 160.0) + age);
                float intensity = 0.0042 / (abs(dist - q0) + noise + 0.0016);
                float q1 = max(0.0, age - 0.22) * speed + 0.14 * sin(w0);
                intensity += (0.0022 / (abs(dist - q1) + noise + 0.0016)) * step(0.22, age);
                return rippleColor * half(intensity * smoothstep(3.5, 2.0, age) * smoothstep(0.0, 0.1, age));
            }

            half4 main(float2 fragCoord) {
                float minRes = min(uResolution.x, uResolution.y);
                if (minRes <= 0.0) return half4(0.03, 0.015, 0.07, 1.0);
                float2 uv = (fragCoord - 0.5 * uResolution) / minRes;
                half3 finalColor = half3(0.0);
                ${(0 until MAX_RIPPLES).joinToString("\n") { "finalColor = max(finalColor, getRichRipple(uv, uR$it));" }}
                float2 cursorPos = (uCursor.xy - 0.5 * uResolution) / minRes;
                float cursorDist = length(uv - cursorPos);
                float speedFactor = clamp(uCursor.z, 0.0, 1.0);
                float dotCore = exp(-cursorDist * mix(45.0, 110.0, speedFactor)) * 0.95;
                float dotGlow = exp(-cursorDist * mix(14.0, 32.0, speedFactor)) * 0.35;
                finalColor = max(finalColor, mix(half3(0.3, 0.85, 1.5), half3(0.9, 0.4, 1.4), speedFactor) * half((dotCore + dotGlow) * uCursor.w));
                float ambient = smoothstep(0.98, 1.0, hash(uv + uTime * 0.05)) * 0.05;
                return half4(clamp(finalColor + half3(ambient * 0.6, ambient * 0.7, ambient + 0.02), 0.0, 1.0), 1.0);
            }
        """.trimIndent()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            shader = RuntimeShader(agslCode)
            paint.shader = shader
            startTime = System.nanoTime()
            for (i in 0 until MAX_RIPPLES) ripples[i] = floatArrayOf(0f, 0f, 0f, 0f)
        }
        private fun spawnRipple(x: Float, y: Float, t: Float) {
            ripples[rippleIndex] = floatArrayOf(x, y, t, 1f)
            rippleIndex = (rippleIndex + 1) % MAX_RIPPLES
            lastSpawnTimeSec = t; lastSpawnX = x; lastSpawnY = y
        }
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            val t = (System.nanoTime() - startTime) / 1e9f
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    targetTouchX = event.x; targetTouchY = event.y; currentTouchX = event.x; currentTouchY = event.y
                    targetAlpha = 1f; currentSpeed = 0f; spawnRipple(event.x, event.y, t)
                }
                MotionEvent.ACTION_MOVE -> {
                    currentSpeed = currentSpeed * 0.6f + hypot(event.x - targetTouchX, event.y - targetTouchY) * 0.4f
                    targetTouchX = event.x; targetTouchY = event.y; targetAlpha = 1f
                    if (hypot(event.x - lastSpawnX, event.y - lastSpawnY) >= 140f && (t - lastSpawnTimeSec) >= 0.28f) spawnRipple(event.x, event.y, t)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> targetAlpha = 0f
            }
        }
        override fun onSurfaceChanged(h: SurfaceHolder, f: Int, w: Int, height: Int) {
            super.onSurfaceChanged(h, f, w, height)
            width = w.toFloat(); this.height = height.toFloat()
            shader?.setFloatUniform("uResolution", width, this.height)
            drawFrame()
        }
        override fun onVisibilityChanged(v: Boolean) {
            super.onVisibilityChanged(v)
            isVisible = v
            if (v) Choreographer.getInstance().postFrameCallback(this) else Choreographer.getInstance().removeFrameCallback(this)
        }
        override fun doFrame(frameTimeNanos: Long) {
            if (!isVisible) return
            currentTouchX += (targetTouchX - currentTouchX) * 0.85f
            currentTouchY += (targetTouchY - currentTouchY) * 0.85f
            currentAlpha += (targetAlpha - currentAlpha) * 0.25f
            currentSpeed *= 0.82f
            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }
        private fun drawFrame() {
            if (width <= 0f) return
            val canvas = surfaceHolder.lockHardwareCanvas() ?: return
            try {
                canvas.drawColor(Color.rgb(8, 4, 18))
                val s = shader ?: return
                s.setFloatUniform("uTime", (System.nanoTime() - startTime) / 1e9f)
                s.setFloatUniform("uCursor", currentTouchX, currentTouchY, (currentSpeed / 70f).coerceIn(0f, 1f), currentAlpha)
                for (i in 0 until MAX_RIPPLES) s.setFloatUniform("uR$i", ripples[i])
                canvas.drawPaint(paint)
            } finally { surfaceHolder.unlockCanvasAndPost(canvas) }
        }
    }
}
