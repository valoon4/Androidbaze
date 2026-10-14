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
        targetSdk = 35
        versionCode = 20
        versionName = "2.0-level0"
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
