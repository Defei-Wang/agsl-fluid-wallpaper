package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.max
import kotlin.random.Random

class TinyJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int,
    val m: Float = Random.nextFloat() * 20f
) : AbyssalOrganism(x, y, sc, (PI / 20.0).toFloat(), 6, rgb, vx, vy, tInit, layer) {

    override fun getPointHitRadius(baseScale: Float): Float = (90f * baseScale).coerceIn(60f, 160f)

    private fun getInstantVelocity(): Pair<Float, Float> {
        val cMid = 0.11f - t / 48f + m
        val swimVx = -2.06f * FastMath.cos(cMid)
        val swimVy = -8.25f * FastMath.cos(cMid * 4f)
        return Pair(swimVx, swimVy)
    }

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 2.0f).coerceAtMost(3.8f)
        startlePulse = 1.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        val (swimVx, swimVy) = getInstantVelocity()
        val speed = FastMath.hypot(swimVx, swimVy)
        val dirX = if (speed > 0.001f) swimVx / speed else 0f
        val dirY = if (speed > 0.001f) swimVy / speed else -1f

        val cruise = 0.10f * motionScale * speedMultiplier
        x += dirX * cruise * frameFactor
        y += dirY * cruise * frameFactor

        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        t += baseSpeed * motionScale * speedMultiplier * frameFactor

        val safeMargin = 400f * baseScale
        if (y < -safeMargin) y = h + safeMargin * 0.5f
        if (y > h + safeMargin) y = -safeMargin * 0.5f
        if (x < -safeMargin) x = w + safeMargin * 0.5f
        if (x > w + safeMargin) x = -safeMargin * 0.5f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 25f + layerIndex * 5.0f
        val alpha = (baseIdleAlpha + startlePulse * 45f).coerceIn(baseIdleAlpha, 195f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 900f * currentScale
        val maxR2 = maxR * maxR
        val count = 3000
        val step = 10000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val k = 9f * FastMath.cos(i * 5f) * FastMath.sin(i)
            val e = FastMath.cos(i * 3f) * FastMath.cos(i * 2f) * 9f
            val mag = FastMath.hypot(k, e)

            val sinT = FastMath.sin(t / 2f + m)
            val d = (mag * mag * mag) / 1999f + 1.5f - (sinT * sinT * sinT) / 3f

            val sinTerm = FastMath.sin(d * d - t + m)
            val pVal = FastMath.exp(sinTerm * FastMath.ln(max(d, 0.05f)))
            val c = d / 16f - t / 48f + m

            // 彻底去除 0.45f 的人工缩小，恢复全尺寸
            val rx = (99f * FastMath.sin(c) + k * pVal) * currentScale
            val ry = (99f * FastMath.sin(c * 4f) + e * pVal) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}
