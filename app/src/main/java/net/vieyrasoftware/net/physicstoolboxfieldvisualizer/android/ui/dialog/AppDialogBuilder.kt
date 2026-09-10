package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.dialog

import android.content.Context
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun appDialogBuilder(context: Context): MaterialAlertDialogBuilder =
    MaterialAlertDialogBuilder(context, R.style.AppDialogTheme)

fun appDestructiveDialogBuilder(context: Context): MaterialAlertDialogBuilder =
    MaterialAlertDialogBuilder(context, R.style.AppDestructiveDialogTheme)
