package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import java.util.Locale

/**
 * Builds and launches the "e-mail the developer" intent shared by every settings screen.
 *
 * The subject carries the platform tag, app label, and version so Android and iOS support
 * mail can be told apart at a glance in the inbox; the body is pre-filled with the device
 * diagnostics needed to reproduce most sensor issues, below space for the user's message.
 */
object FeedbackEmail {

    const val SUPPORT_ADDRESS = "support@vieyrasoftware.net"

    fun send(context: Context, appLabel: String) {
        val versionName: String
        val versionCode: Long
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = info.versionName ?: "unknown"
            versionCode = PackageInfoCompat.getLongVersionCode(info)
        } catch (e: PackageManager.NameNotFoundException) {
            launch(context, subjectLine(appLabel, "unknown"), "")
            return
        }

        val subject = subjectLine(appLabel, versionName)
        val body = diagnosticsBlock(
            versionName = versionName,
            versionCode = versionCode,
            androidRelease = Build.VERSION.RELEASE ?: "unknown",
            sdkInt = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER ?: "",
            model = Build.MODEL ?: "",
            localeTag = Locale.getDefault().toLanguageTag()
        )
        launch(context, subject, body)
    }

    private fun launch(context: Context, subject: String, body: String) {
        val mailto = "mailto:$SUPPORT_ADDRESS" +
            "?subject=${Uri.encode(subject)}" +
            "&body=${Uri.encode(body)}"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(mailto)
            // Some clients read the extras instead of the mailto query.
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.no_email_app_found, SUPPORT_ADDRESS),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun subjectLine(appLabel: String, versionName: String): String =
        "[Android] $appLabel $versionName"

    fun diagnosticsBlock(
        versionName: String,
        versionCode: Long,
        androidRelease: String,
        sdkInt: Int,
        manufacturer: String,
        model: String,
        localeTag: String
    ): String {
        val device = if (model.startsWith(manufacturer, ignoreCase = true) || manufacturer.isBlank()) {
            model
        } else {
            "$manufacturer $model"
        }
        return buildString {
            appendLine()
            appendLine()
            appendLine("------------------------------")
            appendLine("App version: $versionName ($versionCode)")
            appendLine("Android: $androidRelease (SDK $sdkInt)")
            appendLine("Device: $device")
            append("Locale: $localeTag")
        }
    }
}
