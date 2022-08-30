package com.frankc0.kontrolmouse

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import com.frankc0.kontrolmouse.databinding.ActivityFullscreenBinding
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import com.frankc0.kontrolmouse.BluetoothConfiguration.AbsoluteMouseSender
import com.frankc0.kontrolmouse.BluetoothConfiguration.BluetoothController
import com.frankc0.kontrolmouse.ViewChanges.FullScreenActivityViewChanges
import kotlin.math.abs
import android.os.Build
import android.view.*
import com.frankc0.kontrolmouse.util.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity(), KeyEvent.Callback, SensorEventListener {

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var viewChanges: FullScreenActivityViewChanges
    private var absoluteMouseSender: AbsoluteMouseSender? = null
    private lateinit var lowPassFilter: LowPassFilter

    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrixSensor = FloatArray(9)
    private val orientationAnglesSensor = FloatArray(3)

    private val relativeOrientationTopLeft = FloatArray(2)
    private val relativeOrientationBottomRight = FloatArray(2)
    private var topLeft = false
    private var bottomRight = false
    private var center = false

    private val angleCorrection = FloatArray(2)
    private var heightScreenAngle = 0f
    private var widthScreenAngle = 0f

    private lateinit var cursor: ImageView
    private lateinit var paramsMarginIMG: RelativeLayout.LayoutParams

    private var clickType: Byte = 2

    @SuppressLint("ClickableViewAccessibility", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)

        setContentView(binding.root)
        viewChanges = FullScreenActivityViewChanges(binding)
        fullScreenMode()
        createCursorInMiniScreen()

        lowPassFilter = LowPassFilter(0.04f, 0.06f)

        setButtonsActions()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    public override fun onStart() {
        super.onStart()
        connectToBluetoothDevice()
    }
    private fun connectToBluetoothDevice() {
        BluetoothController.init(this)
        BluetoothController.getSender { hidd, device ->
            val mainHandler = Handler(applicationContext.mainLooper)
            mainHandler.post {
                binding.tvConnected.text = "Connected via bluetooth"
                absoluteMouseSender = AbsoluteMouseSender(hidd, device)
            }
        }
//      override disconnect function
        BluetoothController.getDisconnector {
            val mainHandler = Handler(applicationContext.mainLooper)
            mainHandler.post {
                binding.tvConnected.text = "App not connected via bluetooth"
                absoluteMouseSender = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(
                        event.values, 0,
                        accelerometerReading, 0, accelerometerReading.size
                    )
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(
                        event.values, 0,
                        magnetometerReading, 0, magnetometerReading.size
                    )
                }
                else -> {
                    Log.i(TAG, "NO ES")
                }
            }
            updateOrientationAnglesSensor()
        }
    }
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i(TAG, "onAccuracyChanged hola")
    }

    private fun updateOrientationAnglesSensor() {
        SensorManager.getRotationMatrix(
            rotationMatrixSensor, null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrixSensor, orientationAnglesSensor)
        viewChanges.setOrientationTV(orientationAnglesSensor)

        if (center) {
            val axisPosition =
                minMaxFilter(
                    roundIntArray(
                        lowPassFilter.lowPassFilter(
                            anglesToAxisScreen()
                        )
                    ), 0, 4095
                )

            viewChanges.setTextViewScreenPositions(axisPosition)
            viewChanges.setInScreenMousePosition(
                axisPosition,
                paramsMarginIMG,
                cursor,
                intArrayOf(800, 450),
                intArrayOf(4095, 4095)
            )

            if (clickType == AbsoluteMouseSender.CLICK && absoluteMouseSender != null) {
                absoluteMouseSender!!.sendAnsMouseReport(
                    axisPosition,
                    clickType
                )
            }
        }
    }

    private val dimensionScreen = 4095f
    private val positionScreen = FloatArray(2)

    private fun anglesToAxisScreen(): FloatArray {
        val angleX = angleCorrection(orientationAnglesSensor[0], angleCorrection[0])
        val angleY = angleCorrection(orientationAnglesSensor[1], angleCorrection[1])

        val orientAngleX = angleCorrection(relativeOrientationTopLeft[0], angleCorrection[0])
        val orientAngleY = angleCorrection(relativeOrientationTopLeft[1], angleCorrection[1])

        val difX = (angleX - orientAngleX)
        val difY = (angleY - orientAngleY)
        positionScreen[0] = proportion(difX, widthScreenAngle, dimensionScreen)
        positionScreen[1] = proportion(difY, heightScreenAngle, dimensionScreen)

        return positionScreen
    }

    private fun updateRelativeOrientationAngles(
        relativeOrientation: FloatArray,
        newOrientation: FloatArray
    ) {
        relativeOrientation[0] = newOrientation[0]
        relativeOrientation[1] = newOrientation[1]

        lowPassFilter.output[0] = newOrientation[0]
        lowPassFilter.output[1] = newOrientation[1]
        angleCorrection[0] = relativeOrientation[0]
        angleCorrection[1] = relativeOrientation[1]

        widthScreenAngle = abs(
            angleCorrection(relativeOrientationTopLeft[0], angleCorrection[0]) -
                    angleCorrection(relativeOrientationBottomRight[0], angleCorrection[0])
        )
        heightScreenAngle = abs(
            angleCorrection(relativeOrientationTopLeft[1], angleCorrection[1]) -
                    angleCorrection(relativeOrientationBottomRight[1], angleCorrection[1])
        )

    }

    companion object {
        private const val TAG = "FS Activity"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonsActions() {

        // Button top-left screen orientate
        binding.btnSetAngleTL.setOnClickListener {
            updateRelativeOrientationAngles(relativeOrientationTopLeft, orientationAnglesSensor)
            topLeft = true
            center = topLeft && bottomRight
        }
        // Button bottom-right screen orientate
        binding.btnSetAngleBR.setOnClickListener {
            updateRelativeOrientationAngles(relativeOrientationBottomRight, orientationAnglesSensor)
            bottomRight = true
            center = topLeft && bottomRight
        }

        // Button Click
        binding.buttonClick.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    clickType = AbsoluteMouseSender.CLICK
                }
                MotionEvent.ACTION_UP -> {
                    clickType = AbsoluteMouseSender.NO_CLICK_2
                    binding.switchClick.isChecked = false
                }
            }
            true
        }

        // Switch hold click
        binding.switchClick.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                clickType = AbsoluteMouseSender.CLICK
                ViewCompat.setBackgroundTintList(
                    binding.buttonClick,
                    ColorStateList.valueOf(Color.GREEN)
                )
            } else {
                clickType = AbsoluteMouseSender.NO_CLICK_2
                ViewCompat.setBackgroundTintList(
                    binding.buttonClick,
                    ColorStateList.valueOf(Color.MAGENTA)
                )
            }
        }
    }
    private fun fullScreenMode() {
        if (Build.VERSION.SDK_INT >= 30) {
            binding.root.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            binding.root.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    private fun createCursorInMiniScreen() {
        cursor = ImageView(this)
        cursor.setBackgroundColor(Color.GREEN)
        paramsMarginIMG = RelativeLayout.LayoutParams(20, 20)
        paramsMarginIMG.leftMargin = 200
        paramsMarginIMG.topMargin = 200
        binding.framePantalla.addView(cursor, paramsMarginIMG)
    }
}
