package com.frankc0.kontrolmouse.util

import kotlin.math.PI
import kotlin.math.roundToInt

fun angleCorrection(a: Float, centerAngle: Float): Float {
    val newAngle = a - centerAngle
    if (newAngle > PI) {
        return (newAngle - 2 * PI).toFloat()
    } else if (newAngle < -PI) {
        return (newAngle + 2 * PI).toFloat()
    }
    return newAngle
}

fun minMaxFilter(n: Int, min: Int, max: Int): Int {
    if (n > max) return max
    else if (n < min) return min
    return n
}

fun minMaxFilter(n: IntArray, min: Int, max: Int): IntArray {
    val filtered = IntArray(2)
    filtered[0] = minMaxFilter(n[0], min, max)
    filtered[1] = minMaxFilter(n[1], min, max)
    return filtered
}

fun roundIntArray(array: FloatArray): IntArray {
    return intArrayOf(
        array[0].roundToInt(),
        array[1].roundToInt()
    )
}

fun proportion(input: Float, maxInput: Float, maxOut: Float): Float {
    return (input / maxInput * maxOut)
}