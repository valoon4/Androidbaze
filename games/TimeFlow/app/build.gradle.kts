plugins {
    id("com.android.application")
}
android {
    namespace = "com.valoon4.timeflow"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.valoon4.timeflow"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-debug"
    }
    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
