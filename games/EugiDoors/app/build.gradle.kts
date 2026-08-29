plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.eugi.doors"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.eugi.doors"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "0.2-retro"
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
