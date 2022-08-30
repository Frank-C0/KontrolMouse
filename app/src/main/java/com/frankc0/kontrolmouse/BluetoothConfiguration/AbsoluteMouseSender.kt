package com.frankc0.kontrolmouse.BluetoothConfiguration

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import java.nio.ByteBuffer

fun ByteArray.toHexString() = joinToString(" ") { "%02x".format(it) }

@Suppress("MemberVisibilityCanBePrivate")
open class AbsoluteMouseSender(
    val hidDevice: BluetoothHidDevice,
    val host: BluetoothDevice
) {
    fun sendAnsMouseReport(position: IntArray, clickType: Byte) {
        val bytesArrX = ByteArray(2) { 0 }
        val buffX: ByteBuffer = ByteBuffer.wrap(bytesArrX)
        buffX.putShort(position[0].toShort())

        val bytesArrY = ByteArray(2) { 0 }
        val buffY: ByteBuffer = ByteBuffer.wrap(bytesArrY)
        buffY.putShort(position[1].toShort())

        val report_bytes = ByteArray(5)
        report_bytes[0] = clickType
        report_bytes[1] = bytesArrX[1]
        report_bytes[2] = bytesArrX[0]
        report_bytes[3] = bytesArrY[1]
        report_bytes[4] = bytesArrY[0]

        hidDevice.sendReport(host, ID_AbsMouse, report_bytes)
    }

    companion object {
        const val TAG = "AbsoluteMouseSender"
        const val ID_AbsMouse = 10
        const val CLICK: Byte = 1
        const val NO_ClICK: Byte = 0
        const val NO_CLICK_2: Byte = 2
    }
}