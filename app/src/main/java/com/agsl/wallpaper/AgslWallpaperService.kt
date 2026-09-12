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
        private var activeSlot = 0
        private var isDragging = false
        private var currentStrokeSeed = 0f
        private var rawTouchX = -1f; private var rawTouchY = -1f
        private var smoothTouchX = -1f; private var smoothTouchY = -1f
        private var currentSpeed = 0f

        private val agslCode = """
            uniform float2 uResolution;
            uniform float uTime;
            ${(0 until MAX_SEGMENTS).joinToString("\n") { "uniform float4 uSegA$it;\nuniform float4 uSegT$it;" }}
            
            float hash(float2 p) { return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453); }

            half3 getOrganicSegment(float2 uv, float4 segA, float4 segT) {
                if (segT.z < 0.5) return half3(0.0);
                float minRes = min(uResolution.x, uResolution.y);
                float2 a = (segA.xy - 0.5 * uResolution) / minRes;
                float2 b = (segA.zw - 0.5 * uResolution) / minRes;
                float2 ba = b - a; float2 pa = uv - a;
                float l2 = dot(ba, ba);
                float h = (l2 < 0.00001) ? 0.0 : clamp(dot(pa, ba) / l2, 0.0, 1.0);
                float rawDist = length(pa - ba * h);
                float age = uTime - mix(segT.x, segT.y, h);
                if (age > 3.5 || age <= 0.0) return half3(0.0);
                
                float speed = 0.70;
                float R = age * speed;
                float ang = atan(uv.y - 0.5 * (a.y + b.y), uv.x - 0.5 * (a.x + b.x));
                float wave1 = sin(rawDist * 8.0 - age * 3.0 + sin(ang * 2.0) * 2.5);
                float wave2 = cos(ang * 3.0 + sin(rawDist * 16.0 - age * 1.5));
                float macroFold = clamp(R * 0.28, 0.0, 0.15) * sin(wave1 + wave2 + sin(rawDist * 22.0 - age * 1.2));
                float targetR = R + macroFold;
                float intensity = 0.0055 / (abs(rawDist - targetR) + 0.0032);
                float R1 = max(0.0, R - 0.16);
                if (R1 > 0.0) intensity += (0.0020 / (abs(rawDist - (R1 + macroFold * 0.7)) + 0.0032)) * step(0.16 / speed, age);

                float glitter = 0.025 * abs(sin(ang * 24.0 + rawDist * 36.0 - age * 4.0));
                float noise = 0.030 * fract(hash(uv * 180.0) + age);
                intensity *= (0.88 + 0.24 * (glitter + noise));

                float frontMask = smoothstep(0.03, -0.01, rawDist - targetR);
                float spatialDecay = 1.0 / sqrt(max(0.04, R * 2.0 + 0.08));
                float temporalFade = smoothstep(3.5, 2.0, age) * smoothstep(0.0, 0.06, age);
                half3 col = mix(half3(0.25, 0.85, 1.55), half3(1.15, 0.40, 1.35), fract(segT.w * 17.713));
                return col * half(intensity * frontMask * spatialDecay * temporalFade);
            }

            half4 main(float2 fragCoord) {
                float minRes = min(uResolution.x, uResolution.y);
                if (minRes <= 0.0) return half4(0.0, 0.0, 0.0, 1.0);
                float2 uv = (fragCoord - 0.5 * uResolution) / minRes;
                half3 finalColor = half3(0.0);
                ${(0 until MAX_SEGMENTS).joinToString("\n                ") { "finalColor = max(finalColor, getOrganicSegment(uv, uSegA$it, uSegT$it));" }}
                float ambient = smoothstep(0.98, 1.0, hash(uv + uTime * 0.05)) * 0.04;
                return half4(clamp(finalColor + half3(ambient * 0.5, ambient * 0.6, ambient + 0.02), 0.0, 1.0), 1.0);
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
        private fun startNewSegment(ax: Float, ay: Float, t: Float) {
            activeSlot = (activeSlot + 1) % MAX_SEGMENTS
            segCoords[activeSlot] = floatArrayOf(ax, ay, ax, ay)
            segTimes[activeSlot] = floatArrayOf(t, t, 1f, currentStrokeSeed)
        }
        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            val t = (System.nanoTime() - startTime) / 1e9f
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true; currentStrokeSeed = t; rawTouchX = event.x; rawTouchY = event.y
                    smoothTouchX = event.x; smoothTouchY = event.y; currentSpeed = 0f; startNewSegment(event.x, event.y, t)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) return
                    rawTouchX = event.x; rawTouchY = event.y
                    val dist = hypot(event.x - segCoords[activeSlot][0], event.y - segCoords[activeSlot][1])
                    currentSpeed = currentSpeed * 0.6f + dist * 0.4f
                    if (dist >= (120f + currentSpeed * 0.5f).coerceIn(120f, 260f) && (t - segTimes[activeSlot][0]) >= 0.14f) startNewSegment(event.x, event.y, t)
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
            if (isDragging && smoothTouchX > 0f) {
                smoothTouchX += (rawTouchX - smoothTouchX) * 0.85f
                smoothTouchY += (rawTouchY - smoothTouchY) * 0.85f
                segCoords[activeSlot][2] = smoothTouchX; segCoords[activeSlot][3] = smoothTouchY
                segTimes[activeSlot][1] = (System.nanoTime() - startTime) / 1e9f
            }
            currentSpeed *= 0.88f
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
                for (i in 0 until MAX_SEGMENTS) {
                    s.setFloatUniform("uSegA$i", segCoords[i])
                    s.setFloatUniform("uSegT$i", segTimes[i])
                }
                canvas.drawPaint(paint)
            } finally { surfaceHolder.unlockCanvasAndPost(canvas) }
        }
    }
}
