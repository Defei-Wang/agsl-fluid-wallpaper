package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.max
import kotlin.random.Random

class SingleHydroid(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 1,
    val m: Float = Random.nextFloat() * 19f
) : AbyssalOrganism(x, y, sc, (PI / 30.0).toFloat(), 5, rgb, vx, vy, tInit, layer) {

    override fun getPointHitRadius(baseScale: Float): Float = (75f * baseScale).coerceIn(45f, 140f)

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 2.0f).coerceAtMost(3.8f)
        startlePulse = 1.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        t += baseSpeed * motionScale * speedMultiplier * frameFactor

        val margin = 260f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 45f + layerIndex * 8.0f
        val alpha = (baseIdleAlpha + startlePulse * 45f).coerceIn(baseIdleAlpha, 225f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 600f * currentScale
        val maxR2 = maxR * maxR
        val count = 3600
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val k = 9f * FastMath.cos(i * 5f) * FastMath.sin(i)
            val e = FastMath.cos(i * 7f) * FastMath.cos(i) * 9f
            if (e <= 0f) continue

            val mag = FastMath.hypot(k, e)
            val cosT = FastMath.cos(t / 4f + m)
            val d = (mag * mag * mag) / 999f + 4.6f - (cosT * cosT * cosT) / 3f
            val o = FastMath.sin(d * d - t + m)
            val c = d / 8f - t / 32f + m

            val termK = k / FastMath.exp(o * 1.0986123f)
            val termE = FastMath.exp(o * FastMath.ln(max(e, 0.05f)))

            // 原始水螅体点阵（中心对齐）
            val rx = (99f * FastMath.sin(c) + termK) * (currentScale * 0.75f)
            val ry = (99f * FastMath.cos(c / 3f) + d * 39f + termE - 275f) * (currentScale * 0.75f)

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}
