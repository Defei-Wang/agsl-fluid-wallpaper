package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*
import kotlin.random.Random

// 零 GC、高频内联数学辅助函数
@Suppress("NOTHING_TO_INLINE")
private inline fun fastHypot(x: Float, y: Float): Float = sqrt(x * x + y * y)

abstract class AbyssalOrganism(
    var x: Float,
    var y: Float,
    val scaleFactor: Float,
    val baseSpeed: Float,
    val speciesId: Int,
    val rgb: IntArray,
    var driftVx: Float,
    var driftVy: Float,
    var t: Float
) {
    companion object {
        const val MAX_POINTS = 2400
    }

    var isOffscreen = false
    var startlePulse = 0f
    protected var escapeBoost = 0f

    val pts = FloatArray(MAX_POINTS * 2)
    var validPointCount = 0

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    protected val motionScale = 0.52f

    open fun getPointHitRadius(baseScale: Float): Float = (38f * baseScale).coerceIn(24f, 65f)

    abstract fun evaluateShape(currentScale: Float): Int

    open fun update(w: Float, h: Float, baseScale: Float) {
        val speedMultiplier = (1f + escapeBoost).coerceAtMost(8.5f)
        x += driftVx * motionScale * speedMultiplier
        y += driftVy * motionScale * speedMultiplier

        escapeBoost *= 0.925f
        startlePulse *= 0.90f
        t += baseSpeed * motionScale * speedMultiplier

        val margin = 180f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    open fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor

        paint.strokeWidth = 3.8f + 0.8f * scaleFactor
        val idleAlpha = 78f
        val alpha = (idleAlpha + startlePulse * 115f).coerceIn(idleAlpha, 240f).toInt()
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
            i += 2
        }
        return bestD2
    }

    open fun clearReaction() {
        startlePulse = 0f
        escapeBoost = 0f
    }

    open fun applyImpulse() {
        escapeBoost = (escapeBoost + 1.25f).coerceAtMost(8.5f)
        startlePulse = (startlePulse + 1.1f).coerceAtMost(1.7f)
    }

    // 零分配就地写入，绝不跳点，保留完整连续触手
    @Suppress("NOTHING_TO_INLINE")
    protected inline fun addPoint(ptr: Int, px: Float, py: Float, maxR2: Float): Int {
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

// 0 虫 A
class ClassicJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 240.0).toFloat(), 0, rgb, vx, vy, tInit) {

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 340f * currentScale
        val maxR2 = maxR * maxR
        val count = 1800
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 295f
            val k = (5f + sin(yVal * 2f - t / 2f) * 2f) * cos(i / 29f)
            val e = yVal / 7f - 13f
            val d = fastHypot(k, e) - 6f
            val safeK = if (abs(k) < 0.20f) (if (k < 0f) -0.20f else 0.20f) else k

