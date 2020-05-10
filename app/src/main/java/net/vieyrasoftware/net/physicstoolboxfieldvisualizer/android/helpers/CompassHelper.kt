package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import kotlin.math.sqrt

/**
 * Created by inio on 2/28/18.
 */
class CompassHelper(context: Context, private val rotationHelper: DisplayRotationHelper) : SensorEventListener {
    companion object {
        const val DECAY_RATE = 0.9f
        const val SQRT_HALF = 0.70710678118f // sqrt(0.5)
        const val VALID_TRESHOLD = 0.1f
    }
    private val accumulated = FloatArray(3)
    private var deviceToWorld: Pose? = null
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java)
    fun onResume() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 200000 /* 5Hz */)
    }

    fun onUpdate(frame: Frame) {
        deviceToWorld = getDevicePose(frame).extractRotation()
        if (frame.camera.trackingState != TrackingState.TRACKING) {
            for (i in 0..2) {
                accumulated[i] = 0f
            }
        }
    }

    fun onPause() {
        sensorManager.unregisterListener(this)
    }

    fun getFieldDirection(out: FloatArray?) {
        System.arraycopy(accumulated, 0, out, 0, 3)
    }

    fun rotationValid(): Boolean {
        return accumulated[0] * accumulated[0] + accumulated[2] * accumulated[2] > VALID_TRESHOLD
    }

    /**
     * Returns the rotation about the Y axis (in radians) that results in the local X axis
     * pointing east.
     */
    fun rotateXToEastAngle(): Float {
        if (!rotationValid()) {
            return 0f
        }
        val eastX = accumulated[0]
        val eastZ = accumulated[2]
        // negative because positive rotation about Y rotates X away from Z
        return (-Math.atan2(eastZ.toDouble(), eastX.toDouble())).toFloat()
    }

    fun rotateXToEastPose(): Pose? {
        return MathHelpers.axisRotation(1, rotateXToEastAngle())
    }

    private fun getDevicePose(frame: Frame): Pose { // Cheat: Pose.makeInterpolated for rotation multiplication
        return frame.camera.displayOrientedPose.compose(
                Pose.makeInterpolated(
                        Pose.IDENTITY,
                        Pose.makeRotation(0f, 0f, SQRT_HALF, SQRT_HALF),
                        rotationHelper.rotation.toFloat()))
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
        val rotated = FloatArray(3)
        deviceToWorld!!.rotateVector(sensorEvent.values, 0, rotated, 0)
        for (i in 0..2) {
            accumulated[i] = accumulated[i] * DECAY_RATE + rotated[i]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) { // Should probably do something here...
    }

}