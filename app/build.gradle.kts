plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
// Keep the existing Play upload identity outside the repository. Never generate a replacement key.
val uploadSigningNames = listOf(
    "MAGNA_AR_STORE_FILE", "MAGNA_AR_STORE_PASSWORD", "MAGNA_AR_KEY_ALIAS", "MAGNA_AR_KEY_PASSWORD"
)
val uploadSigning = uploadSigningNames.associateWith { providers.environmentVariable(it).orNull }
val hasUploadSigning = uploadSigning.values.all { !it.isNullOrBlank() }
val hasPartialUploadSigning = uploadSigning.values.any { !it.isNullOrBlank() } && !hasUploadSigning
val requireReleaseSigning = providers.gradleProperty("requireReleaseSigning").orNull == "true"
check(!hasPartialUploadSigning) {
    "Incomplete Magna-AR upload signing configuration. Set all four MAGNA_AR signing environment variables."
}
check(!requireReleaseSigning || hasUploadSigning) {
    "Release signing is required. Configure the existing Magna-AR upload key using the MAGNA_AR signing environment variables."
}

android {
    if (hasUploadSigning) {
        signingConfigs.create("upload") {
            storeFile = file(uploadSigning.getValue("MAGNA_AR_STORE_FILE")!!)
            storePassword = uploadSigning.getValue("MAGNA_AR_STORE_PASSWORD")
            keyAlias = uploadSigning.getValue("MAGNA_AR_KEY_ALIAS")
            keyPassword = uploadSigning.getValue("MAGNA_AR_KEY_PASSWORD")
        }
    }
    namespace = "net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "net.vieyrasoftware.physicstoolboxfieldvisualizer.android"
        minSdk = 24
        targetSdk = 37
        versionCode = 23
        versionName = "2026.09.10"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    buildFeatures { compose = true; buildConfig = true }
    buildTypes {
        release {
            if (hasUploadSigning) signingConfig = signingConfigs.getByName("upload")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    // Keep inherited translation gaps visible while Android uses English fallbacks.
    lint { warning.add("MissingTranslation") }
    testOptions { unitTests.isReturnDefaultValues = true }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.sceneview.arsceneview)
    implementation(libs.google.arcore)
    implementation(libs.kotlin.math)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}

tasks.withType<Test>().configureEach { inputs.dir("src/main/res") }
