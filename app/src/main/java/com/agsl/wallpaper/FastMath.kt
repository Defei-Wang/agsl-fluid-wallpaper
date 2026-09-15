package com.agsl.wallpaper

import kotlin.math.PI
import kotlin.math.sqrt

object FastMath {
    const val TABLE_SIZE = 8192
    const val MASK = TABLE_SIZE - 1
    const val RAD_TO_INDEX = 1303.7972938f
    
    val SIN_TABLE = FloatArray(TABLE_SIZE) { i ->
        kotlin.math.sin((i + 0.5f) / TABLE_SIZE * 2f * PI.toFloat())
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sin(rad: Float): Float {
        val idx = (rad * RAD_TO_INDEX).toInt() and MASK
        return SIN_TABLE[idx]
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun cos(rad: Float): Float {
        val idx = ((rad * RAD_TO_INDEX).toInt() + 2048) and MASK
        return SIN_TABLE[idx]
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun hypot(x: Float, y: Float): Float = sqrt(x * x + y * y)

    @Suppress("NOTHING_TO_INLINE")
    inline fun atan2(y: Float, x: Float): Float = kotlin.math.atan2(y.toDouble(), x.toDouble()).toFloat()

    @Suppress("NOTHING_TO_INLINE")
    inline fun exp(v: Float): Float = kotlin.math.exp(v)

    @Suppress("NOTHING_TO_INLINE")
    inline fun ln(v: Float): Float = kotlin.math.ln(v)
}
