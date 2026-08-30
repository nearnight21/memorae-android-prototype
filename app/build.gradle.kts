import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localProperties = Properties().apply {
    rootProject.file("local.properties")
        .takeIf { it.isFile }
        ?.inputStream()
        ?.use(::load)
}

val amapKey = providers.gradleProperty("AMAP_KEY")
    .orElse(providers.environmentVariable("AMAP_KEY"))
    .orElse(localProperties.getProperty("amap.key", ""))
android {
    namespace = "com.memorae.prototype"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.memorae.prototype"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-prototype"

        manifestPlaceholders["AMAP_KEY"] = amapKey.get()
        buildConfigField("boolean", "AMAP_KEY_PRESENT", amapKey.get().isNotBlank().toString())

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.autofill:autofill:1.3.0")
    implementation("androidx.savedstate:savedstate-compose:1.3.3")

    implementation("androidx.compose.ui:ui:1.9.3")
    implementation("androidx.compose.ui:ui-graphics:1.9.3")
    implementation("androidx.compose.foundation:foundation:1.9.3")

    implementation("com.amap.api:3dmap-location-search:10.1.200_loc6.4.9_sea9.7.4")
}
