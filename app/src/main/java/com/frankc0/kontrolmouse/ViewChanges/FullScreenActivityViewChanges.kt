package com.frankc0.kontrolmouse.ViewChanges

import android.widget.ImageView
import android.widget.RelativeLayout
import com.frankc0.kontrolmouse.databinding.ActivityFullscreenBinding
import com.frankc0.kontrolmouse.util.minMaxFilter
import kotlin.math.roundToInt

class FullScreenActivityViewChanges(
    val binding: ActivityFullscreenBinding
) {
    fun setOrientationTV(angles: FloatArray){
        binding.tvAzimutal.text = angles[0].toString()
        binding.tvPitch.text = angles[1].toString()
        binding.tvRoll.text = angles[2].toString()
    }
    fun setTextViewScreenPositions(axis: IntArray){
        binding.tvScreenX.text = axis[0].toString()
        binding.tvScreenY.text = axis[1].toString()
    }
    fun setInScreenMousePosition(position: IntArray,
                                 positionImageMouse: RelativeLayout.LayoutParams,
                                 cursor: ImageView,
                                 positionMax: IntArray,
                                 sizeScreenInScreen: IntArray){
        val convertedPosition = proportionalScreen(position, sizeScreenInScreen, positionMax)

        val x = minMaxFilter(convertedPosition[0], 0,sizeScreenInScreen[0])
        val y = minMaxFilter(convertedPosition[1], 0,sizeScreenInScreen[1])

        positionImageMouse.leftMargin = x
        positionImageMouse.topMargin = y

        cursor.layoutParams = positionImageMouse

        binding.tvScreenXC.text = x.toString()
        binding.tvScreenYC.text = y.toString()
    }
    private fun proportionalScreen(input: IntArray, maxInput: IntArray, maxOut: IntArray): IntArray {
        val output = IntArray(2)
        output[0] = (input[0].toFloat() / maxInput[0].toFloat() * maxOut[0].toFloat()).roundToInt()
        output[1] = (input[1].toFloat() / maxInput[1].toFloat() * maxOut[1].toFloat()).roundToInt()
        return output
    }

}