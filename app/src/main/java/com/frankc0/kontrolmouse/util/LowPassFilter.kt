package com.frankc0.kontrolmouse.util

class LowPassFilter(
    var smoothX: Float,
    var smoothY: Float
) {
    var output = FloatArray(2)
    fun lowPassFilter(input: FloatArray): FloatArray {
        output[0] = (output[0] + smoothX * (input[0] - output[0]))
        output[1] = (output[1] + smoothY * (input[1] - output[1]))
        return (output)
    }
}