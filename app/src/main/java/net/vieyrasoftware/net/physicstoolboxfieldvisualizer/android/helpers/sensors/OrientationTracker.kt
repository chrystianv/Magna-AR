package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class OrientationTracker: SensorEventListener {
    companion object {
        fun attachToSensors(orientationTracker: OrientationTracker, sensorManager: SensorManager) {
            val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            val gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            sensorManager.registerListener(orientationTracker, gravitySensor, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(orientationTracker, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        fun detachFromSensors(orientationTracker: OrientationTracker, sensorManager: SensorManager) {
            sensorManager.unregisterListener(orientationTracker)
        }
    }

    private val magneticField = MutableLiveData(FloatArray(3))
    private val gravity = MutableLiveData(FloatArray(3))
    private val extractedMatrices = MediatorLiveData<Pair<FloatArray, FloatArray>>()
    init {
        extractedMatrices.addSource(magneticField) { updateOrientationFromComposite() }
        extractedMatrices.addSource(gravity) { updateOrientationFromComposite() }
    }
    private val rotationMatrix = Transformations.map(extractedMatrices) { it.first }
    private val inclinationMatrix = Transformations.map(extractedMatrices) { it.second }
    // Yaw, Pitch, Roll
    val orientation = Transformations.map(rotationMatrix) {
        val orientationScratch = FloatArray(3)
        SensorManager.getOrientation(it, orientationScratch)
        // orientation contains: azimuth, pitch and roll
        orientationScratch[0] *= -1f
        for ((index, radians) in orientationScratch.withIndex()) {
            orientationScratch[index] = Math.toDegrees(radians.toDouble()).toFloat()
        }
        orientationScratch
    }
    val inclination = Transformations.map(inclinationMatrix) { SensorManager.getInclination(it) }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing, ignore it
    }

    override fun onSensorChanged(event: SensorEvent?) = when(event?.sensor?.type) {
        Sensor.TYPE_GRAVITY -> handleGravity(event)
        Sensor.TYPE_MAGNETIC_FIELD -> handleMagneticField(event)
        else -> Unit // do nothing
    }

    private fun handleGravity(event: SensorEvent) {
        gravity.postValue(event.values)
    }
    private fun handleMagneticField(event: SensorEvent) {
        magneticField.postValue(event.values)
    }
    private fun updateOrientationFromComposite() {
        val gravity = gravity.value
        val magneticField = magneticField.value
        if (gravity == null || magneticField == null) {
            return
        }
        val rotationScratch = FloatArray(9)
        val inclinationScratch = FloatArray(9)
        val success = SensorManager.getRotationMatrix(
                rotationScratch,
                inclinationScratch,
                gravity,
                magneticField
        )
        if (!success) {
            return
        }
        extractedMatrices.postValue(Pair(rotationScratch, inclinationScratch))
    }
}