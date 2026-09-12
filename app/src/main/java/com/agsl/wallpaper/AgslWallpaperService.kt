package com.agsl.wallpaper // 如未执行自动化改名脚本，请保持为 package com.yuru.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceHolder
import kotlin.math.hypot

class AgslWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = AgslEngine()

    inner class AgslEngine : Engine(), Choreographer.FrameCallback {

        private val TAG = "AgslWallpaper"
        private var shader: RuntimeShader? = null
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var isVisible = false
        private var startTime = 0L
        private var width = 0f
        private var height = 0f

        // 扩充至 16 组长效线段，确保高速滑动下尾迹存活达到 2.5~3.5 秒
        private val MAX_SEGMENTS = 16
        private val segCoords = Array(MAX_SEGMENTS) { FloatArray(4) } // ax, ay, bx, by
        private val segTimes = Array(MAX_SEGMENTS) { FloatArray(4) }  // tA, tB, activeFlag, seed
        private var activeSlot = 0

        private var isDragging = false
        private var currentStrokeSeed = 0f

        // 消除硬件采样抖动的平滑追踪点
        private var rawTouchX = -1f
        private var rawTouchY = -1f
        private var smoothTouchX = -1f
        private var smoothTouchY = -1f
        private var currentSpeed = 0f

        private val agslCode = """
            uniform float2 uResolution;
            uniform float uTime;
            
            ${(0 until MAX_SEGMENTS).joinToString("\n") { "uniform float4 uSegA$it;\nuniform float4 uSegT$it;" }}
            
            float hash(float2 p) {
                return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
            }

            // 具有开尔文长尾扩散特性的有机流体线段波
            half3 getOrganicSegment(float2 uv, float4 segA, float4 segT) {
                if (segT.z < 0.5) return half3(0.0);
                
                float minRes = min(uResolution.x, uResolution.y);
                float2 a = (segA.xy - 0.5 * uResolution) / minRes;
                float2 b = (segA.zw - 0.5 * uResolution) / minRes;
                
                float2 ba = b - a; float2 pa = uv - a;
                float l2 = dot(ba, ba);
                float h = (l2 < 0.00001) ? 0.0 : clamp(dot(pa, ba) / l2, 0.0, 1.0);
                float rawDist = length(pa - ba * h);
                
                float t_touch = mix(segT.x, segT.y, h);
                float age = uTime - t_touch;
                if (age > 3.5 || age <= 0.0) return half3(0.0);
                
                // 膨胀波速：尾部随时间持续平滑扩散至大尺度
                float speed = 0.70;
                float R = age * speed;
                
                // 沿轨迹轴向平滑演变的极角，消除端点畸变
                float2 mid = 0.5 * (a + b);
                float ang = atan(uv.y - mid.y, uv.x - mid.x);
                
                // 标志性多阶有机折叠
                float fold = sin(ang * 2.0);
                float wave1 = sin(rawDist * 8.0 - age * 3.0 + fold * 2.5);
                float wave2 = cos(ang * 3.0 + sin(rawDist * 16.0 - age * 1.5));
                float macroFold = clamp(R * 0.28, 0.0, 0.15) * sin(wave1 + wave2 + sin(rawDist * 22.0 - age * 1.2));
                
                // 消除频闪核心：分母必须保持纯几何连续，严禁注入高频噪波
                float targetR = R + macroFold;
                float d0 = abs(rawDist - targetR);
                float intensity = 0.0055 / (d0 + 0.0032);
                
                // 次级跟随回声波（弱化能量，保持长尾纯净）
                float R1 = max(0.0, R - 0.16);
                if (R1 > 0.0) {
                    intensity += (0.0020 / (abs(rawDist - (R1 + macroFold * 0.7)) + 0.0032)) * step(0.16 / speed, age);
                }

                // 晶粒噪波仅作为分子端温和的调幅乘子，杜绝数值爆炸
                float glitter = 0.025 * abs(sin(ang * 24.0 + rawDist * 36.0 - age * 4.0));
                float noise = 0.030 * fract(hash(uv * 180.0) + age);
                float modulation = 0.88 + 0.24 * (glitter + noise);
                intensity *= modulation;

                // 物理因果律前沿与时空能量衰减（0.0\~0.06 秒诞生，2.2\~3.5 秒从容消散）
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
                
                // 16 组长效线段基于 max 极值透明穿透融合，杜绝接缝与撕裂
                half3 finalColor = half3(0.0);
                ${(0 until MAX_SEGMENTS).joinToString("\n                ") { "finalColor = max(finalColor, getOrganicSegment(uv, uSegA$it, uSegT$it));" }}
                
                // 微弱宇宙星尘底色
                float ambient = smoothstep(0.98, 1.0, hash(uv + uTime * 0.05)) * 0.04;
                finalColor += half3(ambient * 0.5, ambient * 0.6, ambient + 0.02);
                
                return half4(clamp(finalColor, 0.0, 1.0), 1.0);
            }
        """.trimIndent()

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            try {
                shader = RuntimeShader(agslCode)
                paint.shader = shader
                startTime = System.nanoTime()
            } catch (e: Throwable) {
                Log.e(TAG, "Shader compile error", e)
                shader = null
            }
            for (i in 0 until MAX_SEGMENTS) {
                segCoords[i] = floatArrayOf(0f, 0f, 0f, 0f)
                segTimes[i] = floatArrayOf(0f, 0f, 0f, 0f)
            }
        }

        private fun startNewSegment(ax: Float, ay: Float, time: Float) {
            activeSlot = (activeSlot + 1) % MAX_SEGMENTS
            segCoords[activeSlot][0] = ax
            segCoords[activeSlot][1] = ay
            segCoords[activeSlot][2] = ax
            segCoords[activeSlot][3] = ay
            segTimes[activeSlot][0] = time
            segTimes[activeSlot][1] = time
            segTimes[activeSlot][2] = 1f // 激活
            segTimes[activeSlot][3] = currentStrokeSeed
        }

        override fun onTouchEvent(event: MotionEvent) {
            super.onTouchEvent(event)
            if (width <= 0f || height <= 0f) return

            val elapsedTimeSec = (System.nanoTime() - startTime) / 1_000_000_000.0f

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    currentStrokeSeed = elapsedTimeSec
                    rawTouchX = event.x
                    rawTouchY = event.y
                    smoothTouchX = event.x
                    smoothTouchY = event.y
                    currentSpeed = 0f
                    startNewSegment(event.x, event.y, elapsedTimeSec)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) return
                    rawTouchX = event.x
                    rawTouchY = event.y

                    val ax = segCoords[activeSlot][0]
                    val ay = segCoords[activeSlot][1]
                    val moveDist = hypot((event.x - ax).toDouble(), (event.y - ay).toDouble()).toFloat()
                    currentSpeed = currentSpeed * 0.6f + moveDist * 0.4f

                    // 动态自适应步长：慢速时细致（120px），疾速时舒展（拉伸至 260px）
                    val dynamicThreshold = (120f + currentSpeed * 0.5f).coerceIn(120f, 260f)
                    val timeSpan = elapsedTimeSec - segTimes[activeSlot][0]

                    // 空间位移与时间窗口双达标才开启下一段，确保尾迹拥有充分存活期
                    if (moveDist >= dynamicThreshold && timeSpan >= 0.14f) {
                        startNewSegment(event.x, event.y, elapsedTimeSec)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                }
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            if (w <= 0 || h <= 0) return
            width = w.toFloat()
            height = h.toFloat()
            try {
                shader?.setFloatUniform("uResolution", width, height)
            } catch (e: Throwable) {
                Log.e(TAG, "Resolution error", e)
            }
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
            if (!isVisible) return

            // 硬件级平滑低通滤波（IIR 0.85），过滤电容屏高频采样抖动
            if (isDragging && smoothTouchX > 0f) {
                smoothTouchX += (rawTouchX - smoothTouchX) * 0.85f
                smoothTouchY += (rawTouchY - smoothTouchY) * 0.85f

                // 将滤波后的坐标与当前真实帧时间锁同步更新至活动端点
                segCoords[activeSlot][2] = smoothTouchX
                segCoords[activeSlot][3] = smoothTouchY
                val elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f
                segTimes[activeSlot][1] = elapsed
            }
            currentSpeed *= 0.88f

            drawFrame()
            Choreographer.getInstance().postFrameCallback(this)
        }

        private fun drawFrame() {
            if (width <= 0f || height <= 0f) return
            val holder = surfaceHolder
            var canvas: Canvas? = null

            try {
                canvas = holder.lockHardwareCanvas()
                if (canvas != null) {
                    canvas.drawColor(Color.rgb(8, 4, 18))
                    val s = shader ?: return
                    val elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0f

                    try {
                        s.setFloatUniform("uTime", elapsed)

                        for (i in 0 until MAX_SEGMENTS) {
                            s.setFloatUniform("uSegA$i", segCoords[i])
                            s.setFloatUniform("uSegT$i", segTimes[i])
                        }

                        canvas.drawPaint(paint)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Render uniform error", e)
                    }
                }
            } finally {
                canvas?.let {
                    try {
                        holder.unlockCanvasAndPost(it)
                    } catch (_: Throwable) {}
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            Choreographer.getInstance().removeFrameCallback(this)
            shader = null
        }
    }
}