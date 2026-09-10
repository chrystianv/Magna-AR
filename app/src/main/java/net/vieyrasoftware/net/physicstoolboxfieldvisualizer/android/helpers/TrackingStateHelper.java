package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers;

import com.google.ar.core.Camera;
import com.google.ar.core.TrackingFailureReason;

import android.content.Context;
import android.content.res.Resources;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R;

/** Gets human readibly tracking failure reasons and suggested actions. */
public class TrackingStateHelper {
    public static String getTrackingFailureReasonString(Context context, Camera camera) {
        Resources resources = context.getResources();
        TrackingFailureReason reason = camera.getTrackingFailureReason();
        switch (reason) {
            case NONE:
                return "";
            case BAD_STATE:
                return resources.getString(R.string.bad_state_message);
            case INSUFFICIENT_LIGHT:
                return resources.getString(R.string.insufficient_light_message);
            case EXCESSIVE_MOTION:
                return resources.getString(R.string.excessive_motion_message);
            case INSUFFICIENT_FEATURES:
                return resources.getString(R.string.insufficient_features_message);
        }
        return resources.getString(R.string.unknown_tracking_failure_reason, reason);
    }
}
