package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import androidx.lifecycle.ViewModel

class VisualizerActivityViewModel: ViewModel() {

    var xOffset = 0.0f
    var yOffset = 0.0f
    var zOffset = 0.0f

    var x = 0.0f
    var y = 0.0f
    var z = 0.0f

    var xOffsetArray: ArrayList<Float> = ArrayList()

    fun addXOffsetArray (xValue: Float) {
        xOffsetArray.add(xValue)
    }

    private fun calculateAverage(marks: List<Float>): Double {
        var sum: Long = 0
        for (mark in marks) {
            sum += mark.toLong()
        }
        return if (marks.isEmpty()) 0.0 else 1.0 * sum / marks.size
    }
}
