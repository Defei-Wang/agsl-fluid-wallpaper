package com.agsl.wallpaper

// VERSION: v9 accelerate-native-motion / low idle brightness / natural respawn / stochastic population / pure black

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*
import kotlin.random.Random


// ============================================================
// 基础单体
// ============================================================

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

    var isOffscreen = false

    // 每个对象自己的受惊状态：同时控制亮度和原生运动加速。
    var startlePulse = 0f

    // 点击后只放大这个生物“自己原本的运动函数”。
    // 不额外指定一个新的逃跑方向，因此方向始终与它原来的运动一致。
    private var escapeBoost = 0f

    val pointCount = 2200

    val pts = FloatArray(pointCount * 2)
    var validPointCount = 0

    // Reject numerical branch-jumps inside one parametric organism.
    // The Processing formulas can pass through singular regions; when that
    // happens, a few points can suddenly jump hundreds of pixels away and
    // form a detached loop/ellipse in an otherwise empty area.
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

    // 基础速度比最初版本慢，但比上一版略恢复。
    // 点击后的额外速度主要作用在 x/y 平移，不加速内部公式。
    private val motionScale = 0.55f

    // --------------------------------------------------------
    // 每种单体自己的命中范围

    // 不再所有物种统一 135
    // --------------------------------------------------------

    open fun getHitRadius(baseScale: Float): Float {
        return 125f * scaleFactor * baseScale
    }

    // 真正按“画出来的点”命中，而不是只按中心点命中。
    // 这样虫 A / 长水母 2 / 章鱼的细长触须也能被直接点中，
    // 空白区域则不会因为离中心近而误触。
    open fun getPointHitRadius(baseScale: Float): Float {
        return (34f * baseScale).coerceIn(24f, 58f)
    }


    abstract fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int


    // --------------------------------------------------------
    // 独立运动
    // --------------------------------------------------------

    fun update(
        w: Float,
        h: Float,
        baseScale: Float
    ) {

        // 点击不会创造新的运动方向。
        // 它只会把这个生物原本的平移方向和原本的 t 运行同时加速。
        val speedMultiplier =
            (1f + escapeBoost).coerceAtMost(9f)

        x += driftVx * motionScale * speedMultiplier
        y += driftVy * motionScale * speedMultiplier

        // 受惊状态缓慢衰减：不点时会自然恢复成原来的慢速。
        escapeBoost *= 0.925f
        startlePulse *= 0.90f

        // 关键：点击后加快的是它自己原本的数学动画函数，
        // 因此运动方向/旋转方向与未点击时完全一致。
        t += baseSpeed * motionScale * speedMultiplier

        val margin =
            240f *
            scaleFactor *
            baseScale

        if (
            y < -margin ||
            x < -margin ||
            x > w + margin ||
            y > h + margin * 1.4f
        ) {
            isOffscreen = true
        }
    }


    // --------------------------------------------------------
    // 绘制
    // --------------------------------------------------------

    fun draw(
        canvas: Canvas,
        baseScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ) {

        val currentScale =
            baseScale * scaleFactor

        paint.strokeWidth =
            1.8f + 0.5f * scaleFactor

        // 平时非常暗，只有被点击后才明显变亮。
        // 小水母因为最小，待机时再压暗一些。
        val idleAlpha =
            if (speciesId == 2) 18f else 28f

        val alpha =
            (idleAlpha + startlePulse * 105f)
                .coerceIn(
                    idleAlpha,
                    175f
                ).toInt()

        paint.color =
            Color.argb(
                alpha,
                rgb[0],
                rgb[1],
                rgb[2]
            )

        validPointCount =
            evaluateShape(
                currentScale,
                touchX,
                touchY,
                touchDown
            )

        canvas.drawPoints(
            pts,
            0,
            validPointCount * 2,
            paint
        )
    }


    // --------------------------------------------------------
    // 精准命中
    // --------------------------------------------------------

    fun hitTest(
        tx: Float,
        ty: Float,
        baseScale: Float
    ): Float {

        val threshold = getPointHitRadius(baseScale)
        val threshold2 = threshold * threshold
        var bestD2 = Float.MAX_VALUE

        // 点阵已经是上一帧实际绘制的位置。
        // 每隔少量采样点检查一次，足够精确，同时不会影响正常渲染。
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


    // --------------------------------------------------------
    // 单纯清除反应，不影响运动状态
    // --------------------------------------------------------

    fun clearReaction() {
        startlePulse = 0f
        escapeBoost = 0f
    }


    // --------------------------------------------------------
    // 点击一次 -> 自己加速一次
    // --------------------------------------------------------

    fun applyImpulse(
        tx: Float,
        ty: Float
    ) {
        // 不根据点击位置重新计算逃跑方向。
        // 点击只增加“自己原本运动函数”的运行速度。
        // 因而它始终沿原来的平移方向/原来的 t 运行方向继续逃跑。
        escapeBoost =
            (escapeBoost + 1.15f)
                .coerceAtMost(8f)

        // 点击是唯一的“变亮”触发器。
        startlePulse =
            (startlePulse + 1.0f)
                .coerceAtMost(1.6f)
    }


    // --------------------------------------------------------
    // 只有当前 selected object 才能产生水波形变
    // --------------------------------------------------------

    protected fun putFilteredPoint(
        ptr: Int,
        px: Float,
        py: Float,
        currentScale: Float,
        maxRadiusLocal: Float,
        maxJumpLocal: Float
    ): Int {
        if (!px.isFinite() || !py.isFinite() || currentScale <= 0f) {
            return ptr
        }

        val localX = (px - x) / currentScale
        val localY = (py - y) / currentScale

        if (!localX.isFinite() || !localY.isFinite()) {
            return ptr
        }

        val radius2 = localX * localX + localY * localY
        if (!radius2.isFinite() || radius2 > maxRadiusLocal * maxRadiusLocal) {
            return ptr
        }

        // 连续参数曲线中的相邻采样点不应该突然跨越很远。
        // 一旦出现这种数值爆点，直接丢弃该点，并继续等待曲线回到
        // 上一个连续分支；因此不会产生独立的大椭圆/飞线。
        if (hasPreviousPoint) {
            val dx = localX - previousLocalX
            val dy = localY - previousLocalY
            val jump2 = dx * dx + dy * dy
            if (!jump2.isFinite() || jump2 > maxJumpLocal * maxJumpLocal) {
                return ptr
            }
        }

        if (ptr + 1 >= pts.size) {
            return ptr
        }

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
    ): Pair<Float, Float> {
        // 点击的唯一效果：变亮 + 加快平移。
        // 不对形状坐标做任何“水波/放大镜”式变形。
        return px to py
    }
}


