package com.frankc0.kontrolmouse.util
import kotlin.math.PI

fun main() {
    var count = -3.1f
    while (count<=3.1f){
        println(count.toString() +" : "+rectificationAngle(count, -2f).toString())
        count+=0.1f
    }
}

fun rectificationAngle(a: Float, centerAngle: Float): Float {
    val newAngle = a - centerAngle
    if (newAngle < PI && newAngle > -PI){
        return newAngle
    }
    if (newAngle > PI){
        return (newAngle - 2* PI).toFloat()
    } else if (newAngle < -PI){
        return (newAngle + 2* PI).toFloat()
    }
    return newAngle
}