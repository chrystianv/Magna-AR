package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color, typography, and shape tokens shared with iOS (PR #855, M3Theme.swift).
// Dark-only. Seeded from brand green #388E3C. Values are copied hex-for-hex
// from iOS so the two platforms render identical readouts.
object M3Color {
    val primary                 = Color(0xFF7FD98A)
    val onPrimary               = Color(0xFF003910)
    val primaryContainer        = Color(0xFF00531A)
    val onPrimaryContainer      = Color(0xFF9AF5A4)
    val surface                 = Color(0xFF0F140F)
    val surfaceContainerLow     = Color(0xFF171C17)
    val surfaceContainer        = Color(0xFF1C211C)
    val surfaceContainerHigh    = Color(0xFF262B26)
    val surfaceContainerHighest = Color(0xFF313631)
    val onSurface               = Color(0xFFDFE4DE)
    val onSurfaceVariant        = Color(0xFFC2C8BE)
    val outline                 = Color(0xFF8B9287)
    val outlineVariant          = Color(0xFF414941)
    val error                   = Color(0xFFFFB4AB)
    // Glyph color for content sitting *on* [error] — a filled error chip or
    // badge. M3ToolTheme already declared this literal; it belongs here with
    // the rest of the ramp.
    val onError                 = Color(0xFF690005)

    // Caution accent (scan-throttle notice, trial countdown). Not a standard
    // M3 slot; mirror hex-for-hex into iOS M3Theme.swift like the rest.
    // 9.5:1 on surfaceContainer, so legal for small text.
    val warning                 = Color(0xFFFFB74D)

    // Brand green for app bars that carry white chrome. Not a standard M3 slot.
    //
    // This is the app bar colour, so it tracks the app bar: every XML-hosted
    // screen paints its Toolbar `?attr/colorPrimary`, which resolves to
    // `@color/my_primary` #4CAF50. Hold this token to the same hex and the
    // Compose-hosted bars (Play hub, in-challenge, Learn More, User Guide) are
    // the same green as the rest of the app instead of four screens that read
    // as a darker shade of it.
    //
    // It was #2E7D32 for contrast: white title text on #4CAF50 measures 2.78:1,
    // under the 4.5:1 WCAG asks of a 22sp title and the 3:1 it asks of the
    // action icons, where #2E7D32 gave 5.13:1. That shortfall is real but it is
    // not specific to these bars — the XML Toolbar has carried it app-wide all
    // along, so darkening only the Compose bars bought contrast on four screens
    // at the price of making them the visual odd ones out. Raising contrast is
    // worth doing as one deliberate pass over `my_primary` and every bar that
    // reads it; until then these bars stay consistent with the app.
    //
    // Still NOT a change to `primary` itself: under PhysicsToolboxTheme that
    // token is also a *foreground* tint on dark surfaces (LoadScreen's icons
    // and labels), so the two want to move independently.
    val brandAppBar            = Color(0xFF4CAF50)

    // North on the compass rose. Not a standard M3 slot, and deliberately not
    // [error]: the compass paints error red for interference warnings, and the
    // landmark telling you where north is must not read as an alert. Red north
    // is the oldest convention in navigation — every magnetic needle has a red
    // north end — so it wants to be vivid, where [error] is tuned to be a
    // gentle tone for warning text. 5.8:1 on [surface]. Mirror hex-for-hex into
    // iOS M3Theme.swift like the rest.
    val compassNorth            = Color(0xFFFF5252)

    // Tool-grid category header accent. Not a standard M3 slot; a desaturated,
    // warm-of-red tone the owner picked over pure red — ~5.3:1 on surface (legal
    // for small text) and distinct from the REC red used elsewhere in the app.
    // Mirror hex-for-hex into iOS M3Theme.swift like the rest, if this need
    // arises on iOS.
    val categoryAccent          = Color(0xFFD1717A)

    // The capture-data accent: recording FABs on every graph screen and the WiFi
    // scan button. Deliberately NOT [error] (which is a gentle text tone): this
    // is chrome for "this control captures data", the oldest convention in the
    // app. White on it measures 3.68:1, so labels are bold 14sp+ only.
    // 34 other files still hardcode this hex; migrate them to this token when
    // each screen is next touched.
    val recordAccent            = Color(0xFFF44336)
}
