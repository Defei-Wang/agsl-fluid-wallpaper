package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI

class PlumpBug(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 2
) : AbyssalOrganism(x, y, sc, (PI / 180.0).toFloat(), 1, rgb, vx, vy, tInit, layer) {

    override fun getPointHitRadius(baseScale: Float): Float = (95f * baseScale).coerceIn(60f, 160f)

    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 1.6f).coerceAtMost(3.2f)
        startlePulse = 1.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        // 空间宏观移动：顺沿既定微速航向平滑加速，指数阻尼渐出
        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        // 生命时钟全速运行：解绑 motionScale，激活全虫身持续金粉流光
        t += baseSpeed * (1f + escapeBoost * 0.5f) * frameFactor

        val margin = 260f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.8f

        val baseIdleAlpha = 45f + layerIndex * 8.0f
        val alpha = (baseIdleAlpha + startlePulse * 45f).coerceIn(baseIdleAlpha, 235f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        val maxR = 520f * currentScale
        val maxR2 = maxR * maxR
        val count = 8000
        val step = 20000f / count.toFloat()

        for (p in count - 1 downTo 0) {
            val i = p * step
            val yVal = i / 500f
            // 注入 t*0.2f 行波相位，消除干涉死锁
            val k = FastMath.cos(yVal * 9f + t * 0.2f) * (if (yVal < 9f) FastMath.sin(t + yVal) * 28f else 11f)
            val e = yVal / 8f - 13f
            val o = FastMath.hypot(k, e) / 6f

            // 保持原版节段包络与全虫身金粉流光
            val q = k * yVal / 15f + 79f + k * FastMath.sin(yVal) * (1f + FastMath.sin(o * 4f - e - t * 8f))
            val c = o / 2f - e / 4f - t

            val rx = (q * FastMath.sin(c) + 70f * FastMath.sin(c / 3f)) * currentScale
            val ry = ((q / 0.7f) * FastMath.cos(c)) * currentScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}
