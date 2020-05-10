package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper to ask camera permission.
 */
object PermissionHelper {
    private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    private const val CAMERA_PERMISSION_CODE = 0
    private const val STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private const val STORAGE_PERMISSION_CODE = 1
    /**
     * Check to see we have the necessary permissions for this app.
     */
    @JvmStatic
    fun hasCameraPermission(activity: Activity?): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, CAMERA_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    @JvmStatic
    fun requestCameraPermission(activity: Activity?) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(CAMERA_PERMISSION),
                CAMERA_PERMISSION_CODE)
    }

    @JvmStatic
    fun hasWriteStoragePermission(activity: Activity?): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, STORAGE_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun requestStoragePermission(activity: Activity?) {
        ActivityCompat.requestPermissions(activity!!, arrayOf(STORAGE_PERMISSION),
                STORAGE_PERMISSION_CODE)
    }

    /**
     * Check to see if we need to show the rationale for this permission.
     */
    fun shouldShowRequestPermissionRationale(activity: Activity?): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity!!, CAMERA_PERMISSION)
    }

    /**
     * Launch Application Setting to grant permission.
     */
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }
}