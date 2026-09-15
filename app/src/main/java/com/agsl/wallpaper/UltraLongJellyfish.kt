package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI
import kotlin.math.max

class UltraLongJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 1
) : AbyssalOrganism(x, y, sc, (PI / 520.0).toFloat(), 3, rgb, vx, vy, tInit, layer) {

    override fun getPointHitRadius(baseScale: Float): Float = (95f * baseScale).coerceIn(60f, 170f)

    override fun applyImpulse() {
        // 点击受刺激冲刺响应幅度保持不变，确保前进推力充足
        escapeBoost = (escapeBoost + 3.0f).coerceAtMost(5.5f)
        startlePulse = 1.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        // 空间前进位移与喷水推进冲量完全保持不变
        val pulse = FastMath.sin(t - 1.2f)
        val powerStroke = if (pulse > 0f) pulse * pulse * 0.162f else 0f

        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += (driftVy - powerStroke) * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        // 静态游动速率已减半；受触后的增益系数缩减至原来的 1/4 (0.75f / 4 = 0.1875f)
        t += baseSpeed * (1f + escapeBoost * 0.1875f) * frameFactor

        val margin = 360f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 32f + layerIndex * 5.5f
        val alpha = (baseIdleAlpha + startlePulse * 45f).coerceIn(baseIdleAlpha, 205f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 640f * currentScale
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
