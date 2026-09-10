package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.theme

import androidx.compose.runtime.Composable

// The palette formerly defined here (SettingsDarkColorScheme) moved to
// M3ToolTheme.kt as M3DarkColorScheme so M3 tool screens (WiFi, ...) can share
// it. Screens with intentionally different palettes (calibration, CPU/battery
// temperature, video analysis, multi record) keep their own schemes.

/** Wraps a settings screen in the shared settings color scheme. */
@Composable
fun SettingsTheme(content: @Composable () -> Unit) = M3ToolTheme(content)
