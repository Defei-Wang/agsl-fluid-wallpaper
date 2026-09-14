package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*
import kotlin.random.Random

object FastMath {
    const val TABLE_SIZE = 8192
    const val MASK = TABLE_SIZE - 1
    const val RAD_TO_INDEX = 1303.7972938f
    
    val SIN_TABLE = FloatArray(TABLE_SIZE) { i ->
        kotlin.math.sin((i + 0.5f) / TABLE_SIZE * 2f * PI.toFloat())
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sin(rad: Float): Float {
        val idx = (rad * RAD_TO_INDEX).toInt() and MASK
        return SIN_TABLE[idx]
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun cos(rad: Float): Float {
        val idx = ((rad * RAD_TO_INDEX).toInt() + 2048) and MASK
        return SIN_TABLE[idx]
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun hypot(x: Float, y: Float): Float = sqrt(x * x + y * y)

    @Suppress("NOTHING_TO_INLINE")
    inline fun atan2(y: Float, x: Float): Float = kotlin.math.atan2(y.toDouble(), x.toDouble()).toFloat()

    @Suppress("NOTHING_TO_INLINE")
    inline fun exp(v: Float): Float = kotlin.math.exp(v)

    @Suppress("NOTHING_TO_INLINE")
    inline fun ln(v: Float): Float = kotlin.math.ln(v)
}

abstract class AbyssalOrganism(
    var x: Float,
    var y: Float,
    val scaleFactor: Float,
    val baseSpeed: Float,
    val speciesId: Int,
    val rgb: IntArray,
    var driftVx: Float,
    var driftVy: Float,
    var t: Float,
    var layerIndex: Int
) {
    companion object {
        const val MAX_POINTS = 12000
    }

    var isOffscreen = false
    var startlePulse = 0f
    protected var escapeBoost = 0f

    var surgeVx = 0f
    var surgeVy = 0f

    val pts = FloatArray(MAX_POINTS * 2)
    var validPointCount = 0

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    protected val motionScale = 0.28f

    open fun getPointHitRadius(baseScale: Float): Float = (52f * baseScale).coerceIn(32f, 90f)

    abstract fun evaluateShape(currentScale: Float): Int

    open fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = (1f + escapeBoost).coerceAtMost(7.5f)

        x += (driftVx * motionScale * speedMultiplier + surgeVx) * frameFactor
        y += (driftVy * motionScale * speedMultiplier + surgeVy) * frameFactor

        val decay = (1f - 0.040f * frameFactor).coerceIn(0.88f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay
        surgeVx *= decay
        surgeVy *= decay

        t += baseSpeed * motionScale * speedMultiplier * frameFactor

        val margin = 260f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    open fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor

        paint.strokeWidth = 1.8f

        val baseIdleAlpha = 35f + layerIndex * 11.0f
        val glowStrength = if (speciesId in intArrayOf(0, 1, 2, 3, 6)) 22f else 35f
        val alpha = (baseIdleAlpha + startlePulse * glowStrength).coerceIn(baseIdleAlpha, 230f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    open fun hitTest(tx: Float, ty: Float, baseScale: Float): Float {
        val threshold = getPointHitRadius(baseScale)
        val threshold2 = threshold * threshold
        var bestD2 = Float.MAX_VALUE

        var i = 0
        while (i < validPointCount) {
            val px = pts[i * 2]
            val py = pts[i * 2 + 1]
            if (px.isFinite() && py.isFinite()) {
                val dx = tx - px
                val dy = ty - py
                val d2 = dx * dx + dy * dy
                if (d2 < bestD2 && d2 <= threshold2) {
                    bestD2 = d2
                }
            }
            i += 4
        }
        return bestD2
    }

    open fun clearReaction() {
        startlePulse = 0f
        escapeBoost = 0f
    }

    open fun applyImpulse() {
        escapeBoost = (escapeBoost + 2.5f).coerceAtMost(6.0f)
        startlePulse = 1.4f
    }

    protected fun addPoint(ptr: Int, px: Float, py: Float, maxR2: Float): Int {
        if (!px.isFinite() || !py.isFinite()) return ptr
        val dx = px - x
        val dy = py - y
        if (dx * dx + dy * dy > maxR2) return ptr
        if (ptr + 1 >= pts.size) return ptr

        pts[ptr] = px
        pts[ptr + 1] = py
        return ptr + 2
    }
}

// 0 瘦虫 (中景 Layer 2，8000点动态金粉流光全覆盖)
class SlimBug(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 2
) : AbyssalOrganism(x, y, sc, (PI / 180.0).toFloat(), 0, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 0.55f).coerceAtMost(1.2f)
        startlePulse = 0.8f
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 480f * currentScale
        val maxR2 = maxR * maxR
        val count = 8000
        val step = 20000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 500f
            // 加入时间相位 t * 0.2f，让瘦虫的金粉高光全虫身动态流淌
            val k = FastMath.cos(yVal * 9f + t * 0.2f) * (if (yVal < 9f) FastMath.sin(t + yVal) * 28f else 11f)
            val e = yVal / 8f - 13f
            val o = FastMath.hypot(k, e) / 6f
            val q = k * yVal / 15f + 79f + k * FastMath.sin(yVal) * (1f + FastMath.sin(o * 4f - e - t * 8f))
            val c = o / 2f - e / 4f - t
            val rx = (q * FastMath.sin(c) + 70f * FastMath.sin(c / 3f)) * currentScale
            val ry = ((q / 0.7f) * FastMath.cos(c)) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 1 胖虫 (中景 Layer 2，加入动态相位扫描，金粉流光贯穿全身、从始至终无死角)
class PlumpBug(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 2
) : AbyssalOrganism(x, y, sc, (PI / 160.0).toFloat(), 1, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 0.55f).coerceAtMost(1.2f)
        startlePulse = 0.8f
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 480f * currentScale
        val maxR2 = maxR * maxR
        val count = 8000
        val step = 20000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 600f
            // 核心修复：在 cos 空间频率中加入时间动态项 t * 0.25f，使焦散金粉带平滑扫过全虫身
            val k = FastMath.cos(yVal * 7f + t * 0.25f) * (if (yVal < 19f) FastMath.sin(t / 8f + yVal * 9f) * 31f else 9f)
            val e = yVal / 8f - 13f
            val o = FastMath.hypot(k, e) / 6f
            
            val safeK = if (abs(k) < 0.001f) (if (k >= 0f) 0.001f else -0.001f) else k
            val safeO = max(o, 0.001f)
            val safeE = if (abs(e) < 0.001f) (if (e >= 0f) 0.001f else -0.001f) else e

            val q = 59f + FastMath.cos(yVal) / safeK + (k / safeO) * 4f * (5f / safeO / safeE * 9f + FastMath.sin(o * 3f - e * 9f - t))
            val c = o / 2f - e / 6f - t / 8f
            val rx = (q * FastMath.sin(c) - 28f) * currentScale
            val ry = ((q + 70f) * FastMath.cos(c) + 75f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 2 冠状长水母 (中深景 Layer 1，标量游速适度加快，8000点纤毛，霜月清蓝)
class CrownLongMedusa(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 1
) : AbyssalOrganism(x, y, sc, (PI / 240.0).toFloat(), 2, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 3.2f).coerceAtMost(6.5f)
        startlePulse = 2.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = (1f + escapeBoost).coerceAtMost(6.5f)

        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.035f * frameFactor).coerceIn(0.90f, 0.99f)
        escapeBoost *= decay
        startlePulse *= decay

        val pulseRate = 1f + escapeBoost * 0.45f
        t += baseSpeed * motionScale * speedMultiplier * pulseRate * frameFactor

        val margin = 260f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 540f * currentScale
        val maxR2 = maxR * maxR
        val count = 8000
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val xVal = i % 200f
            val yVal = i / 43f
            val k = 5f * FastMath.cos(xVal / 14f) * FastMath.cos(yVal / 30f)
            val e = yVal / 8f - 13f
            val d = (k * k + e * e) / 59f + 4f
            val q = 60f - 3f * FastMath.sin(FastMath.atan2(k, e) * e) + k * (3f + 4f / d * FastMath.sin(d * d - t * 2f))
            val c = d / 2f + e / 99f - t / 18f
            val rx = (q * FastMath.sin(c)) * currentScale
            val ry = ((q + d * 9f) * FastMath.cos(c) + 65f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 3 超长尾水母 (中深景 Layer 1，标量游速适度加快，8000点长拖尾，霜月清蓝)
class UltraLongMedusa(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 1
) : AbyssalOrganism(x, y, sc, (PI / 280.0).toFloat(), 3, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 3.2f).coerceAtMost(6.5f)
        startlePulse = 2.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = (1f + escapeBoost).coerceAtMost(6.5f)

        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.035f * frameFactor).coerceIn(0.90f, 0.99f)
        escapeBoost *= decay
        startlePulse *= decay

        val pulseRate = 1f + escapeBoost * 0.45f
        t += baseSpeed * motionScale * speedMultiplier * pulseRate * frameFactor

        val margin = 260f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 620f * currentScale
        val maxR2 = maxR * maxR
        val count = 8000
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val xVal = i
            val yVal = i / 41f
            val k = 5f * FastMath.cos(xVal / 19f) * FastMath.cos(yVal / 30f)
            val e = yVal / 8f - 12f
            val d = (k * k + e * e) / 59f + 2f
            val c = d * d / 7f - t
            val safeD = max(d, 0.1f)
            val q = 4f * FastMath.sin(FastMath.atan2(k, e) * 9f) + 9f * FastMath.sin(d - t) - (k / safeD) * (9f + FastMath.sin(d * 9f - t * 16f) * 3f)

            val rx = (q + 50f * FastMath.cos(c)) * currentScale
            val ry = (q * FastMath.sin(c) + d * 45f - 160f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 4 经典小水母 (常态暗淡通透，仅受触时变为明亮金白，6000点整数晶格)
class SmallJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int,
    val m: Float = Random.nextFloat() * 20f
) : AbyssalOrganism(x, y, sc, (PI / 36.0).toFloat(), 4, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        super.applyImpulse()
        surgeVy = -1.6f
        startlePulse = 1.8f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.8f

        val baseIdleAlpha = 20f + layerIndex * 4.5f
        val alpha = (baseIdleAlpha + startlePulse * 95f).coerceIn(baseIdleAlpha, 220f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 300f * currentScale
        val maxR2 = maxR * maxR
        val count = 6000

        for (p in count - 1 downTo 0) {
            val i = p.toFloat()
            val k = 2f * FastMath.cos(i * 342f)
            val e = FastMath.sin(i * 271f) * 2f
            val d = FastMath.hypot(k, e) / 1.6f
            val pp = 5f + 2f * FastMath.sin(d * 8f - t * 3f + m)
            val c = (d * d / 9f) - t / 8f + m
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d

            val rx = (k * pp + (9f / safeD) * FastMath.sin(k * 2f) + 89f * FastMath.sin(c)) * currentScale
            val ry = (79f * FastMath.sin(c * 2f) + (9f / safeD) * FastMath.sin(e * 2f) + e * pp) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 5 单体自然水螅体 (羽毛舒展，常态暗淡静息，受触高光闪烁，冰晶海青)
class SingleHydroid(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int,
    val m: Float = Random.nextFloat() * 20f
) : AbyssalOrganism(x, y, sc, (PI / 16.0).toFloat(), 5, rgb, vx, vy, tInit, layer) {

    override fun getPointHitRadius(baseScale: Float): Float = (38f * baseScale).coerceIn(24f, 65f)

    override fun applyImpulse() {
        super.applyImpulse()
        driftVy = -abs(driftVy).coerceAtLeast(0.06f)
        surgeVy = -1.8f
        startlePulse = 1.8f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.7f

        val baseIdleAlpha = 18f + layerIndex * 5.5f
        val alpha = (baseIdleAlpha + startlePulse * 75f).coerceIn(baseIdleAlpha, 235f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 380f * currentScale
        val maxR2 = maxR * maxR
        val count = 2500
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val k = 9f * FastMath.cos(i * 5f) * FastMath.sin(i)
            val e = FastMath.cos(i * 7f) * FastMath.cos(i) * 9f
            if (e <= 0f) continue

            val mag = FastMath.hypot(k, e)
            val cosVal = FastMath.cos(t / 4f + m)
            val d = (mag * mag * mag) / 999f + 4.6f - (cosVal * cosVal * cosVal) / 3f
            val o = FastMath.sin(d * d - t + m).coerceIn(-1.2f, 1.2f)

            val safeE = max(e, 0.45f)
            val expTerm = FastMath.exp(o * 1.0986123f)
            val expE = FastMath.exp(o * FastMath.ln(safeE)).coerceIn(0f, 25f)

            val c = d / 8f - t / 32f + m
            val rx = (75f * FastMath.sin(c) + k / expTerm) * (currentScale * 1.6f)
            val ry = (75f * FastMath.cos(c / 3f) + d * 36f + expE - 180f) * (currentScale * 1.6f)

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 6 深海巨章/鱿 (最深远景 Layer 0，2.4f 绝对锐化细线，72f 明度，深海铜紫银)
class AbyssalOctopus(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 0
) : AbyssalOrganism(x, y, sc, (PI / 90.0).toFloat(), 6, rgb, vx, vy, tInit, layer) {

    override fun applyImpulse() {
        super.applyImpulse()
        driftVy = -abs(driftVy).coerceAtLeast(0.35f)
        surgeVy = -3.2f
        startlePulse = 0.5f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        validPointCount = evaluateShape(currentScale)
        if (validPointCount <= 0) return

        paint.strokeWidth = 2.4f

        val baseAlpha = 72f
        val alpha = (baseAlpha + startlePulse * 18f).coerceIn(baseAlpha, 120f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])
        canvas.drawPoints(pts, 0, validPointCount * 2, paint)
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 640f * currentScale
        val maxR2 = maxR * maxR

        for (p in 9999 downTo 0) {
            val i = p.toFloat()
            val yVal = i / 345f
            val k = (if (yVal < 11f) 6f + FastMath.sin((yVal.toInt() xor 8).toFloat()) * 6f else yVal / 5f + FastMath.cos(yVal / 2f)) * FastMath.cos(i - t / 4f)
            val e = yVal / 7f - 13f
            val d = FastMath.hypot(k, e) + FastMath.sin(e / 4f + t) / 2f
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d
            val q = yVal * (k / safeD) * (3f + FastMath.sin(d * 2f + yVal / 2f - t * 4f))
            val c = d / 2f + 1f - t / 2f

            val rx = (q + 60f * FastMath.cos(c)) * currentScale
            val ry = (q * FastMath.sin(c) + d * 29f - 170f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 生态管理器 (严格限定 5 类生物：水螅体、小水母、长水母、虫、章鱼)
class JellyfishEcology {
    private val activeOrganisms = ArrayList<AbyssalOrganism>(20)
    private var isInit = false
    private var lastTimeNanos = 0L

    private var targetPopulation = 9
    private var nextPopulationDecisionNanos = 0L
    private var nextSpawnNanos = 0L
    private var lastInteractionNanos = 0L

    private var selectedOrganism: AbyssalOrganism? = null

    private val speciesColors = arrayOf(
        intArrayOf(218, 245, 232), // 0 瘦虫：冰霜浅薄荷翠白
        intArrayOf(218, 245, 232), // 1 胖虫：冰霜浅薄荷翠白
        intArrayOf(205, 235, 255), // 2 冠状长水母：深海霜月清蓝
        intArrayOf(205, 235, 255), // 3 超长尾水母：深海霜月清蓝
        intArrayOf(252, 248, 232), // 4 经典小水母：极淡微暖象牙金白
        intArrayOf(205, 242, 248), // 5 水螅体：冰晶蛋白海青白
        intArrayOf(215, 195, 202)  // 6 深海巨章/鱿：深海冷透铜紫银
    )

    fun ensureInit(w: Float, h: Float) {
        if (isInit || w <= 0f || h <= 0f) return
        lastTimeNanos = System.nanoTime()

        targetPopulation = chooseTargetPopulation()
        nextPopulationDecisionNanos = System.nanoTime() + randomDecisionDelayNanos()

        activeOrganisms.add(instantiateOrganism(6, w * 0.50f, h * 0.28f, 0)) // 巨章 (Layer 0)
        activeOrganisms.add(instantiateOrganism(2, w * 0.78f, h * 0.48f, 1)) // 长水母 (Layer 1)
        activeOrganisms.add(instantiateOrganism(0, w * 0.24f, h * 0.62f, 2)) // 瘦虫 (Layer 2)

        activeOrganisms.add(instantiateOrganism(4, w * 0.38f, h * 0.38f, 1)) // 小水母 1
        activeOrganisms.add(instantiateOrganism(4, w * 0.72f, h * 0.75f, 3)) // 小水母 2
        activeOrganisms.add(instantiateOrganism(4, w * 0.20f, h * 0.85f, 4)) // 小水母 3

        activeOrganisms.add(instantiateOrganism(5, w * 0.62f, h * 0.68f, 0)) // 水螅体 1
        activeOrganisms.add(instantiateOrganism(5, w * 0.16f, h * 0.22f, 2)) // 水螅体 2
        activeOrganisms.add(instantiateOrganism(5, w * 0.86f, h * 0.26f, 4)) // 水螅体 3

        nextSpawnNanos = System.nanoTime() + Random.nextLong(1_000_000_000L, 2_000_000_000L)
        isInit = true
    }

    private fun instantiateOrganism(species: Int, px: Float, py: Float, explicitLayer: Int = -1): AbyssalOrganism {
        val layer = if (explicitLayer in 0..4) explicitLayer else {
            when (species) {
                6 -> 0
                2, 3 -> 1
                0, 1 -> 2
                4 -> Random.nextInt(0, 5)
                else -> Random.nextInt(0, 5)
            }
        }

        val sc = when (species) {
            6 -> Random.nextFloat() * 0.12f + 1.05f
            2, 3 -> (Random.nextFloat() * 0.10f + 0.88f) * (0.88f + layer * 0.04f)
            0, 1 -> (Random.nextFloat() * 0.10f + 0.72f) * (0.88f + layer * 0.04f)
            4 -> 0.35f + layer * 0.035f + Random.nextFloat() * 0.03f
            else -> 0.20f + layer * 0.055f + Random.nextFloat() * 0.02f
        }

        val rgb = speciesColors[species.coerceIn(0, 6)]
        val tInit = Random.nextFloat() * 100f

        val vx: Float
        val vy: Float
        when (species) {
            6 -> {
                vx = (Random.nextFloat() - 0.5f) * 0.10f
                vy = -(Random.nextFloat() * 0.14f + 0.25f)
            }
            0, 1 -> {
                vx = (Random.nextFloat() - 0.5f) * 0.04f
                vy = -(Random.nextFloat() - 0.5f) * 0.04f - 0.10f
            }
            4 -> {
                vx = (Random.nextFloat() - 0.5f) * 0.05f
                vy = -(Random.nextFloat() * 0.06f + 0.11f)
            }
            2, 3 -> {
                vx = (Random.nextFloat() - 0.5f) * 0.03f
                vy = -(Random.nextFloat() * 0.04f + 0.06f)
            }
            else -> {
                vx = (Random.nextFloat() - 0.5f) * 0.02f
                vy = -(Random.nextFloat() * 0.02f + 0.03f)
            }
        }

        return when (species) {
            0 -> SlimBug(px, py, sc, rgb, vx, vy, tInit, layer)
            1 -> PlumpBug(px, py, sc, rgb, vx, vy, tInit, layer)
            2 -> CrownLongMedusa(px, py, sc, rgb, vx, vy, tInit, layer)
            3 -> UltraLongMedusa(px, py, sc, rgb, vx, vy, tInit, layer)
            4 -> SmallJellyfish(px, py, sc, rgb, vx, vy, tInit, layer)
            5 -> SingleHydroid(px, py, sc, rgb, vx, vy, tInit, layer)
            else -> AbyssalOctopus(px, py, sc, rgb, vx, vy, tInit, layer)
        }
    }

    private fun chooseTargetPopulation(): Int {
        val r = Random.nextFloat()
        return when {
            r < 0.25f -> 8
            r < 0.75f -> 9
            else -> 10
        }
    }

    private fun randomDecisionDelayNanos(): Long {
        return 4_000_000_000L + Random.nextLong(0L, 5_000_000_000L)
    }

    private fun chooseSpawnSpecies(): Int {
        var hasOctopus = false
        var hydroidCount = 0
        var smallCount = 0
        var bugCount = 0
        var longCount = 0

        for (i in 0 until activeOrganisms.size) {
            when (activeOrganisms[i].speciesId) {
                6 -> hasOctopus = true
                5 -> hydroidCount++
                4 -> smallCount++
                0, 1 -> bugCount++
                2, 3 -> longCount++
            }
        }

        val r = Random.nextFloat()
        if (hydroidCount < 4 && r < 0.45f) return 5
        if (smallCount < 3 && r < 0.70f) return 4
        if (longCount < 1 && r < 0.82f) return if (Random.nextBoolean()) 2 else 3
        if (bugCount == 0 && r < 0.92f) return if (Random.nextBoolean()) 0 else 1
        if (!hasOctopus && r < 0.98f) return 6

        return if (Random.nextFloat() < 0.60f) 5 else 4
    }

    private fun sortOrganismsByLayer() {
        for (i in 1 until activeOrganisms.size) {
            val key = activeOrganisms[i]
            var j = i - 1
            while (j >= 0 && activeOrganisms[j].layerIndex > key.layerIndex) {
                activeOrganisms[j + 1] = activeOrganisms[j]
                j--
            }
            activeOrganisms[j + 1] = key
        }
    }

    fun onTouchEvent(e: MotionEvent, w: Float, h: Float) {
        val idx = e.actionIndex
        val tx = e.getX(idx)
        val ty = e.getY(idx)
        val baseScale = min(w, h) / 400f

        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                var bestOrg: AbyssalOrganism? = null
                var bestScore = -Float.MAX_VALUE

                for (i in activeOrganisms.size - 1 downTo 0) {
                    val org = activeOrganisms[i]
                    val d2 = org.hitTest(tx, ty, baseScale)
                    if (d2 < Float.MAX_VALUE) {
                        val score = org.layerIndex * 10000f - d2
                        if (score > bestScore) {
                            bestScore = score
                            bestOrg = org
                        }
                    }
                }

                for (i in 0 until activeOrganisms.size) {
                    val org = activeOrganisms[i]
                    if (org !== bestOrg) org.clearReaction()
                }

                selectedOrganism = bestOrg
                if (selectedOrganism != null) {
                    selectedOrganism!!.applyImpulse()
                    lastInteractionNanos = System.nanoTime()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedOrganism = null
            }
        }
    }

    fun render(canvas: Canvas, w: Float, h: Float) {
        val now = System.nanoTime()
        if (lastTimeNanos == 0L) lastTimeNanos = now
        val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.003f, 0.033f)
        lastTimeNanos = now

        val baseScale = min(w, h) / 400f
        canvas.drawColor(Color.BLACK)

        for (i in activeOrganisms.size - 1 downTo 0) {
            val org = activeOrganisms[i]
            org.update(w, h, baseScale, dt)

            if (org.isOffscreen) {
                if (selectedOrganism === org) selectedOrganism = null
                activeOrganisms.removeAt(i)
            }
        }

        sortOrganismsByLayer()

        for (i in 0 until activeOrganisms.size) {
            activeOrganisms[i].draw(canvas, baseScale)
        }

        if (now >= nextPopulationDecisionNanos) {
            targetPopulation = chooseTargetPopulation()
            nextPopulationDecisionNanos = now + Random.nextLong(4_000_000_000L, 7_000_000_000L)
        }

        val recentlyDisturbed = lastInteractionNanos > 0L && now - lastInteractionNanos < 1_200_000_000L
        if (activeOrganisms.size < targetPopulation && now >= nextSpawnNanos && !recentlyDisturbed) {
            val species = chooseSpawnSpecies()
            val spawnX = w * (0.08f + Random.nextFloat() * 0.84f)
            val spawnY = h + 100f + Random.nextFloat() * 120f
            activeOrganisms.add(instantiateOrganism(species, spawnX, spawnY))
            nextSpawnNanos = now + Random.nextLong(1_000_000_000L, 2_000_000_000L)
        }

        if (activeOrganisms.size >= targetPopulation) {
            nextSpawnNanos = maxOf(nextSpawnNanos, now + 800_000_000L)
        }
    }
}
