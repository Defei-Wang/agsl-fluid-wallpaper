package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

abstract class AbyssalOrganism(
    var x: Float,
    var y: Float,
    var scaleFactor: Float,
    val baseSpeed: Float,
    val speciesId: Int,
    val rgb: IntArray,
    var driftVx: Float,
    var driftVy: Float,
    var t: Float,
    var layerIndex: Int = 0
) {
    val pts = FloatArray(24000)
    var validPointCount = 0
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    var isOffscreen = false
    var escapeBoost = 0f
    var startlePulse = 0f

    val motionScale = 0.28f

    open fun getPointHitRadius(baseScale: Float): Float = (70f * baseScale).coerceIn(40f, 150f)

    open fun hitTest(tx: Float, ty: Float, baseScale: Float): Float {
        val dx = tx - x
        val dy = ty - y
        val r = getPointHitRadius(baseScale)
        val d2 = dx * dx + dy * dy
        return if (d2 <= r * r) d2 else Float.MAX_VALUE
    }

    // 核心重构：点击不产生生造的位移矢量，只平滑加速原有的运动速率
    open fun applyImpulse() {
        escapeBoost = (escapeBoost + 1.8f).coerceAtMost(3.5f)
        startlePulse = 1.0f
    }

    open fun clearReaction() {}

    // 物理渐出 (Ease-Out) 运动学模型
    open fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        // 严格顺沿固有运动航向加速，平滑渐出衰减
        x += driftVx * motionScale * speedMultiplier * frameFactor
        y += driftVy * motionScale * speedMultiplier * frameFactor

        // 指数级自然阻尼衰减（Ease-Out）
        val decay = (1f - 0.045f * frameFactor).coerceIn(0.90f, 0.98f)
        escapeBoost *= decay
        startlePulse *= decay

        t += baseSpeed * (1f + escapeBoost * 0.5f) * frameFactor

        val margin = 320f * scaleFactor * baseScale
        if (y < -margin || x < -margin || x > w + margin || y > h + margin * 1.5f) {
            isOffscreen = true
        }
    }

    abstract fun evaluateShape(currentScale: Float): Int

    open fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 35f + layerIndex * 6.0f
        val alpha = (baseIdleAlpha + startlePulse * 50f).coerceIn(baseIdleAlpha, 220f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    protected inline fun addPoint(ptr: Int, px: Float, py: Float, maxR2: Float): Int {
        val dx = px - x
        val dy = py - y
        if (dx * dx + dy * dy < maxR2 && ptr + 1 < pts.size) {
            pts[ptr] = px
            pts[ptr + 1] = py
            return ptr + 2
        }
        return ptr
    }
}
