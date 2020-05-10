package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import com.google.ar.core.Camera
import com.google.ar.core.TrackingFailureReason

/**
 * Gets human readable tracking failure reasons and suggested actions.
 */
object TrackingStateHelper {
    private const val INSUFFICIENT_FEATURES_MESSAGE = "Can't find anything. Aim device at a surface with more texture or color."
    private const val EXCESSIVE_MOTION_MESSAGE = "Moving too fast. Slow down."
    private const val INSUFFICIENT_LIGHT_MESSAGE = "Too dark. Try moving to a well-lit area."
    private const val BAD_STATE_MESSAGE = "Tracking lost due to bad internal state. Please try restarting the AR experience."
    @JvmStatic
    fun getTrackingFailureReasonString(camera: Camera): String {
        return when (val reason = camera.trackingFailureReason) {
            TrackingFailureReason.NONE -> "Unknown tracking failure"
            TrackingFailureReason.BAD_STATE -> BAD_STATE_MESSAGE
            TrackingFailureReason.INSUFFICIENT_LIGHT -> INSUFFICIENT_LIGHT_MESSAGE
            TrackingFailureReason.EXCESSIVE_MOTION -> EXCESSIVE_MOTION_MESSAGE
            TrackingFailureReason.INSUFFICIENT_FEATURES -> INSUFFICIENT_FEATURES_MESSAGE
            else -> "Unknown tracking failure reason: $reason"
        }
    }
}