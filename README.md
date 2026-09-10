# Magna-AR for Android

Standalone magnetic-field visualization using the device magnetometer and ARCore. The visualization, compass, controls, settings, and team screens are ported from Physics Toolbox Suite (source revision `560d8c419`, September 10, 2026).

Magna-AR is supported by the National Science Foundation Cyberlearning program, grant NSF #1822728. Existing project/team attribution and the standalone launcher icon are retained.

## Build

Use a current Android Studio installation with JDK 17 or newer and Android SDK 37. The checked-in Gradle wrapper uses Gradle 9.5.0 and Android Gradle Plugin 9.3.2. Kotlin/Compose tooling is 2.3.0. Dependencies resolve from Google Maven and Maven Central.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:bundleRelease :app:assembleRelease
./gradlew :app:connectedDebugAndroidTest
```

The app supports Android 7.0/API 24 and above, targets API 37, and requires ARCore support, OpenGL ES 3.0, and a magnetometer. Keep the SDK location in your local `local.properties` file.

The application ID remains `net.vieyrasoftware.physicstoolboxfieldvisualizer.android`, allowing an update to the existing listing when signed with its existing release key. Version code is 23; verify it exceeds every version uploaded to Play before submission. Release outputs are intentionally unsigned until configured with the existing upload key. Do not put signing credentials in source control.

## Functionality

- Real magnetic vectors in the camera view, manually placed or added automatically.
- Suite layout, onboarding lesson, XYZ/total readouts, numerical vector labels and heatmap scale.
- AR compass, image capture and Android share sheet, and reset.
- Material 3 settings and project/team attribution.
- Standalone launch screen with camera denial recovery, bounded ARCore availability checks, AR service installation, and missing-magnetometer handling.

SceneView 2.3.0 replaces the discontinued Sceneform implementation. Arrow assets use GLB. Kotlin synthetic views, the old Sceneform Gradle plugin, JCenter, obsolete onboarding libraries, CameraX alpha dependencies, and public-storage permissions are removed. The app does not include the suite's analytics or billing integrations.

Snapshot sharing uses a non-exported FileProvider limited to `cache/magna_ar_snapshots/`. Images are shared only through user action. Camera permission is required; broad storage access is not.

## Validation and release

See [release readiness](docs/RELEASE_READINESS.md) for evidence and remaining physical-device/release checks. The suite's existing translations are retained; untranslated messages fall back to English. New standalone launch messages have English and Spanish translations. Lint reports translation gaps as warnings so they remain visible; other lint errors still fail the build.

## Release signing

Use the **existing Magna-AR upload key**, with all four environment variables supplied by your local secret manager or CI secrets:

- `MAGNA_AR_STORE_FILE`: absolute path to the existing keystore.
- `MAGNA_AR_STORE_PASSWORD`: keystore password.
- `MAGNA_AR_KEY_ALIAS`: existing upload-key alias.
- `MAGNA_AR_KEY_PASSWORD`: key password.

With none set, release builds remain unsigned for review. Partial configuration fails clearly. To require a signed release, run:

```sh
./gradlew :app:bundleRelease :app:assembleRelease -PrequireReleaseSigning=true
```

No keystore or passwords belong in this repository. The configured key still needs to be matched against the upload certificate registered in Play Console before upload.

Run `python3 scripts/audit_localization.py` for a per-language missing-string report. English and Spanish completeness are regression-tested; other inherited languages retain English fallback until reviewed.
