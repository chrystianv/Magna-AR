# Release readiness — September 10, 2026

## Implemented

Ported the suite's Magna-AR renderer, controls, field readouts, lesson, snapshot UI, compass, settings and team screens from suite revision `560d8c419`. The standalone package identity and legacy icon remain intact. Legacy Sceneform/synthetic-view/onboarding/build dependencies are replaced by SceneView, AndroidX, Material 3 Compose, AGP 9.3.2 and Gradle 9.5.0, with SDK 37 and Java 17.

Standalone-specific work includes permission denial recovery, bounded availability polling, ARCore installation handling, magnetometer checks, restricted snapshot sharing, cancellation of asynchronous model loading with the activity lifecycle, and guards for early activity termination. Empty sensor-history operations and fractional averaging are corrected. No suite analytics, Firebase, billing, or public-storage permissions are included.

## Verified

- `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `assembleRelease`, and `bundleRelease` succeed.
- Five JVM regression tests pass: automatic vector scheduling starts once and stops correctly, the snapshot control retains its suite layout, Spanish covers every translatable string, and Spanish formatting arguments match English.
- Lint: zero errors. Remaining warnings include inherited translations, dependency updates, and style suggestions. Only missing translations are explicitly downgraded to warnings; English fallback remains available.
- Three instrumentation smoke tests pass on both 4 KB and 16 KB ARM64 emulators: AR/Filament/Compose native libraries load, settings opens without requiring camera access, and FileProvider refuses files outside the snapshot directory.
- Emulator UI checks: initial welcome, settings, camera permission request, denial/retry, and AR service installation prompt.
- APK ZIP alignment passes `zipalign -c -P 16 4`. All twelve packaged ARM64/x86_64 libraries pass the 16 KB ELF LOAD-segment alignment check.
- Several upstream libraries have non-16-KB RELRO end boundaries. Their ARM64 libraries load successfully on the actual 16 KB emulator. The checker reports these boundaries separately; this does not establish x86_64 runtime compatibility or replace a full AR session test.
- Minified release APK and unsigned AAB are produced. A separate copy of the release APK was signed with the development key solely for emulator launch testing. The debug instrumentation APK cannot be run against the minified release because it references unminified Kotlin classes; device instrumentation results above apply to debug builds.

## Before publishing

1. Test a sustained AR session on supported physical phones: vector direction and strength, rotation, automatic placement, numerical labels, heatmap, compass orientation, reset, pause/resume, camera interruptions, screenshots and sharing. Include a 16 KB phone and a lower-end supported device. Emulator smoke tests do not validate magnetic measurements or tracking quality.
2. Validate installation over the currently published app using the existing release signing identity; confirm retained preferences. Verify version code 23 exceeds all uploaded Play artifacts.
3. Review the Spanish translations on device; all translatable strings now have Spanish text. Thirty other inherited locales still have gaps and use English fallback. See `LOCALIZATION_GAPS.json`, regenerated with `python3 scripts/audit_localization.py`.
4. Supply the existing upload key through the four `MAGNA_AR_*` environment variables documented in README, then build with `-PrequireReleaseSigning=true`. The missing-credentials guard has been verified to fail clearly; no release credentials are available yet. Confirm the certificate matches Play Console, then run internal testing/pre-launch reports. This work does not publish a release or change Play Console data.
5. Review store screenshots, privacy policy/Data Safety declarations, device exclusions, and third-party notices against the final release dependency list. The app sends no built-in analytics; support email includes a user-reviewable device diagnostic block.
6. Exercise the added GitHub Actions workflow after the change is applied. It is prepared but has not run on GitHub.

## Apply status

The modernization has been applied to the standalone Magna-AR repository with explicit owner approval. Git history and the standalone application ID are preserved. No source changes were made to Physics Toolbox Suite. The modernization is prepared for pull-request review.

## Follow-up checks

- Only `emulator-5554` is currently connected; physical-device AR testing remains pending.
- Optional external upload signing is implemented. A signing-required Gradle configuration fails as expected without credentials; unsigned debug/release builds still succeed.
- Removed 103 generated/local-only Gradle, IDE, and SDK-location files from Git tracking while retaining local files. The repository cleanup is included in the modernization pull request.

## Initial code-review findings

- Magnetic vectors are transformed with the display-oriented camera pose even though magnetometer samples are in Android sensor coordinates. Use the frame's Android sensor pose for vector conversion and test portrait/landscape invariance before merging.
- Automatic placement retains every arrow and loads a model/material/light for each. Add a bounded retention/reuse policy and validate a sustained session before release.

These findings are not covered by the passing startup and native-library smoke tests.
