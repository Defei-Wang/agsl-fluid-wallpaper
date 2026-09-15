package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

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
        intArrayOf(0, 0, 0),       // 0 占位
        intArrayOf(225, 248, 236), // 1 胖虫：浅薄荷金白
        intArrayOf(205, 235, 255), // 2 经典长水母：深海霜月清蓝
        intArrayOf(212, 215, 238), // 3 超长尾水母：低饱和冷紫灰蓝
        intArrayOf(252, 248, 232), // 4 金色中等小水母：纯正象牙金白
        intArrayOf(195, 245, 235), // 5 水螅体：清透冰晶水青
        intArrayOf(205, 235, 248)  // 6 迷你小水母：极淡荧光冷蓝白
    )

    fun ensureInit(w: Float, h: Float) {
        if (isInit || w <= 0f || h <= 0f) return
        lastTimeNanos = System.nanoTime()

        targetPopulation = chooseTargetPopulation()
        nextPopulationDecisionNanos = System.nanoTime() + randomDecisionDelayNanos()

        val initialSpecies = intArrayOf(1, 2, 3, 4, 4, 5, 5, 6, 6)
        for (species in initialSpecies) {
            val (px, py) = sampleScatteredPosition(w, h, 260f)
            activeOrganisms.add(instantiateOrganism(species, px, py))
        }

        nextSpawnNanos = System.nanoTime() + Random.nextLong(1_000_000_000L, 2_000_000_000L)
        isInit = true
    }

    private fun sampleScatteredPosition(w: Float, h: Float, minDist: Float): Pair<Float, Float> {
        var bestX = w * (0.15f + Random.nextFloat() * 0.70f)
        var bestY = h * (0.12f + Random.nextFloat() * 0.76f)
        var maxObservedDist = 0f

        for (attempt in 0 until 18) {
            val candidateX = w * (0.12f + Random.nextFloat() * 0.76f)
            val candidateY = h * (0.10f + Random.nextFloat() * 0.80f)
            var closestD = Float.MAX_VALUE

            for (i in 0 until activeOrganisms.size) {
                val org = activeOrganisms[i]
                val d = hypot(candidateX - org.x, candidateY - org.y)
                if (d < closestD) closestD = d
            }

            if (closestD >= minDist) {
                return Pair(candidateX, candidateY)
            }
            if (closestD > maxObservedDist) {
                maxObservedDist = closestD
                bestX = candidateX
                bestY = candidateY
            }
        }
        return Pair(bestX, bestY)
    }

    private fun instantiateOrganism(
        species: Int,
        px: Float,
        py: Float,
        explicitLayer: Int = -1,
        customVx: Float? = null,
        customVy: Float? = null
    ): AbyssalOrganism {
        val layer = if (explicitLayer in 0..4) explicitLayer else {
            when (species) {
                2, 3 -> 1
                1 -> 2
                4, 5, 6 -> Random.nextInt(0, 5)
                else -> Random.nextInt(0, 5)
            }
        }

        val sc = when (species) {
            2, 3 -> (Random.nextFloat() * 0.08f + 0.90f) * (0.90f + layer * 0.03f)
            1 -> (Random.nextFloat() * 0.08f + 0.76f) * (0.90f + layer * 0.03f)
            4 -> 0.54f + layer * 0.040f + Random.nextFloat() * 0.03f
            5 -> 0.40f + layer * 0.040f + Random.nextFloat() * 0.03f
            else -> 0.48f + layer * 0.040f + Random.nextFloat() * 0.03f
        }

        val rgb = speciesColors[species.coerceIn(0, 6)]
        val tInit = Random.nextFloat() * 100f

        // 水平速度：中等水母恢复原生自然微飘，其余动物保持不变
        val vx: Float = customVx ?: when (species) {
            1 -> (Random.nextFloat() - 0.5f) * 0.012f
            2 ->  0f
            3 -> (Random.nextFloat() - 0.5f) * 0.015f
            4 -> (Random.nextFloat() - 0.5f) * 0.050f
            5 -> (Random.nextFloat() - 0.5f) * 0.025f
            else -> 0f
        }

        // 垂直速度：超长水母提升至 1.8 倍，中等水母恢复原版舒适游速，其余严格锁定
        val vy: Float = customVy ?: when (species) {
            1 -> -(Random.nextFloat() * 0.015f + 0.025f)
            2 ->  0f
            3 -> -(Random.nextFloat() * 0.15f + 0.80f)
            4 -> -(Random.nextFloat() * 0.05f + 0.09f)
            5 -> -(Random.nextFloat() * 0.03f + 0.07f)
            else -> 0f
        }

        return when (species) {
            1 -> PlumpBug(px, py, sc, rgb, vx, vy, tInit, layer)
            2 -> LongJellyfish(px, py, sc, rgb, vx, vy, tInit, layer)
            3 -> UltraLongJellyfish(px, py, sc, rgb, vx, vy, tInit, layer)
            4 -> SmallJellyfish(px, py, sc, rgb, vx, vy, tInit, layer)
            5 -> SingleHydroid(px, py, sc, rgb, vx, vy, tInit, layer)
            else -> TinyJellyfish(px, py, sc, rgb, vx, vy, tInit, layer)
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
        var hydroidCount = 0
        var tinyCount = 0
        var smallCount = 0
        var plumpCount = 0
        var longClassicCount = 0
        var longUltraCount = 0

        for (i in 0 until activeOrganisms.size) {
            when (activeOrganisms[i].speciesId) {
                6 -> tinyCount++
                5 -> hydroidCount++
                4 -> smallCount++
                1 -> plumpCount++
                2 -> longClassicCount++
                3 -> longUltraCount++
            }
        }

        val r = Random.nextFloat()
        if (plumpCount == 0 && r < 0.55f) return 1
        if (longClassicCount == 0 && r < 0.70f) return 2
        if (longUltraCount == 0 && r < 0.80f) return 3
        if (tinyCount < 3 && r < 0.88f) return 6
        if (hydroidCount < 3 && r < 0.94f) return 5
        if (smallCount < 3) return 4

        val pool = intArrayOf(4, 5, 6)
        return pool[Random.nextInt(pool.size)]
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
            val edgeRoll = Random.nextFloat()

            val spawnX: Float
            val spawnY: Float
            val vx: Float
            val vy: Float

            when {
                edgeRoll < 0.70f -> {
                    spawnX = w * (0.08f + Random.nextFloat() * 0.84f)
                    spawnY = h + 100f + Random.nextFloat() * 120f
                    vx = (Random.nextFloat() - 0.5f) * 0.03f
                    vy = when (species) {
                        2 -> -(Random.nextFloat() * 0.04f + 0.06f)
                        3 -> -(Random.nextFloat() * 0.08f + 0.38f)
                        else -> -(Random.nextFloat() * 0.04f + 0.08f)
                    }                }
                edgeRoll < 0.85f -> {
                    spawnX = -120f - Random.nextFloat() * 60f
                    spawnY = h * (0.30f + Random.nextFloat() * 0.60f)
                    vx = Random.nextFloat() * 0.05f + 0.03f
                    vy = when (species) {
                        2 -> -(Random.nextFloat() * 0.04f + 0.06f)
                        3 -> -(Random.nextFloat() * 0.08f + 0.38f)
                        else -> -(Random.nextFloat() * 0.04f + 0.08f)
                    }                }
                else -> {
                    spawnX = w + 120f + Random.nextFloat() * 60f
                    spawnY = h * (0.30f + Random.nextFloat() * 0.60f)
                    vx = -(Random.nextFloat() * 0.05f + 0.03f)
                    vy = when (species) {
                        2 -> -(Random.nextFloat() * 0.04f + 0.06f)
                        3 -> -(Random.nextFloat() * 0.08f + 0.38f)
                        else -> -(Random.nextFloat() * 0.04f + 0.08f)
                    }                }
            }

            activeOrganisms.add(instantiateOrganism(species, spawnX, spawnY, -1, vx, vy))
            nextSpawnNanos = now + Random.nextLong(1_000_000_000L, 2_000_000_000L)
        }

        if (activeOrganisms.size >= targetPopulation) {
            nextSpawnNanos = maxOf(nextSpawnNanos, now + 800_000_000L)
        }
    }
}



