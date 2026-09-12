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

        private val MAX_SEGMENTS = 16
        private val segCoords = Array(MAX_SEGMENTS) { FloatArray(4) }
        private val segTimes = Array(MAX_SEGMENTS) { FloatArray(4) }
        private var ringIndex = 0
        private var isDragging = false
        private var lastX = 0f; private var lastY = 0f; private var lastTimeSec = 0f
        private var currentForce = 0.40f

        private val processSegmentsCode = (0 until MAX_SEGMENTS).joinToString("\n") { i ->
            """
            {
                float4 sA = uSegA$i; float4 sT = uSegT$i;
                if (sT.z > 0.5) {
                    float2 a = (sA.xy - 0.5 * uResolution) / minRes;
                    float2 b = (sA.zw - 0.5 * uResolution) / minRes;
                    float2 ba = b - a; float2 pa = uv - a;
                    float l2 = dot(ba, ba);
                    float h = (l2 > 0.00002) ? clamp(dot(pa, ba) / l2, 0.0, 1.0) : 0.0;
                    float d = length(pa - ba * h);
                    phi = min(phi, d + 0.38 * mix(sT.x, sT.y, h));
                    dAll = min(dAll, d);
                    force = max(force, sT.w);
                }
            }
            """
        }

        private val agslCode = """
            uniform float2 uResolution;
            uniform float uTime;
            ${(0 until MAX_SEGMENTS).joinToString("\n") { "uniform float4 uSegA$it;\nuniform float4 uSegT$it;" }}

            half4 main(float2 fragCoord) {
                float minRes = min(uResolution.x, uResolution.y);
                if (minRes <= 0.0) return half4(0.015, 0.015, 0.035, 1.0);
                float2 uv = (fragCoord - 0.5 * uResolution) / minRes;
                float phi = 1e9; float dAll = 1e9; float force = 0.35;
                $processSegmentsCode

                if (phi > 1e8) return half4(0.015, 0.015, 0.035, 1.0);

                float speed = 0.38;
                float distFromFront = phi - speed * uTime;
                float targetR = max(0.0, speed * uTime - phi + dAll);
                float intensity = 0.0072 / (abs(distFromFront) + 0.0012);
                float R1 = max(0.0, targetR - 0.16);
                if (R1 > 0.0) intensity += 0.0032 / (abs(distFromFront + 0.16) + 0.0012);
                if (force > 0.45) {
                    float R2 = max(0.0, targetR - 0.30);
                    if (R2 > 0.0) intensity += (0.0020 * force) / (abs(distFromFront + 0.30) + 0.0012);
                }

                float frontMask = smoothstep(0.022, -0.005, distFromFront);
                float spatialDecay = 1.0 / sqrt(max(0.03, targetR * 2.2 + 0.06));
                float age = uTime - phi / speed;
                float w = intensity * frontMask * spatialDecay * smoothstep(mix(2.8, 4.0, force), mix(2.8, 4.0, force) * 0.55, age) * smoothstep(0.0, 0.04, age);
                float colorGain = mix(0.25, 0.28, force);
                half3 col = half3(0.0, 0.0, 0.0);
                col += mix(half3(1.00, 0.52, 0.20), half3(1.00, 0.36, 0.28), half(force)) * half(w * colorGain);
                col += mix(half3(0.30, 0.58, 1.00), half3(0.20, 0.80, 1.00), half(force)) * half(w * (colorGain * 0.50));
                return half4(clamp(half3(0.015, 0.015, 0.035) + col, 0.0, 1.0), 1.0);
            }
        """.trimIndent()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            shader = RuntimeShader(agslCode)
            paint.shader = shader
            startTime = System.nanoTime()
            for (i in 0 until MAX_SEGMENTS) {
                segCoords[i] = floatArrayOf(0f, 0f, 0f, 0f)
                segTimes[i] = floatArrayOf(0f, 0f, 0f, 0f)
            }
        }
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            val t = (System.nanoTime() - startTime) / 1e9f
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true; currentForce = 0.40f; lastX = event.x; lastY = event.y; lastTimeSec = t
                    ringIndex = (ringIndex + 1) % MAX_SEGMENTS
                    segCoords[ringIndex] = floatArrayOf(event.x, event.y, event.x, event.y)
                    segTimes[ringIndex] = floatArrayOf(t, t, 1f, currentForce)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) return
                    val dist = hypot(event.x - lastX, event.y - lastY)
                    val dt = (t - lastTimeSec).coerceAtLeast(0.001f)
                    currentForce = (currentForce * 0.7f + (0.25f + ((dist / dt) / 1500f).coerceIn(0f, 1f) * 0.75f) * 0.3f).coerceIn(0.2f, 0.95f)
                    segCoords[ringIndex][2] = event.x; segCoords[ringIndex][3] = event.y; segTimes[ringIndex][1] = t; segTimes[ringIndex][3] = currentForce
                    if (dist >= 40f) {
                        ringIndex = (ringIndex + 1) % MAX_SEGMENTS
                        segCoords[ringIndex] = floatArrayOf(event.x, event.y, event.x, event.y)
                        segTimes[ringIndex] = floatArrayOf(t, t, 1f, currentForce)
                        lastX = event.x; lastY = event.y; lastTimeSec = t
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDragging = false
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
            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }
        private fun drawFrame() {
            if (width <= 0f) return
            val canvas = surfaceHolder.lockHardwareCanvas() ?: return
            try {
                canvas.drawColor(Color.rgb(4, 4, 9))
                val s = shader ?: return
                s.setFloatUniform("uTime", (System.nanoTime() - startTime) / 1e9f)
                for (i in 0 until MAX_SEGMENTS) {
                    s.setFloatUniform("uSegA$i", segCoords[i])
                    s.setFloatUniform("uSegT$i", segTimes[i])
                }
                canvas.drawPaint(paint)
            } finally { surfaceHolder.unlockCanvasAndPost(canvas) }
        }
    }
}
