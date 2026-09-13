package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*
import kotlin.random.Random

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
        const val MAX_POINTS = 2200
    }

    var isOffscreen = false
    var startlePulse = 0f
    protected var escapeBoost = 0f

    val pts = FloatArray(MAX_POINTS * 2)
    var validPointCount = 0

    private var hasPreviousPoint = false
    private var previousLocalX = 0f
    private var previousLocalY = 0f

    protected fun resetPointContinuity() {
        hasPreviousPoint = false
        previousLocalX = 0f
        previousLocalY = 0f
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    protected val motionScale = 0.52f

    open fun getPointHitRadius(baseScale: Float): Float {
        return (36f * baseScale).coerceIn(24f, 60f)
    }

    abstract fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int

    open fun update(w: Float, h: Float, baseScale: Float) {
        val speedMultiplier = (1f + escapeBoost).coerceAtMost(9f)
        x += driftVx * motionScale * speedMultiplier
        y += driftVy * motionScale * speedMultiplier

        escapeBoost *= 0.925f
        startlePulse *= 0.90f
        t += baseSpeed * motionScale * speedMultiplier

        val margin = 240f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.4f) {
            isOffscreen = true
        }
    }

    open fun draw(
        canvas: Canvas,
        baseScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ) {
        val currentScale = baseScale * scaleFactor
        
        // 4 像素粒子物理直径 (3.8f ~ 4.6f)
        paint.strokeWidth = 3.8f + 0.8f * scaleFactor

        // 待机透明度 78f（清晰通透不刺眼），触碰激发至 240f
        val idleAlpha = 78f
        val alpha = (idleAlpha + startlePulse * 115f).coerceIn(idleAlpha, 240f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale, touchX, touchY, touchDown)
        canvas.drawPoints(pts, 0, validPointCount * 2, paint)
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

    open fun applyImpulse(tx: Float, ty: Float) {
        escapeBoost = (escapeBoost + 1.25f).coerceAtMost(8.5f)
        startlePulse = (startlePulse + 1.1f).coerceAtMost(1.7f)
    }

    protected fun putFilteredPoint(
        ptr: Int,
        px: Float,
        py: Float,
        currentScale: Float,
        maxRadiusLocal: Float,
        maxJumpLocal: Float
    ): Int {
        if (!px.isFinite() || !py.isFinite() || currentScale <= 0f) return ptr

        val localX = (px - x) / currentScale
        val localY = (py - y) / currentScale
        if (!localX.isFinite() || !localY.isFinite()) return ptr

        val radius2 = localX * localX + localY * localY
        if (!radius2.isFinite() || radius2 > maxRadiusLocal * maxRadiusLocal) return ptr

        if (hasPreviousPoint) {
            val dx = localX - previousLocalX
            val dy = localY - previousLocalY
            val jump2 = dx * dx + dy * dy
            if (!jump2.isFinite() || jump2 > maxJumpLocal * maxJumpLocal) return ptr
        }

        if (ptr + 1 >= pts.size) return ptr

        pts[ptr] = px
        pts[ptr + 1] = py
        previousLocalX = localX
        previousLocalY = localY
        hasPreviousPoint = true
        return ptr + 2
    }

    protected fun displaceByWater(
        px: Float,
        py: Float,
        tx: Float,
        ty: Float,
        touchDown: Boolean
    ): Pair<Float, Float> = px to py
}

// 0 虫 A (中型 - 冰霜微薄荷冷白)
class ClassicJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 240.0).toFloat(), 0, rgb, vx, vy, tInit) {

    override fun getPointHitRadius(baseScale: Float): Float = (44f * baseScale).coerceIn(30f, 72f)

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {
        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 300f
        val count = 1800
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 295f
            val k = (5f + sin(yVal * 2f - t / 2f) * 2f) * cos(i / 29f)
            val e = yVal / 7f - 13f
            val d = hypot(k, e) - 6f
            val safeK = if (abs(k) < 0.20f) (if (k < 0f) -0.20f else 0.20f) else k

            val q = 3f * sin(k * 2f) + cos(yVal) / safeK + sin(yVal / 25f) * k * (9f + 4f * sin(e * 9f - d * 3f + t * 2f))
            val c = d - t
            val rx = (q + 50f * cos(c)) * currentScale
            val ry = (q * sin(c) + d * 39f) * currentScale

            val point = displaceByWater(x + rx, y + ry, touchX, touchY, touchDown)
            ptr = putFilteredPoint(ptr, point.first, point.second, currentScale, maxRadiusLocal, 72f)
        }
        return ptr / 2
    }
}

