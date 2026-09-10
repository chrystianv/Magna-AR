package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    /**
     * Returns false and displays an error message if AR rendering can not run, true if it can run
     * on this device.
     *
     * <p>AR rendering requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if AR rendering can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();

        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "AR rendering requires OpenGL ES 3.0 or later");
            Toast.makeText(activity, R.string.ar_opengl_version_required, Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