            val q = 3f * sin(k * 2f) + cos(yVal) / safeK + sin(yVal / 25f) * k * (9f + 4f * sin(e * 9f - d * 3f + t * 2f))
            val c = d - t
            val rx = (q + 50f * cos(c)) * currentScale
            val ry = (q * sin(c) + d * 39f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 1 长水母
class CrownMedusa(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 20.0).toFloat(), 1, rgb, vx, vy, tInit) {

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 400f * currentScale
        val maxR2 = maxR * maxR
        val count = 1800
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 43f
            val k = 5f * cos(i / 14f) * cos(yVal / 30f)
            val e = yVal / 8f - 13f
            val d = (k * k + e * e) / 59f + 6f
            val q = 90f - 5f * sin(atan2(k, e) * e) + k * (3f + sin(d * d - t * 2f))
            val c = d / 2f - t / 18f
            val rx = q * sin(c) * currentScale
            val ry = (q + d * d * sin(d * 2f - t / 3f)) * cos(c) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 2 单体小水母 (完整正弦展开曲面，触手与伞盖饱满且不扎堆)
class SmallJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    val m: Float = Random.nextFloat() * 20f
) : AbyssalOrganism(x, y, sc, (PI / 50.0).toFloat(), 2, rgb, vx, vy, tInit) {

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 240f * currentScale
        val maxR2 = maxR * maxR
        val count = 650
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val k = 2f * cos(i * 342f)
            val e = sin(i * 271f) * 2f
            val d = fastHypot(k, e) / 1.6f
            val pp = 5f + 2f * sin(d * 8f - t * 3f + m)
            val c = (d * d / 9f) - t / 8f + m
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d

            val rx = (k * pp + (9f / safeD) * sin(k * 2f) + 89f * sin(c)) * currentScale
            val ry = (79f * sin(c * 2f) + (9f / safeD) * sin(e * 2f) + e * pp) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 3 章鱼
class AbyssalOctopus(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 120.0).toFloat(), 3, rgb, vx, vy, tInit) {

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 340f * currentScale
        val maxR2 = maxR * maxR
        val count = 1800
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 345f
            val k = (if (yVal < 11f) 6f + sin((yVal.toInt() xor 8).toFloat()) * 6f else yVal / 5f + cos(yVal / 2f)) * cos(i - t / 4f)
            val e = yVal / 7f - 13f
            val d = fastHypot(k, e) + sin(e / 4f + t) / 2f
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d
            val q = yVal * (k / safeD) * (3f + sin(d * 2f + yVal / 2f - t * 4f))
            val c = d / 2f + 1f - t / 2f

            val rx = (q + 60f * cos(c)) * currentScale
            val ry = (q * sin(c) + d * 29f - 170f) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}

// 生态管理器 (零 Iterator 分配，满帧 120Hz 循环)
class JellyfishEcology {
    private val activeOrganisms = ArrayList<AbyssalOrganism>(20)
    private var isInit = false
    private var lastTimeNanos = 0L

    private var targetPopulation = 10
    private var nextPopulationDecisionNanos = 0L
    private var nextSpawnNanos = 0L
    private var lastInteractionNanos = 0L

    private var selectedOrganism: AbyssalOrganism? = null

    private val speciesColors = arrayOf(
        intArrayOf(235, 246, 242), // 0 虫 A
        intArrayOf(234, 244, 252), // 1 长水母
        intArrayOf(252, 248, 236), // 2 小水母
        intArrayOf(246, 238, 250)  // 3 章鱼
    )

    fun ensureInit(w: Float, h: Float) {
        if (isInit || w <= 0f || h <= 0f) return
        lastTimeNanos = System.nanoTime()

        targetPopulation = chooseTargetPopulation()
        nextPopulationDecisionNanos = System.nanoTime() + randomDecisionDelayNanos()

        activeOrganisms.add(instantiateOrganism(3, w * 0.5f, h * 0.35f))
        activeOrganisms.add(instantiateOrganism(1, w * 0.75f, h * 0.6f))
        activeOrganisms.add(instantiateOrganism(0, w * 0.25f, h * 0.7f))

        repeat(5) {
            val pos = generateScatteredPosition(w, h)
            activeOrganisms.add(instantiateOrganism(2, pos.first, pos.second))
        }

        nextSpawnNanos = System.nanoTime() + Random.nextLong(400_000_000L, 1_000_000_000L)
        isInit = true
    }

    private fun generateScatteredPosition(w: Float, h: Float): Pair<Float, Float> {
        var bestX = w * 0.5f
        var bestY = h * 0.5f
        var maxMinDist = -1f

        for (attempt in 0 until 20) {
            val candX = Random.nextFloat() * (w * 0.84f) + w * 0.08f
            val candY = Random.nextFloat() * (h * 0.82f) + h * 0.09f
            var minDist = Float.MAX_VALUE

            for (i in 0 until activeOrganisms.size) {
                val org = activeOrganisms[i]
                val d = fastHypot(candX - org.x, candY - org.y)
                if (d < minDist) minDist = d
            }

            if (minDist > maxMinDist) {
                maxMinDist = minDist
                bestX = candX
                bestY = candY
            }
            if (minDist > 250f) break
        }
        return bestX to bestY
    }

