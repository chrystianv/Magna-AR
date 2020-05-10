package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.*
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.sensors.MagnetometerData
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.sensors.OrientationTracker

class CompassViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        fun nextReading(heading: FloatArray): FloatArray {
            return floatArrayOf(0f, 0f, 0f)
        }
    }

    // Sensor combinators (Digital sensors is another way to all them)
    private val sensorManager = getApplication<Application>().getSystemService(SensorManager::class.java)!!
    private val magnetometerData = MagnetometerData()
    init {
        MagnetometerData.attachToSensors(magnetometerData, sensorManager)
    }
    private val orientationTracker = OrientationTracker()
    init {
        OrientationTracker.attachToSensors(orientationTracker, sensorManager)
    }

    override fun onCleared() {
        MagnetometerData.detachFromSensors(magnetometerData, sensorManager)
        OrientationTracker.detachFromSensors(orientationTracker, sensorManager)
        super.onCleared()
    }

    fun getOrientationLiveData(): LiveData<FloatArray> = orientationTracker.orientation
    fun getMagneticFieldLiveData(): LiveData<FloatArray> = magnetometerData.field
    fun getInclinationLiveData(): LiveData<Float> = orientationTracker.inclination

    // TODO: Implement the ViewModel
}