package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.io.File
import java.util.*
import kotlin.math.sqrt

class MagnetometerData : SensorEventListener {
    companion object {
        fun attachToSensors(magnetometerData: MagnetometerData, sensorManager: SensorManager) {
            val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            sensorManager.registerListener(
                    magnetometerData,
                    magneticFieldSensor,
                    SensorManager.SENSOR_DELAY_GAME
            )
        }
        fun detachFromSensors(magnetometerData: MagnetometerData, sensorManager: SensorManager) {
            magnetometerData.setState(false)
            val magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            sensorManager.unregisterListener(magnetometerData)
        }
    }

    @JvmField
    var accuracy = 0

    enum class Dimension(val idx: Int) {
        X(0), Y(1), Z(2), TOTAL(3);
    }

    enum class Mode {
        RECORDING, STOPPED;

        operator fun next(): Mode {
            return values()[(ordinal + 1) % values().size]
        }

        companion object {
            fun fromBool(b: Boolean): Mode {
                return if (b) RECORDING else STOPPED
            }
        }
    }

    @JvmField
    val mode = MutableLiveData<Mode>()
    private val history = MutableLiveData<List<ArrayList<Float>>>()
    @JvmField
    val field = MutableLiveData<FloatArray>()
    private val maxMF = MutableLiveData<Float>()
    private val minMF = MutableLiveData<Float>()
    private val averageMF = MutableLiveData<Float>()
    private val frequencyMF = MutableLiveData<Float>()

    init {
        history.value = listOf(
                ArrayList(),
                ArrayList(),
                ArrayList(),
                ArrayList()
        )
        field.value = FloatArray(4)
        mode.postValue(Mode.STOPPED)
        maxMF.postValue(0f)
        minMF.postValue(0f)
    }

    fun historical(c: Dimension): List<Float> {
        return Optional.ofNullable(history.value).map { l: List<ArrayList<Float>> -> l[c.idx] }.orElse(ArrayList())
    }

    fun historicalTotals(): List<Float> {
        return historical(Dimension.TOTAL)
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val field = FloatArray(4)
        System.arraycopy(sensorEvent.values, 0, field, 0, 3)
        field[Dimension.TOTAL.idx] = sqrt(field[0] * field[0] + field[1] * field[1] + (field[2] * field[2]).toDouble()).toFloat()
        if (mode.value == Mode.RECORDING) {
            for (dim in Dimension.values()) {
                record(dim, field[dim.idx])
            }
        }
        this.field.postValue(field)
        //    System.out.println(sensorEvent.accuracy);
        accuracy = sensorEvent.accuracy
    }

    private fun record(dim: Dimension, v: Float) {
        Transformations.map(history) { l: List<ArrayList<Float>> -> l[dim.idx].add(v) }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        var accuracy = accuracy
        when (accuracy) {
            0 -> {
                println("Unreliable")
                accuracy = 0
            }
            1 -> {
                println("Low Accuracy")
                accuracy = 1
            }
            2 -> {
                println("Medium Accuracy")
                accuracy = 2
            }
            3 -> {
                println("High Accuracy")
                accuracy = 3
            }
        }
    }

    fun toggleState() {
        mode.postValue(Optional.ofNullable(mode.value).map { obj: Mode -> obj.next() }.orElse(Mode.STOPPED))
        maxMF.postValue(Collections.max(historicalTotals()))
        minMF.postValue(Collections.min(historicalTotals()))
        val mediaDir = File(Environment.getExternalStorageDirectory().toString() + "/PhysicsToolboxFieldVisualizer/")
        // Create a folder if not exists
        if (!mediaDir.exists()) mediaDir.mkdir()
    }

    fun setState(b: Boolean) {
        mode.postValue(Mode.fromBool(b))
    }
}