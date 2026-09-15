package com.agsl.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import kotlin.math.PI

class LongJellyfish(
    x: Float,
    y: Float,
    sc: Float,
    rgb: IntArray,
    vx: Float,
    vy: Float,
    tInit: Float,
    layer: Int = 1
) : AbyssalOrganism(x, y, sc, (PI / 220.0).toFloat(), 2, rgb, 0f, 0f, tInit, layer) {

    // 适配 50% 屏幕回环范围的大尺寸碰撞检测
    override fun getPointHitRadius(baseScale: Float): Float = (150f * baseScale).coerceIn(100f, 260f)

    // 受触显著加速：提升内部时钟与回环游弋速率
    override fun applyImpulse() {
        escapeBoost = (escapeBoost + 2.5f).coerceAtMost(4.5f)
        startlePulse = 1.0f
    }

    override fun update(w: Float, h: Float, baseScale: Float, dt: Float) {
        val frameFactor = dt * 60f
        val speedMultiplier = 1f + escapeBoost

        // 彻底去除人为强制向上直线平移，由原作者数学方程主导大范围回旋游弋
        val decay = (1f - 0.040f * frameFactor).coerceIn(0.90f, 0.985f)
        escapeBoost *= decay
        startlePulse *= decay

        // 生命时钟全速推进，受击时回环速度急剧加快
        t += baseSpeed * speedMultiplier * frameFactor

        // 屏幕循环锚定：始终维持在中心区域大半径环游
        if (x < w * 0.15f) x = w * 0.15f
        if (x > w * 0.85f) x = w * 0.85f
        if (y < h * 0.20f) y = h * 0.20f
        if (y > h * 0.80f) y = h * 0.80f
    }

    override fun draw(canvas: Canvas, baseScale: Float) {
        val currentScale = baseScale * scaleFactor
        paint.strokeWidth = 1.6f

        val baseIdleAlpha = 35f + layerIndex * 6.0f
        val alpha = (baseIdleAlpha + startlePulse * 45f).coerceIn(baseIdleAlpha, 210f).toInt()
        paint.color = Color.argb(alpha, rgb[0], rgb[1], rgb[2])

        validPointCount = evaluateShape(currentScale)
        if (validPointCount > 0) {
            canvas.drawPoints(pts, 0, validPointCount * 2, paint)
        }
    }

    override fun evaluateShape(currentScale: Float): Int {
        var ptr = 0
        // 放大回旋包络半径，铺满约 50% 屏幕面积
        val orbitScale = currentScale * 1.55f
        val maxR = 900f * currentScale
        val maxR2 = maxR * maxR
        val count = 10000

        for (p in count - 1 downTo 0) {
            val i = p.toFloat()
            val xVal = i % 200f
            val yVal = i / 43f
            val k = 5f * FastMath.cos(xVal / 14f) * FastMath.cos(yVal / 30f)
            val e = yVal / 8f - 13f
            val d = (k * k + e * e) / 59f + 4f

            // 原作者原生方程：t/18f 驱动宏观环形大转圈回游
            val q = 60f - 3f * FastMath.sin(FastMath.atan2(k, e) * e) + k * (3f + 4f / d * FastMath.sin(d * d - t * 2f))
            val c = d / 2f + e / 99f - t / 18f

            val rx = (q * FastMath.sin(c)) * orbitScale
            val ry = ((q + d * 9f) * FastMath.cos(c) + 65f) * orbitScale

            ptr = addPoint(ptr, x + rx, y + ry, maxR2)
        }
        return ptr / 2
    }
}