// 1 长水母 (次大 - 月光微青白)
class CrownMedusa(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 20.0).toFloat(), 1, rgb, vx, vy, tInit) {

    override fun getPointHitRadius(baseScale: Float): Float = (48f * baseScale).coerceIn(32f, 76f)

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {
        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 360f
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

            val point = displaceByWater(x + rx, y + ry, touchX, touchY, touchDown)
            ptr = putFilteredPoint(ptr, point.first, point.second, currentScale, maxRadiusLocal, 92f)
        }
        return ptr / 2
    }
}

// 2 真正单体小水母 (微型个体 - 纯净微暖象牙白)
// 彻底解开 4 联扎堆束缚，全屏自然均匀散落漫游
class SmallJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    val subIndex: Int = Random.nextInt(4),
    val m: Float = Random.nextFloat() * 20f
) : AbyssalOrganism(x, y, sc, (PI / 50.0).toFloat(), 2, rgb, vx, vy, tInit) {

    override fun getPointHitRadius(baseScale: Float): Float = (32f * baseScale).coerceIn(24f, 52f)

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {
        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 220f
        val count = 560 // 单体小水母只需 560 点，性能极高，全屏十多只依然满帧 120Hz
        val step = (10000f / 4f) / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = (p * step) * 4f + subIndex.toFloat()
            val k = 2f * cos(i * 342f)
            val e = sin(i * 271f) * 2f
            val d = hypot(k, e) / 1.6f
            val pp = 5f + 2f * sin(d * 8f - t * 3f + m)
            val c = (d * d / 9f) - t / 8f + m
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d

            // 保留微观水母伞盖与触须自然摆动
            val rx = (k * pp + (9f / safeD) * sin(k * 2f) + 89f * sin(c)) * currentScale
            val ry = (79f * sin(c * 2f) + (9f / safeD) * sin(e * 2f) + e * pp) * currentScale

            val point = displaceByWater(x + rx, y + ry, touchX, touchY, touchDown)
            ptr = putFilteredPoint(ptr, point.first, point.second, currentScale, maxRadiusLocal, 85f)
        }
        return ptr / 2
    }
}

// 3 章鱼 (最大巨兽 - 暮霭微幽紫白)
class AbyssalOctopus(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) : AbyssalOrganism(x, y, sc, (PI / 120.0).toFloat(), 3, rgb, vx, vy, tInit) {

    override fun getPointHitRadius(baseScale: Float): Float = (46f * baseScale).coerceIn(32f, 76f)

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {
        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 290f
        val count = 1800
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 345f
            val k = (if (yVal < 11f) 6f + sin((yVal.toInt() xor 8).toFloat()) * 6f else yVal / 5f + cos(yVal / 2f)) * cos(i - t / 4f)
            val e = yVal / 7f - 13f
            val d = hypot(k, e) + sin(e / 4f + t) / 2f
            val safeD = if (abs(d) < 0.08f) (if (d < 0f) -0.08f else 0.08f) else d
            val q = yVal * (k / safeD) * (3f + sin(d * 2f + yVal / 2f - t * 4f))
            val c = d / 2f + 1f - t / 2f

            val rx = (q + 60f * cos(c)) * currentScale
            val ry = (q * sin(c) + d * 29f - 170f) * currentScale

            val point = displaceByWater(x + rx, y + ry, touchX, touchY, touchDown)
            ptr = putFilteredPoint(ptr, point.first, point.second, currentScale, maxRadiusLocal, 70f)
        }
        return ptr / 2
    }
}

// 生态管理器：小水母群鼎盛期多达 10+ 只，全屏均匀散布
class JellyfishEcology {
    private val activeOrganisms = ArrayList<AbyssalOrganism>()
    private var isInit = false
    private var lastTimeNanos = 0L

    private var targetPopulation = 9
    private var nextPopulationDecisionNanos = 0L
    private var nextSpawnNanos = 0L
    private var lastInteractionNanos = 0L

    private var touchX = -1000f
    private var touchY = -1000f
    private var touchDown = false
    private var selectedOrganism: AbyssalOrganism? = null

    // 趋近于白色的微彩色（低饱和、高透光冷暖白光）
    private val speciesColors = arrayOf(
        intArrayOf(235, 246, 242), // 0 虫 A：冷霜微薄荷白
        intArrayOf(234, 244, 252), // 1 长水母：天青微月光白
        intArrayOf(252, 248, 236), // 2 小水母：温润暖象牙白
        intArrayOf(246, 238, 250)  // 3 章鱼：暮霭微紫幽白
    )

