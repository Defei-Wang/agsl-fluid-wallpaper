package com.agsl.wallpaper

import kotlin.math.hypot

class VortexHistoryManager(private val maxVortices: Int = 16) {

    data class Vortex(
        var x: Float = 0f,
        var y: Float = 0f,
        var gamma: Float = 0f,    // 环量强度 (正负代表旋向)
        var coreR: Float = 0.18f,  // 涡核扩散半径
        var life: Float = 1.0f,
        var decay: Float = 0.045f  // 黏性动能耗散速率，持续 15\~20 秒衰减
    )

    private val vortices = ArrayList<Vortex>(maxVortices)
    private val packedBuffer = FloatArray(maxVortices * 4)

    @Synchronized
    fun injectAngularMomentum(x: Float, y: Float, dGamma: Float, coreRadius: Float = 0.18f) {
        var target: Vortex? = null
        var minDist = Float.MAX_VALUE
        val clusterDist = 90f

        for (v in vortices) {
            val d = hypot(v.x - x, v.y - y)
            if (d < minDist) {
                minDist = d
                target = v
            }
        }

        if (target != null && minDist < clusterDist) {
            target.x = target.x * 0.82f + x * 0.18f
            target.y = target.y * 0.82f + y * 0.18f
            target.gamma = (target.gamma + dGamma).coerceIn(-6.0f, 6.0f)
            target.life = 1.0f
        } else {
            if (vortices.size >= maxVortices) {
                var minLife = Float.MAX_VALUE
                var minIdx = 0
                for (i in vortices.indices) {
                    if (vortices[i].life < minLife) {
                        minLife = vortices[i].life
                        minIdx = i
                    }
                }
                vortices.removeAt(minIdx)
            }
            vortices.add(Vortex(x, y, dGamma, coreRadius, 1.0f))
        }
    }

    @Synchronized
    fun update(dt: Float) {
        val it = vortices.iterator()
        while (it.hasNext()) {
            val v = it.next()
            v.life -= v.decay * dt
            v.coreR += 0.012f * dt // 黏性扩散：涡核半径随时间演化扩大
            if (v.life <= 0.01f || kotlin.math.abs(v.gamma) < 0.03f) {
                it.remove()
            }
        }
    }

    @Synchronized
    fun getUniformData(): Pair<FloatArray, Float> {
        packedBuffer.fill(0f)
        val count = vortices.size
        for (i in 0 until count) {
            val v = vortices[i]
            val base = i * 4
            packedBuffer[base] = v.x
            packedBuffer[base + 1] = v.y
            packedBuffer[base + 2] = v.gamma * v.life
            packedBuffer[base + 3] = v.coreR
        }
        return packedBuffer to count.toFloat()
    }
}
