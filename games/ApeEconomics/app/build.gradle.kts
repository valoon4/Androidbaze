plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.eugi.apeeconomics"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.eugi.apeeconomics"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "0.12-debug"
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
