plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.valoon4.communicationtrainer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.valoon4.communicationtrainer"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "0.5.3-d051-guided"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Previous D05.1 diagnostic APK: retain separately for development only.
        // This is debuggable and MUST NOT be distributed as a security-reviewed APK.
        getByName("debug") {
            applicationIdSuffix = ".d051guidedpreview"
            versionNameSuffix = "-preview"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
        // Non-debuggable, non-production app identity. Inherits release dependencies,
        // NOT debugImplementation(ui-tooling), and never falls back to a debug signer.
        // Deliberately unsigned at Gradle level: a separate, secret-backed, manually
        // approved CI job performs stable-keystore signing after security checks.
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".d051staging"
            versionNameSuffix = "-staging"
            isDebuggable = false
            signingConfig = null
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.lifecycle.viewmodel.ktx)

    debugImplementation(libs.compose.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    // The JVM test graph (Robolectric) previously resolved versions with
    // known advisories. These direct test-only pins override transitive ones.
    // They are NOT bundled in the Android debug/release application runtime.
    testImplementation(libs.guava.test)
    testImplementation(libs.bcprov.test)
}
