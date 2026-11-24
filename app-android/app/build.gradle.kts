plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.alarma2.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alarma2.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-F1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
