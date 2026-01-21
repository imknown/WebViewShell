plugins {
    alias(libs.plugins.android.application)
}

private val buildVersion = libs.versions

android {
    val isPreview = buildVersion.isPreview.get().toBoolean()
    compileSdk {
        version = if (isPreview) {
            preview(buildVersion.compileSdkPreview.get())
        } else {
            release(buildVersion.compileSdk.get().toInt()) {
                minorApiLevel = buildVersion.compileSdkMinor.get().toInt()
                // sdkExtension = buildVersion.compileSdkExtension.get().toInt()
            }
        }
    }
    buildToolsVersion = (if (isPreview) buildVersion.buildToolsPreview else buildVersion.buildTools).get()

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

    base.archivesName = "IMK_WebViewShell-${defaultConfig.versionName}"

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