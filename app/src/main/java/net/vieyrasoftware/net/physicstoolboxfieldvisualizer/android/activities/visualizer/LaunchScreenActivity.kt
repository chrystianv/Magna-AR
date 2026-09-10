package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.ArCoreApk
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.theme.SettingsTheme

/** Standalone entry point: only enter the renderer after its hardware prerequisites are met. */
class LaunchScreenActivity : ComponentActivity() {
    private var message by mutableIntStateOf(R.string.launch_welcome)
    private var denied by mutableStateOf(false)
    private var checking by mutableStateOf(false)
    private var installRequested = false
    private var checkJob: Job? = null
    private val cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) checkSupport() else message = R.string.launch_camera_required
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installRequested = savedInstanceState?.getBoolean("installRequested") ?: false
        denied = savedInstanceState?.getBoolean("denied") ?: false
        setContent {
            SettingsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Magna-AR", style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(20.dp))
                        Text(stringResource(message), style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(24.dp))
                        Button(enabled = !checking, onClick = {
                            if (denied && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
                            } else if (!hasCameraPermission()) cameraPermission.launch(Manifest.permission.CAMERA)
                            else checkSupport()
                        }) { Text(stringResource(if (denied) R.string.launch_allow_camera else R.string.launch_start)) }
                        TextButton(onClick = {
                            startActivity(Intent(this@LaunchScreenActivity, net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings.SettingsActivity::class.java))
                        }) { Text(stringResource(R.string.settings)) }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission()) checkSupport()
    }

    override fun onPause() {
        checkJob?.cancel()
        checking = false
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("installRequested", installRequested)
        outState.putBoolean("denied", denied)
        super.onSaveInstanceState(outState)
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun checkSupport() {
        if (checking) return
        checking = true
        checkJob = lifecycleScope.launch {
            try {
                val sensors = getSystemService(SENSOR_SERVICE) as SensorManager
                if (sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
                    message = R.string.launch_missing_sensor
                    return@launch
                }
                var availability = ArCoreApk.getInstance().checkAvailability(this@LaunchScreenActivity)
                var attempts = 0
                while (availability.isTransient && attempts++ < 25) {
                    delay(200)
                    availability = ArCoreApk.getInstance().checkAvailability(this@LaunchScreenActivity)
                }
                if (!availability.isSupported) {
                    message = if (availability.isUnsupported) R.string.launch_unsupported else R.string.launch_check_failed
                    return@launch
                }
                if (ArCoreApk.getInstance().requestInstall(this@LaunchScreenActivity, !installRequested) == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    installRequested = true
                    return@launch
                }
                startActivity(Intent(this@LaunchScreenActivity, VisualizerActivity::class.java))
                finish()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                installRequested = false
                message = R.string.launch_check_failed
            } finally { checking = false }
        }
    }
}
