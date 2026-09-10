package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class MagnetometerData : SensorEventListener {

    var tareFlag = false

    var x = 0.0f
    var y = 0.0f
    var z = 0.0f

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
    var field = MutableLiveData<FloatArray>()
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
        var field = FloatArray(4)
        System.arraycopy(sensorEvent.values, 0, field, 0, 3)

        if (tareFlag && xOffsetArray.isNotEmpty() && yOffsetArray.isNotEmpty() && zOffsetArray.isNotEmpty()) {
            field[0] = (field[0] - xOffsetArray.average()).toFloat()
            field[1] = (field[1] - yOffsetArray.average()).toFloat()
            field[2] = (field[2] - zOffsetArray.average()).toFloat()
        }


        field[Dimension.TOTAL.idx] =
            sqrt(field[0] * field[0] + field[1] * field[1] + (field[2] * field[2]).toDouble()).toFloat()

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
        history.value?.let { currentHistory ->
            currentHistory[dim.idx].add(v)
            history.postValue(currentHistory) // Update the history LiveData
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        this.accuracy = accuracy
    }

    fun toggleState() {
        mode.postValue(Optional.ofNullable(mode.value).map { obj: Mode -> obj.next() }.orElse(Mode.STOPPED))
        historicalTotals().takeIf { it.isNotEmpty() }?.let {
            maxMF.postValue(it.maxOrNull())
            minMF.postValue(it.minOrNull())
        }
    }

    fun setState(b: Boolean) {
        mode.postValue(Mode.fromBool(b))
    }

    var xOffsetArray: ArrayList<Float> = ArrayList()
    var yOffsetArray: ArrayList<Float> = ArrayList()
    var zOffsetArray: ArrayList<Float> = ArrayList()

    fun calculateAverage(marks: List<Float>): Double {
        return if (marks.isEmpty()) 0.0 else marks.average()
    }

    fun addOffsetArray (xValue: Float, arrayList: ArrayList<Float>) {
        arrayList.add(xValue)
    }
}
