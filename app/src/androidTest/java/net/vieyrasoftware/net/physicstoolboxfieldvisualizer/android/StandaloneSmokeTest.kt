package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.core.content.FileProvider
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings.SettingsActivity

@RunWith(AndroidJUnit4::class)
class StandaloneSmokeTest {
    @Test fun nativeLibrariesLoadOnDevice() {
        // Run on both 4 KB and 16 KB devices: Java-only launch would miss AR linker failures.
        listOf("filament-jni", "filament-utils-jni", "gltfio-jni", "arcore_sdk_jni", "androidx.graphics.path").forEach {
            System.loadLibrary(it)
        }
    }

    @Test fun settingsOpensWithoutCameraPermission() {
        ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
            scenario.onActivity { assertFalse(it.isFinishing) }
        }
    }

    @Test fun snapshotProviderSharesOnlySnapshotDirectory() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("net.vieyrasoftware.physicstoolboxfieldvisualizer.android", context.packageName)
        val allowed = File(context.cacheDir, "magna_ar_snapshots/test.png")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", allowed)
        assertEquals("content", uri.scheme)
        try {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", File(context.cacheDir, "private.txt"))
            fail("Files outside the snapshot directory must not be shareable")
        } catch (_: IllegalArgumentException) { }
    }
}