// ============================================================
// 0 虫 A
// ============================================================

class ClassicJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) :
    AbyssalOrganism(
        x,
        y,
        sc,
        (PI / 240.0).toFloat(),
        0,
        rgb,
        vx,
        vy,
        tInit
    ) {

    override fun getHitRadius(
        baseScale: Float
    ): Float {
        return 125f * scaleFactor * baseScale
    }

    override fun getPointHitRadius(baseScale: Float): Float {
        return (42f * baseScale).coerceIn(30f, 72f)
    }

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {

        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 300f

        // 原 Processing: i = 0..10000, y = i / 295
        val step = 10000f / pointCount.toFloat()

        for (p in pointCount - 1 downTo 0) {

            val i = p * step
            val yVal = i / 295f

            val k =
                (5f + sin(yVal * 2f - t / 2f) * 2f) *
                cos(i / 29f)

            val e = yVal / 7f - 13f

            val d = hypot(k, e) - 6f

            // 原式中有 cos(y)/k；k 接近 0 时只做极小保护，
            // 防止单个采样点爆掉，不改变主体形状。
            val safeK =
                if (abs(k) < 0.20f) {
                    if (k < 0f) -0.20f else 0.20f
                } else {
                    k
                }

            val q =
                3f * sin(k * 2f) +
                cos(yVal) / safeK +
                sin(yVal / 25f) *
                k *
                (9f + 4f * sin(e * 9f - d * 3f + t * 2f))

            val c = d - t

            val rx =
                (
                    q +
                    50f * cos(c)
                ) * currentScale

            val ry =
                (
                    q * sin(c) +
                    d * 39f
                ) * currentScale

            val point =
                displaceByWater(
                    x + rx,
                    y + ry,
                    touchX,
                    touchY,
                    touchDown
                )

            ptr = putFilteredPoint(
                ptr,
                point.first,
                point.second,
                currentScale,
                maxRadiusLocal,
                72f
            )
        }

        return ptr / 2
    }
}

