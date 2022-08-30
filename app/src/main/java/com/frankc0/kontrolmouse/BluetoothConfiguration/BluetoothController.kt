package com.frankc0.kontrolmouse.BluetoothConfiguration

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log

@Suppress("MemberVisibilityCanBePrivate")
object BluetoothController : BluetoothHidDevice.Callback(), BluetoothProfile.ServiceListener {

    fun init(ctx: Context) {
        if (btHid != null)
            return
        btAdapter.getProfileProxy(ctx, this, BluetoothProfile.HID_DEVICE)
    }

    const val TAG = "BT controller"

    val btAdapter by lazy { BluetoothAdapter.getDefaultAdapter()!! }
    var btHid: BluetoothHidDevice? = null
    var hostDevice: BluetoothDevice? = null
    var mpluggedDevice: BluetoothDevice? = null
    var autoPairFlag = true

    private var deviceListener: ((BluetoothHidDevice, BluetoothDevice) -> Unit)? = null
    private var disconnectListener: (() -> Unit)? = null

    val featureReport = FeatureReport()


    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
        Log.i(TAG, "Connected to service")

        if (profile != BluetoothProfile.HID_DEVICE) {
            Log.wtf(TAG, "no acepted profile: $profile")
            return
        }
        val btHid = proxy as? BluetoothHidDevice
        if (btHid == null) {
            Log.wtf(TAG, "Proxy received but it's not BluetoothHidDevice: $proxy")
            return
        }
        BluetoothController.btHid = btHid
        btHid.registerApp(sdpRecord, null, qosOut, { it.run() }, this)//--
        btAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 300000)
//        Log.i(TAG, "Connected HID!!! "+hostDevice+" "+deviceListener)
//
//        hostDevice?.let { deviceListener?.invoke(btHid!!, it) }

    }
    override fun onServiceDisconnected(profile: Int) {
        Log.e(TAG, "Service disconnected!")
        if (profile == BluetoothProfile.HID_DEVICE)
            btHid = null
    }


    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {


        val message = when (state) {
            BluetoothProfile.STATE_CONNECTING -> "CONNECTING"
            BluetoothProfile.STATE_CONNECTED -> "CONNECTED"
            BluetoothProfile.STATE_DISCONNECTING -> "DISCONNECTING"
            BluetoothProfile.STATE_DISCONNECTED -> "DISCONNECTED"
            else -> state.toString()
        }
        Log.i(TAG, "11 Connection bluetooth state change "+message)

        super.onConnectionStateChanged(device, state)

        Log.i(TAG, "Connection bluetooth state change: $message")

        if (state == BluetoothProfile.STATE_CONNECTED) {
            if (device != null) {
                hostDevice = device
                deviceListener?.invoke(btHid!!, device)
            } else {
                Log.e(TAG, "Device not connected (state)")
            }
        } else {
            hostDevice = null
            if (state == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectListener?.invoke()
            }
        }
    }

    override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
        super.onSetReport(device, type, id, data)
        Log.i(TAG, "setreport, this $device and $type and $id and $data")
    }
    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
        Log.i(TAG, "onGetReport, getbefore, first")
        super.onGetReport(device, type, id, bufferSize)
        Log.i(TAG, "onGetReport, second")
        if (type == BluetoothHidDevice.REPORT_TYPE_FEATURE) {
            featureReport.wheelResolutionMultiplier = true
            featureReport.acPanResolutionMultiplier = true
            Log.i(TAG, "onGetReport, type BluetoothHidDevice.REPORT_TYPE_FEATURE: $btHid")

            var wasrs = btHid?.replyReport(device, type, FeatureReport.ID, featureReport.bytes)
            Log.i("replysuccess flag ", wasrs.toString())
        }
    }
    fun getSender(callback: (BluetoothHidDevice, BluetoothDevice) -> Unit) {
        btHid?.let { hidd ->
            hostDevice?.let { host ->
                callback(hidd, host)
                return
            }
        }
        deviceListener = callback
    }
    fun getDisconnector(callback: () -> Unit) {
        disconnectListener = callback
    }

    @SuppressLint("WrongConstant")
    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        super.onAppStatusChanged(pluggedDevice, registered)
        if (registered) {
            var pairedDevices = btHid?.getDevicesMatchingConnectionStates(
                intArrayOf(
                    BluetoothProfile.STATE_CONNECTING,
                    BluetoothProfile.STATE_CONNECTED,
                    BluetoothProfile.STATE_DISCONNECTED,
                    BluetoothProfile.STATE_DISCONNECTING
                )
            )
            Log.d("paired d", "paired devices are : $pairedDevices")
            Log.d("paired d", "${btHid?.getConnectionState(pairedDevices?.get(0))}")
            mpluggedDevice = pluggedDevice

            if (btHid?.getConnectionState(pluggedDevice) == 0 && pluggedDevice != null && autoPairFlag == true) {
                btHid?.connect(pluggedDevice)
            } else if (btHid?.getConnectionState(pairedDevices?.get(0)) == 0 && autoPairFlag == true) {
                Log.i(TAG, "onAppStatusChanged ??? else if")
                btHid?.connect(pairedDevices?.get(0))
            }
        }
    }



    private val sdpRecord by lazy {
        BluetoothHidDeviceAppSdpSettings(
            "Pixel HID1",
            "Mobile BController",
            "bla",
            BluetoothHidDevice.SUBCLASS1_COMBO,

//          esto
//            DescriptorCollection.MOUSE_KEYBOARD_COMBO
            DescriptorCollection.aMouse
        )
    }

    private val qosOut by lazy {
        BluetoothHidDeviceAppQosSettings(
            BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
            800,
            9,
            0,
            11250,
            BluetoothHidDeviceAppQosSettings.MAX
        )
    }
}