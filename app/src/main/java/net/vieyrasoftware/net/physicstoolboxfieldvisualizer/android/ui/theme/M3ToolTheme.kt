package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Shared dark scheme for M3 tool screens, built from the M3Tokens brand-green
// ramp (kept hex-identical with iOS). Extracted from SettingsTheme so tool
// screens (WiFi, ...) and settings screens wear the same scheme.
//
// Role mapping quirk (deliberate, inherited from the settings convention):
// `background` is the page tone, but `surface` maps to M3Color.surfaceContainer
// — the CARD tone one step above the page — with `surfaceVariant` the
// inset-item tone above that. Screens should reference M3Color.* tokens
// directly for surfaces rather than relying on colorScheme.surface.
// Secondary stays the legacy red for reset/destructive accents.
internal val M3DarkColorScheme = darkColorScheme(
    primary = M3Color.primary,
    onPrimary = M3Color.onPrimary,
    primaryContainer = M3Color.primaryContainer,
    onPrimaryContainer = M3Color.onPrimaryContainer,
    secondary = Color(0xFFF44336),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD32F2F),
    background = M3Color.surface,
    onBackground = M3Color.onSurface,
    surface = M3Color.surfaceContainer,
    onSurface = M3Color.onSurface,
    surfaceVariant = M3Color.surfaceContainerHigh,
    onSurfaceVariant = M3Color.onSurfaceVariant,
    surfaceContainerLowest = M3Color.surface,
    surfaceContainerLow = M3Color.surfaceContainerLow,
    surfaceContainer = M3Color.surfaceContainer,
    surfaceContainerHigh = M3Color.surfaceContainerHigh,
    surfaceContainerHighest = M3Color.surfaceContainerHighest,
    outline = M3Color.outline,
    outlineVariant = M3Color.outlineVariant,
    error = M3Color.error,
    onError = M3Color.onError
)

/** Wraps an M3 tool screen (WiFi, ...) in the shared dark color scheme. */
@Composable
fun M3ToolTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = M3DarkColorScheme,
        content = content
    )
}