// ============================================================
// 1 长水母
// ============================================================

class CrownMedusa(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) :
    AbyssalOrganism(
        x,
        y,
        sc,
        (PI / 20.0).toFloat(),
        1,
        rgb,
        vx,
        vy,
        tInit
    ) {

    override fun getHitRadius(
        baseScale: Float
    ): Float {
        return 145f * scaleFactor * baseScale
    }

    override fun getPointHitRadius(baseScale: Float): Float {
        return (48f * baseScale).coerceIn(30f, 76f)
    }

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {

        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 360f
        val step = 10000f / pointCount.toFloat()

        for (p in pointCount - 1 downTo 0) {

            val i = p * step
            val yVal = i / 43f

            val k =
                5f *
                cos(i / 14f) *
                cos(yVal / 30f)

            val e = yVal / 8f - 13f

            val d =
                (k * k + e * e) / 59f + 6f

            val q =
                90f -
                5f * sin(atan2(k, e) * e) +
                k * (
                    3f +
                    sin(d * d - t * 2f)
                )

            val c = d / 2f - t / 18f

            val rx =
                q * sin(c) * currentScale

            val ry =
                (
                    q +
                    d * d *
                    sin(d * 2f - t / 3f)
                ) *
                cos(c) *
                currentScale

            val point =
                displaceByWater(
                    x + rx,
                    y + ry,
                    touchX,
                    touchY,
                    touchDown
                )

            ptr = putFilteredPoint(
                ptr,
                point.first,
                point.second,
                currentScale,
                maxRadiusLocal,
                92f
            )
        }

        return ptr / 2
    }
}

// ============================================================
// 2 小水母（黄色）
// ============================================================

class SmallJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) :
    AbyssalOrganism(
        x,
        y,
        sc,
        (PI / 45.0).toFloat(),
        2,
        rgb,
        vx,
        vy,
        tInit
    ) {

    override fun getHitRadius(baseScale: Float): Float {
        return 78f * scaleFactor * baseScale
    }

    override fun getPointHitRadius(baseScale: Float): Float {
        return (30f * baseScale).coerceIn(22f, 52f)
    }

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {

        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 215f
        val step = 10000f / pointCount.toFloat()

        for (p in pointCount - 1 downTo 0) {
            val i = p * step
            val xVal = i % 200f
            val yVal = i / 55f

            val k = 9f * cos(xVal / 8f)
            val e = yVal / 8f - 12.5f
            val mag = hypot(k, e)
            val d = (mag * mag) / 99f + sin(t) / 6f + 0.5f

            val safeD = if (abs(d) < 0.12f) {
                if (d < 0f) -0.12f else 0.12f
            } else {
                d
            }

            val q =
                99f -
                e * sin(atan2(k, e) * 7f) / safeD +
                k * (3f + cos(d * d - t) * 2f)

            val c = d / 2f + e / 69f - t / 16f

            val rx =
                (q * sin(c) + 0f) * currentScale

            val ry =
                (q + 19f * d) * cos(c) * currentScale

            val point = displaceByWater(
                x + rx,
                y + ry,
                touchX,
                touchY,
                touchDown
            )

            ptr = putFilteredPoint(
                ptr,
                point.first,
                point.second,
                currentScale,
                maxRadiusLocal,
                58f
            )
        }

        return ptr / 2
    }
}

// ============================================================
// 6 章鱼
// ============================================================

