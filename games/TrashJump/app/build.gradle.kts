plugins {
    id("com.android.application")
    kotlin("android")
}
android {
    namespace = "com.eugi.trashjump"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.eugi.trashjump"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-trashwave"
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
