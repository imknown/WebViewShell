plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    // compileSdkExtension = libs.versions.compileSdkExtension.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()
    val isPreview = libs.versions.isPreview.get().toBoolean()
    if (isPreview) {
        compileSdkPreview = libs.versions.compileSdkPreview.get()
        buildToolsVersion = libs.versions.buildToolsPreview.get()
    }

    namespace = "net.imknown.android.webviewshell"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        targetSdk = libs.versions.targetSdk.get().toInt()
        if (isPreview) {
            targetSdkPreview = libs.versions.targetSdkPreview.get()
        }

        versionCode = 3
        versionName = "1.0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.webkit)

    implementation(libs.material)
}