class AbyssalOctopus(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float
) :
    AbyssalOrganism(
        x,
        y,
        sc,
        (PI / 120.0).toFloat(),
        6,
        rgb,
        vx,
        vy,
        tInit
    ) {

    override fun getHitRadius(
        baseScale: Float
    ): Float {
        return 120f * scaleFactor * baseScale
    }

    override fun getPointHitRadius(baseScale: Float): Float {
        return (44f * baseScale).coerceIn(30f, 72f)
    }

    override fun evaluateShape(
        currentScale: Float,
        touchX: Float,
        touchY: Float,
        touchDown: Boolean
    ): Int {

        var ptr = 0
        resetPointContinuity()
        val maxRadiusLocal = 290f
        val step = 10000f / pointCount.toFloat()

        for (p in pointCount - 1 downTo 0) {

            val i = p * step
            val yVal = i / 345f

            val k =
                (
                    if (yVal < 11f) {
                        6f +
                        sin((yVal.toInt() xor 8).toFloat()) * 6f
                    } else {
                        yVal / 5f +
                        cos(yVal / 2f)
                    }
                ) *
                cos(i - t / 4f)

            val e = yVal / 7f - 13f

            val d =
                hypot(k, e) +
                sin(e / 4f + t) / 2f

            val safeD =
                if (abs(d) < 0.08f) {
                    if (d < 0f) -0.08f else 0.08f
                } else {
                    d
                }

            val q =
                yVal *
                (k / safeD) *
                (
                    3f +
                    sin(
                        d * 2f +
                        yVal / 2f -
                        t * 4f
                    )
                )

            val c = d / 2f + 1f - t / 2f

            val rx =
                (
                    q +
                    60f * cos(c)
                ) * currentScale

            val ry =
                (
                    q * sin(c) +
                    d * 29f -
                    170f
                ) * currentScale

            val point =
                displaceByWater(
                    x + rx,
                    y + ry,
                    touchX,
                    touchY,
                    touchDown
                )

            ptr = putFilteredPoint(
                ptr,
                point.first,
                point.second,
                currentScale,
                maxRadiusLocal,
                70f
            )
        }

        return ptr / 2
    }
}

// ============================================================
// Ecology
// ============================================================

class JellyfishEcology {

    private val activeOrganisms =
        ArrayList<AbyssalOrganism>()

    private var isInit =
        false

    private var lastTimeNanos =
        0L

    // 随机人口：目标数量缓慢变化；实际数量由自然生成和逃跑决定。
    private var targetPopulation = 1
    private var nextPopulationDecisionNanos = 0L
    private var nextSpawnNanos = 0L
    private var lastInteractionNanos = 0L


    // 当前触摸位置
    private var touchX =
        -1000f

    private var touchY =
        -1000f

    private var touchDown =
        false


    // 当前真正被选中的单体
    private var selectedOrganism:
        AbyssalOrganism? = null


    // --------------------------------------------------------
    // 只保留 4 种稳定的单体
    //
    // 0 Classic = 虫 A
    // 1 Crown   = 之前减速后的长水母
    // 6 Octopus = 章鱼
    //
    // 删除 4 / RadialAureliaMedusa：它就是容易产生
    // 大椭圆、环状异常分支的“另一个长水母”。
    // --------------------------------------------------------

    private val allowedSpecies =
        intArrayOf(
            0,
            1,
            2,
            6
        )


    // 极低饱和度，整体接近白色；小水母仍保留很弱的暖黄色倾向。
    private val speciesColors =
        arrayOf(
            intArrayOf(224, 232, 239),  // 虫 A：冷白
            intArrayOf(226, 236, 231),  // 长水母：微青白
            intArrayOf(241, 236, 207),  // 小水母：极淡暖黄白
            intArrayOf(232, 226, 239)   // 章鱼：微紫白
        )


    // --------------------------------------------------------
    // 初始化
    // --------------------------------------------------------

    fun ensureInit(
        w: Float,
        h: Float
    ) {

        if (
            isInit ||
            w <= 0f ||
            h <= 0f
        ) {
            return
        }

        lastTimeNanos =
            System.nanoTime()


        // 初始数量也随机，不再固定塞满。
        targetPopulation = chooseTargetPopulation()
        nextPopulationDecisionNanos =
            System.nanoTime() + randomDecisionDelayNanos()

        val initialCount =
            Random.nextInt(1, minOf(targetPopulation, 5) + 1)

        repeat(initialCount) {
            val species = chooseSpawnSpecies()
            val pos = generateScatteredPosition(w, h)
            activeOrganisms.add(
                instantiateOrganism(
                    species,
                    pos.first,
                    pos.second
                )
            )
        }

        nextSpawnNanos =
            System.nanoTime() + Random.nextLong(700_000_000L, 1_800_000_000L)

        isInit = true
    }


    // --------------------------------------------------------
    // 分散生成
    // --------------------------------------------------------