    private fun instantiateOrganism(species: Int, px: Float, py: Float): AbyssalOrganism {
        val sc = when (species) {
            3 -> Random.nextFloat() * 0.18f + 1.10f
            1 -> Random.nextFloat() * 0.16f + 0.88f
            0 -> Random.nextFloat() * 0.14f + 0.70f
            else -> Random.nextFloat() * 0.08f + 0.44f
        }

        val rgb = speciesColors[species.coerceIn(0, 3)]
        val tInit = Random.nextFloat() * 100f
        val vx = (Random.nextFloat() - 0.5f) * 0.14f
        val vy = -(Random.nextFloat() * 0.35f + 0.25f)

        return when (species) {
            0 -> ClassicJellyfish(px, py, sc, rgb, vx, vy, tInit)
            1 -> CrownMedusa(px, py, sc, rgb, vx, vy, tInit)
            2 -> SmallJellyfish(px, py, sc, rgb, vx, vy, tInit)
            else -> AbyssalOctopus(px, py, sc, rgb, vx, vy, tInit)
        }
    }

    private fun chooseTargetPopulation(): Int {
        val r = Random.nextFloat()
        return when {
            r < 0.20f -> 8
            r < 0.50f -> 11
            r < 0.85f -> 13
            else -> 15
        }
    }

    private fun randomDecisionDelayNanos(): Long {
        return 3_000_000_000L + Random.nextLong(0L, 5_000_000_000L)
    }

    private fun chooseSpawnSpecies(): Int {
        var hasOctopus = false
        var longCount = 0
        var bugCount = 0

        for (i in 0 until activeOrganisms.size) {
            when (activeOrganisms[i].speciesId) {
                3 -> hasOctopus = true
                1 -> longCount++
                0 -> bugCount++
            }
        }

        val r = Random.nextFloat()
        if (!hasOctopus && r < 0.08f) return 3
        if (longCount < 2 && r < 0.18f) return 1
        if (bugCount < 3 && r < 0.32f) return 0
        return 2
    }

    fun onTouchEvent(e: MotionEvent, w: Float, h: Float) {
        val idx = e.actionIndex
        val tx = e.getX(idx)
        val ty = e.getY(idx)
        val baseScale = min(w, h) / 400f

        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                var bestOrg: AbyssalOrganism? = null
                var bestD2 = Float.MAX_VALUE

                for (i in 0 until activeOrganisms.size) {
                    val org = activeOrganisms[i]
                    val d2 = org.hitTest(tx, ty, baseScale)
                    if (d2 < bestD2) {
                        bestD2 = d2
                        bestOrg = org
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
        lastTimeNanos = now
        val baseScale = min(w, h) / 400f

        canvas.drawColor(Color.BLACK)

        // 下标反向遍历，零 GC 且支持安全即时移除越界个体
        for (i in activeOrganisms.size - 1 downTo 0) {
            val org = activeOrganisms[i]
            org.update(w, h, baseScale)

            if (org.isOffscreen) {
                if (selectedOrganism === org) selectedOrganism = null
                activeOrganisms.removeAt(i)
            } else {
                org.draw(canvas, baseScale)
            }
        }

        if (now >= nextPopulationDecisionNanos) {
            targetPopulation = chooseTargetPopulation()
            nextPopulationDecisionNanos = now + Random.nextLong(3_500_000_000L, 7_000_000_000L)
        }

        val recentlyDisturbed = lastInteractionNanos > 0L && now - lastInteractionNanos < 1_200_000_000L
        if (activeOrganisms.size < targetPopulation && now >= nextSpawnNanos && !recentlyDisturbed) {
            val species = chooseSpawnSpecies()
            val spawnX = w * (0.08f + Random.nextFloat() * 0.84f)
            val spawnY = h + 80f + Random.nextFloat() * 120f
            activeOrganisms.add(instantiateOrganism(species, spawnX, spawnY))
            nextSpawnNanos = now + Random.nextLong(600_000_000L, 1_400_000_000L)
        }

        if (activeOrganisms.size >= targetPopulation) {
            nextSpawnNanos = maxOf(nextSpawnNanos, now + 500_000_000L)
        }
    }
}
