package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import com.google.ar.core.Pose

internal object MagneticFieldTransform {
    /** Sensor samples use the physical Android sensor axes, independently of screen orientation. */
    fun toWorld(sensorPose: Pose, sample: FloatArray): FloatArray {
        require(sample.size >= 3)
        // The fourth sensor value is the magnitude, not a homogeneous coordinate.
        return sensorPose.rotateVector(floatArrayOf(sample[0], sample[1], sample[2]))
    }
}