    private fun generateScatteredPosition(
        w: Float,
        h: Float
    ): Pair<Float, Float> {

        var bestX =
            w * 0.5f

        var bestY =
            h * 0.5f

        var maxMinDist =
            -1f


        for (
            attempt in 0 until 20
        ) {

            val candX =
                Random.nextFloat() *
                (w * 0.82f) +
                w * 0.09f

            val candY =
                Random.nextFloat() *
                (h * 0.78f) +
                h * 0.11f


            var minDist =
                Float.MAX_VALUE


            for (
                org in activeOrganisms
            ) {

                val d =
                    hypot(
                        candX - org.x,
                        candY - org.y
                    )

                if (
                    d < minDist
                ) {
                    minDist = d
                }
            }


            if (
                minDist >
                maxMinDist
            ) {

                maxMinDist =
                    minDist

                bestX =
                    candX

                bestY =
                    candY
            }


            if (
                minDist > 380f
            ) {
                break
            }
        }


        return bestX to bestY
    }


    // --------------------------------------------------------
    // 创建真正的单体
    // --------------------------------------------------------

    private fun instantiateOrganism(
        species: Int,
        px: Float,
        py: Float
    ): AbyssalOrganism {

        // 大 -> 小：章鱼 > 长水母 > 虫 A > 小水母。
        // 尺寸越小，出现概率越高；这里在数量层面也同步体现。
        val sc = when (species) {
            6 -> Random.nextFloat() * 0.16f + 1.02f   // 章鱼
            1 -> Random.nextFloat() * 0.16f + 0.88f   // 长水母
            0 -> Random.nextFloat() * 0.14f + 0.70f   // 虫 A
            else -> Random.nextFloat() * 0.14f + 0.38f // 小水母
        }

        val colorIndex = when (species) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> 3
        }


        val rgb =
            speciesColors[
                colorIndex
            ]


        val tInit =
            Random.nextFloat() *
            100f


        val vx =
            (
                Random.nextFloat() -
                0.5f
            ) * 0.12f


        val vy =
            -(
                Random.nextFloat() *
                0.35f +
                0.25f
            )


