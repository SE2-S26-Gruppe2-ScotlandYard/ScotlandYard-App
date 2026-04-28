package at.aau.serg.scotlandyard.ui.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * ShakeDetector detects device shake gestures using accelerometer readings.
 * Notifies listener when a shake event is detected.
 */
class ShakeDetector(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var shakeListener: OnShakeListener? = null
    private var lastShakeTime = 0L

    // Shake detection parameters
    private val shakeThreshold = 18f // Acceleration threshold
    private val shakeTimeDelta = 500L // Minimum time between shakes (milliseconds)

    fun interface OnShakeListener {
        fun onShake()
    }

    /**
     * Set the listener for shake events
     */
    fun setOnShakeListener(listener: OnShakeListener) {
        this.shakeListener = listener
    }

    /**
     * Start listening to accelerometer events
     */
    fun start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            Log.d("ShakeDetector", "Accelerometer listener registered")
        } else {
            Log.w("ShakeDetector", "Accelerometer not available on this device")
        }
    }

    /**
     * Stop listening to accelerometer events
     */
    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("ShakeDetector", "Accelerometer listener unregistered")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate magnitude of acceleration
            val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            // Ignore gravity (9.8 m/s²)
            val gForce = acceleration / SensorManager.GRAVITY_EARTH

            // Check if acceleration exceeds threshold
            if (gForce > shakeThreshold) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > shakeTimeDelta) {
                    lastShakeTime = now
                    shakeListener?.onShake()
                    Log.d("ShakeDetector", "Shake detected! G-force: $gForce")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No action needed
    }
}

