package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import android.view.View
import com.google.android.material.snackbar.Snackbar

object SnackbarUtility {
    /************************************ Show Snackbar with message, KeepItDisplayedOnScreen for a short period of time */
    @JvmStatic
    fun showSnackbarTypeShort(rootView: View?, mMessage: String?) {
        Snackbar.make(rootView!!, mMessage!!, Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show()
    }

    /************************************ Show Snackbar with message, KeepItDisplayedOnScreen for a long period of time */
    @JvmStatic
    fun showSnackbarTypeLong(rootView: View?, mMessage: String?) {
        Snackbar.make(rootView!!, mMessage!!, Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .show()
    }

    /************************************ Show Snackbar with message, KeepItDisplayedOnScreen */
    fun showSnackbarTypeConstant(rootView: View?, mMessage: String?) {
        Snackbar.make(rootView!!, mMessage!!, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null)
                .show()
    }
}