        return when (species) {

            0 ->
                ClassicJellyfish(
                    px,
                    py,
                    sc,
                    rgb,
                    vx,
                    vy,
                    tInit
                )

            1 ->
                CrownMedusa(
                    px,
                    py,
                    sc,
                    rgb,
                    vx,
                    vy,
                    tInit
                )

            2 ->
                SmallJellyfish(
                    px,
                    py,
                    sc,
                    rgb,
                    vx,
                    vy,
                    tInit
                )

            else ->
                AbyssalOctopus(
                    px,
                    py,
                    sc,
                    rgb,
                    vx,
                    vy,
                    tInit
                )
        }
    }


    private fun chooseTargetPopulation(): Int {
        val r = Random.nextFloat()

        // 大多数时间只有 2~5 只；偶尔会到 6~7，最低允许 1。
        return when {
            r < 0.16f -> 1
            r < 0.32f -> 2
            r < 0.53f -> 3
            r < 0.72f -> 4
            r < 0.86f -> 5
            r < 0.95f -> 6
            else -> 7
        }
    }


    private fun randomDecisionDelayNanos(): Long {
        return (
            3_000_000_000L +
            Random.nextLong(0L, 5_500_000_000L)
        )
    }


    private fun chooseSpawnSpecies(): Int {
        // 小型个体更常见；大型个体不会固定占位，只是更低概率进入。
        // 这样总体数量会随机，而“大 -> 小、少 -> 多”的统计关系仍然成立。
        return when (Random.nextFloat()) {
            in 0f..0.54f -> 2  // 小水母
            in 0.54f..0.82f -> 0  // 虫 A
            in 0.82f..0.96f -> 1  // 长水母
            else -> 6             // 章鱼
        }
    }

    // ========================================================
    // 核心触摸逻辑
    // ========================================================

    fun onTouchEvent(
        e: MotionEvent,
        w: Float,
        h: Float
    ) {

        val idx =
            e.actionIndex

        val tx =
            e.getX(idx)

        val ty =
            e.getY(idx)

        val baseScale =
            min(w, h) /
            400f


        when (
            e.actionMasked
        ) {

            // ------------------------------------------------
            // 新一次点击
            //
            // 无论之前是谁：
            // 先彻底解除
            // ------------------------------------------------

            MotionEvent.ACTION_DOWN -> {

                var bestOrg: AbyssalOrganism? = null
                var bestD2 = Float.MAX_VALUE

                // 只在实际绘制点附近命中，避免空白误触。
                for (org in activeOrganisms) {
                    val d2 = org.hitTest(tx, ty, baseScale)
                    if (d2 < bestD2) {
                        bestD2 = d2
                        bestOrg = org
                    }
                }

                // 点击空白：清掉所有当前反应。
                // 点击另一只：只让新目标接管，上一只立即停止增速。
                // 连续快速点击同一只：不清它自己的 pulse，速度会叠加。
                for (org in activeOrganisms) {
                    if (org !== bestOrg) {
                        org.clearReaction()
                    }
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


            // ------------------------------------------------
            // 移动
            //
            // 不重新寻找对象！
            // 只服务当前 selectedOrganism
            // ------------------------------------------------

            MotionEvent.ACTION_MOVE -> {

                if (
                    selectedOrganism != null
                ) {

                    touchX =
                        tx

                    touchY =
                        ty

                    touchDown =
                        true
                }
            }


            // ------------------------------------------------
            // 新手指按下
            //
            // 为了避免多指造成交互串线，
            // 新 pointer 不再抢控制权。
            // ------------------------------------------------

            MotionEvent.ACTION_POINTER_DOWN -> {
                // 忽略额外手指
            }


            // ------------------------------------------------
            // 抬起
            // ------------------------------------------------

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                selectedOrganism =
                    null

                touchDown =
                    false

                touchX =
                    -1000f

                touchY =
                    -1000f
            }


            MotionEvent.ACTION_POINTER_UP -> {
                // 不影响主手指
            }
        }
    }


    // ========================================================
    // Render
    // ========================================================

    fun render(
        canvas: Canvas,
        w: Float,
        h: Float
    ) {

        val now =
            System.nanoTime()


        lastTimeNanos =
            now


        val baseScale =
            min(w, h) /
            400f


        // 纯黑背景，不加任何环境光/渐变。
        canvas.drawColor(Color.BLACK)


        // ----------------------------------------------------
        // 更新 + 绘制
        // ----------------------------------------------------

        val iterator =
            activeOrganisms.iterator()


        while (
            iterator.hasNext()
        ) {

            val org =
                iterator.next()


            org.update(
                w,
                h,
                baseScale
            )


            if (
                org.isOffscreen
            ) {

                if (
                    selectedOrganism === org
                ) {

                    selectedOrganism =
                        null
                }

                iterator.remove()

            }
            else {

                // 只有当前对象能看见手指
                val isSelected =
                    selectedOrganism === org


                org.draw(
                    canvas,
                    baseScale,

                    if (isSelected)
                        touchX
                    else
                        -1000f,

                    if (isSelected)
                        touchY
                    else
                        -1000f,

                    isSelected &&
                    touchDown
                )
            }
        }


        // ----------------------------------------------------
        // 自然人口循环
        //
        // 不再维持固定的 1/2/3/6。实际数量会随着逃跑自然减少，
        // 没有点击时则从屏幕下方慢慢游进来。目标数量每隔一段时间
        // 随机改变，所以会出现“少 -> 慢慢变多 -> 又变少”的状态。
        // ----------------------------------------------------

        if (now >= nextPopulationDecisionNanos) {
            targetPopulation = chooseTargetPopulation()
            nextPopulationDecisionNanos =
                now + Random.nextLong(4_500_000_000L, 9_000_000_000L)
        }

        val recentlyDisturbed =
            lastInteractionNanos > 0L &&
            now - lastInteractionNanos < 1_500_000_000L

        if (
            activeOrganisms.size < targetPopulation &&
            now >= nextSpawnNanos &&
            !recentlyDisturbed
        ) {
            // 一次最多进来一只；点击频繁时暂停补充，让“逃跑后变少”
            // 有一个肉眼可见的窗口。
            val species = chooseSpawnSpecies()

            val spawnX =
                w * (0.10f + Random.nextFloat() * 0.80f)

            val spawnY =
                h + 90f + Random.nextFloat() * 130f

            activeOrganisms.add(
                instantiateOrganism(
                    species,
                    spawnX,
                    spawnY
                )
            )

            nextSpawnNanos =
                now + Random.nextLong(900_000_000L, 2_300_000_000L)
        }

        // 如果已经达到目标，下一次补充继续向后推，避免“刚死一只马上补一只”。
        if (activeOrganisms.size >= targetPopulation) {
            nextSpawnNanos =
                maxOf(nextSpawnNanos, now + 700_000_000L)
        }
    }
}
