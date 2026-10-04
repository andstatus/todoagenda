plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

extra["bintrayRepo"] = "maven"
extra["bintrayName"] = "colorpicker"

extra["publishedGroupId"] = "com.rarepebble"
extra["libraryName"] = "HSV-Alpha Color Picker for Android"
extra["artifact"] = "colorpicker"

extra["libraryDescription"] = "A library providing a ColorPreference and ColorPickerView for Android."

extra["siteUrl"] = "https://github.com/martin-stone/hsv-alpha-color-picker-android"
extra["gitUrl"] = "https://github.com/martin-stone/hsv-alpha-color-picker-android.git"

extra["libraryVersion"] = "2.4.2"

extra["developerId"] = "martin-stone"
extra["developerName"] = "Martin Stone"
// extra["developerEmail"] = "@rarepebble.com"

extra["licenseName"] = "The Apache Software License, Version 2.0"
extra["licenseUrl"] = "http://www.apache.org/licenses/LICENSE-2.0.txt"
extra["allLicenses"] = listOf("Apache-2.0")

val rootCompileSdkVersion = rootProject.extra["compileSdkVersion"] as Int
val rootBuildToolsVersion = rootProject.extra["buildToolsVersion"] as String
val rootMinSdkVersion = rootProject.extra["minSdkVersion"] as Int
val rootTargetSdkVersion = rootProject.extra["targetSdkVersion"] as Int

val appCompatVersion = rootProject.extra["appCompatVersion"] as String
val preferenceVersion = rootProject.extra["preferenceVersion"] as String
val ktxVersion = rootProject.extra["ktxVersion"] as String

android {
    compileSdk = rootCompileSdkVersion
    buildToolsVersion = rootBuildToolsVersion

    defaultConfig {
        minSdk = rootMinSdkVersion
        (this as com.android.build.gradle.internal.dsl.DefaultConfig)
            .targetSdkVersion(rootTargetSdkVersion)
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "com.rarepebble.colorpicker"
}

dependencies {
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.preference:preference:$preferenceVersion")
    implementation("androidx.core:core-ktx:$ktxVersion")
}