    fun ensureInit(w: Float, h: Float) {
        if (isInit || w <= 0f || h <= 0f) return
        lastTimeNanos = System.nanoTime()

        targetPopulation = chooseTargetPopulation()
        nextPopulationDecisionNanos = System.nanoTime() + randomDecisionDelayNanos()

        // 初始生成 7~10 只，其中包含 1 只章鱼，1 只长水母，2 只虫 A，其余均为小水母
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

    // 空间泊松排斥分布：确保小水母随机散落且分布均匀，杜绝扎堆
    private fun generateScatteredPosition(w: Float, h: Float): Pair<Float, Float> {
        var bestX = w * 0.5f
        var bestY = h * 0.5f
        var maxMinDist = -1f

        for (attempt in 0 until 24) {
            val candX = Random.nextFloat() * (w * 0.84f) + w * 0.08f
            val candY = Random.nextFloat() * (h * 0.82f) + h * 0.09f
            var minDist = Float.MAX_VALUE

            for (org in activeOrganisms) {
                val d = hypot(candX - org.x, candY - org.y)
                if (d < minDist) minDist = d
            }

            if (minDist > maxMinDist) {
                maxMinDist = minDist
                bestX = candX
                bestY = candY
            }
            if (minDist > 260f) break
        }
        return bestX to bestY
    }

    private fun instantiateOrganism(species: Int, px: Float, py: Float): AbyssalOrganism {
        val sc = when (species) {
            3 -> Random.nextFloat() * 0.18f + 1.10f // 章鱼：体型最大
            1 -> Random.nextFloat() * 0.16f + 0.88f // 长水母：次大
            0 -> Random.nextFloat() * 0.14f + 0.70f // 虫 A：中等
            else -> Random.nextFloat() * 0.08f + 0.44f // 单体小水母：最小
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

    // 鼎盛时期总人口达 12~16 只，其中小水母占绝大多数
    private fun chooseTargetPopulation(): Int {
        val r = Random.nextFloat()
        return when {
            r < 0.20f -> 8
            r < 0.50f -> 11
            r < 0.85f -> 14
            else -> 16
        }
    }

    private fun randomDecisionDelayNanos(): Long {
        return 3_000_000_000L + Random.nextLong(0L, 5_000_000_000L)
    }

    private fun chooseSpawnSpecies(): Int {
        var hasOctopus = false
        var longCount = 0
        var bugCount = 0

        for (org in activeOrganisms) {
            when (org.speciesId) {
                3 -> hasOctopus = true
                1 -> longCount++
                0 -> bugCount++
            }
        }

        val r = Random.nextFloat()

        // 缺席补偿：若全屏无章鱼且总数较多，小概率补充 1 只独体章鱼
        if (!hasOctopus && r < 0.08f) return 3
        if (longCount < 2 && r < 0.18f) return 1
        if (bugCount < 3 && r < 0.32f) return 0

        // 70% 概率持续补充单体小水母
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

                for (org in activeOrganisms) {
                    val d2 = org.hitTest(tx, ty, baseScale)
                    if (d2 < bestD2) {
                        bestD2 = d2
                        bestOrg = org
                    }
                }

                for (org in activeOrganisms) {
                    if (org !== bestOrg) org.clearReaction()
                }

                selectedOrganism = bestOrg
                touchDown = false
                touchX = -1000f
                touchY = -1000f

                if (selectedOrganism != null) {
                    selectedOrganism!!.applyImpulse(tx, ty)
                    touchDown = true
                    lastInteractionNanos = System.nanoTime()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedOrganism != null) {
                    touchX = tx
                    touchY = ty
                    touchDown = true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {}
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedOrganism = null
                touchDown = false
                touchX = -1000f
                touchY = -1000f
            }
            MotionEvent.ACTION_POINTER_UP -> {}
        }
    }

    fun render(canvas: Canvas, w: Float, h: Float) {
        val now = System.nanoTime()
        lastTimeNanos = now
        val baseScale = min(w, h) / 400f

        canvas.drawColor(Color.BLACK)

        val iterator = activeOrganisms.iterator()
        while (iterator.hasNext()) {
            val org = iterator.next()
            org.update(w, h, baseScale)

            if (org.isOffscreen) {
                if (selectedOrganism === org) selectedOrganism = null
                iterator.remove()
            } else {
                val isSelected = selectedOrganism === org
                org.draw(
                    canvas,
                    baseScale,
                    if (isSelected) touchX else -1000f,
                    if (isSelected) touchY else -1000f,
                    isSelected && touchDown
                )
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
