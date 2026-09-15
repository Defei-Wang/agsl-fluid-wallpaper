package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

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

    override fun getPointHitRadius(baseScale: Float): Float = (60f * baseScale).coerceIn(35f, 110f)

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 2.0f).coerceAtMost(3.8f)
        startlePulse = 1.0f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.7f

        val baseIdleAlpha = 30f + layerIndex * 5.0f
        val alpha = (baseIdleAlpha + startlePulse * 50f).coerceIn(baseIdleAlpha, 225f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 360f * currentScale
        val maxR2 = maxR * maxR
        val count = 6000

        for (p in count - 1 downTo 0) {
            val i = p.toFloat()
            val k = 2f * FastMath.cos(i * 342f)
            val e = FastMath.sin(i * 271f) * 2f
            val d = FastMath.hypot(k, e) / 1.6f
            val pp = 5f + 2f * FastMath.sin(d * 8f - t * 3f + m)
            val c = (d * d / 9f) - t / 8f + m
            val safeD = if (abs(d) < 0.08f) 0.08f else d

            val rx = (k * pp + (9f / safeD) * FastMath.sin(k * 2f) + 89f * FastMath.sin(c)) * currentScale
            val ry = (79f * FastMath.sin(c * 2f) + (9f / safeD) * FastMath.sin(e * 2f) + e * pp) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}
