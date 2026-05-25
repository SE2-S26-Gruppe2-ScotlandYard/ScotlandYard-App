package at.aau.serg.scotlandyard.ui.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlin.math.sqrt

/**
 * CheatModeDetector activates cheat mode when the user shakes the device
 * while holding the volume-down button at the same time.
 *
 * The shake threshold (18 g-force) is deliberately higher than the normal
 * ShakeDetector so the combo cannot be triggered accidentally.
 *
 * Usage:
 *  1. Call [start] to register the accelerometer listener (register in onResume / DisposableEffect).
 *  2. Update [isVolumeDownHeld] from a KeyEventDispatcher in the Activity/Composable.
 *  3. Call [stop] to unregister (onPause / onDispose).
 */
class CheatModeDetector(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var cheatListener: OnCheatListener? = null
    private var lastTriggerTime = 0L

    /** Must be set from outside (e.g. a KeyEventDispatcher) while the screen is active. */
    var isVolumeDownHeld: Boolean = false

    // Same magnitude as the normal ShakeDetector – the volume-down button makes the combo unique
    val shakeThresholdG = 4.5f
    val cooldownMs = 1000L

    fun interface OnCheatListener {
        fun onCheatActivated()
    }

    fun setOnCheatListener(listener: OnCheatListener) {
        cheatListener = listener
    }

    /** Register accelerometer listener – call in lifecycle-safe location. */
    fun start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            Log.d("CheatModeDetector", "Sensor listener registered")
        } else {
            Log.w("CheatModeDetector", "Accelerometer not available")
        }
    }

    /** Unregister accelerometer listener – always call in onDispose / onPause. */
    fun stop() {
        sensorManager.unregisterListener(this)
        isVolumeDownHeld = false
        Log.d("CheatModeDetector", "Sensor listener unregistered")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (!isVolumeDownHeld) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = acceleration / SensorManager.GRAVITY_EARTH

        if (gForce > shakeThresholdG) {
            val now = System.currentTimeMillis()
            if (now - lastTriggerTime > cooldownMs) {
                lastTriggerTime = now
                Log.d("CheatModeDetector", "Cheat gesture detected! g=$gForce")
                cheatListener?.onCheatActivated()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // no-op
    }
}

