package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import android.app.Activity
import android.widget.Toast

/**
 * Utility class that allows to show a Toast
 */
fun Activity.showToast(msg: String) = Toast
        .makeText(this, msg, Toast.LENGTH_LONG)
        .show()

