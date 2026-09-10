package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import com.google.ar.core.Pose
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.math.sin
import kotlin.math.cos

class MagneticFieldTransformTest {
    @Test fun physicalDeviceRotationsPreserveTheWorldMagneticField() {
        val world = floatArrayOf(18f, -37f, 42f)
        for (degrees in listOf(0, 90, 180, 270)) {
            val half = Math.toRadians(degrees / 2.0)
            val sensorPose = Pose(floatArrayOf(10f, 20f, -30f), floatArrayOf(0f, 0f, sin(half).toFloat(), cos(half).toFloat()))
            val raw = sensorPose.inverse().rotateVector(world)
            assertArrayEquals("Device rotation $degrees", world, MagneticFieldTransform.toWorld(sensorPose, raw), 0.0001f)
        }
    }

    @Test fun directionUsesSensorAxesRatherThanDisplayAxes() {
        val raw = floatArrayOf(50f, 0f, 0f, 50f)
        val sensorPose = Pose.makeRotation(0f, 0f, 0.70710677f, 0.70710677f)
        assertArrayEquals(floatArrayOf(0f, 50f, 0f), MagneticFieldTransform.toWorld(sensorPose, raw), 0.0001f)
    }

    @Test fun translationAndMagnitudeDoNotBecomeDirectionComponents() {
        assertArrayEquals(floatArrayOf(1f, 2f, 3f), MagneticFieldTransform.toWorld(Pose.makeTranslation(90f, -40f, 20f), floatArrayOf(1f, 2f, 3f, 999f)), 0.0001f)
    }
}
