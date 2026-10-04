plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val rootCompileSdkVersion = rootProject.extra["compileSdkVersion"] as Int
val rootBuildToolsVersion = rootProject.extra["buildToolsVersion"] as String
val rootMinSdkVersion = rootProject.extra["minSdkVersion"] as Int
val rootTargetSdkVersion = rootProject.extra["targetSdkVersion"] as Int

val appCompatVersion = rootProject.extra["appCompatVersion"] as String
val espressoCoreVersion = rootProject.extra["espressoCoreVersion"] as String
val jodaTimeVersion = rootProject.extra["jodaTimeVersion"] as String
val junitVersion = rootProject.extra["junitVersion"] as String
val junitJupiterVersion = rootProject.extra["junitJupiterVersion"] as String
val preferenceVersion = rootProject.extra["preferenceVersion"] as String
val testCoreVersion = rootProject.extra["testCoreVersion"] as String
val testRulesVersion = rootProject.extra["testRulesVersion"] as String
val testRunnerVersion = rootProject.extra["testRunnerVersion"] as String
val ktxVersion = rootProject.extra["ktxVersion"] as String

android {
    compileSdk = rootCompileSdkVersion
    buildToolsVersion = rootBuildToolsVersion

    useLibrary("android.test.mock")

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        versionCode = 719
        versionName = "4.14.1"
        minSdk = rootMinSdkVersion
        targetSdk = rootTargetSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "ORG_TASKS_AUTHORITY", "\"org.tasks\"")
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
    lint {
        abortOnError = false
        warning += setOf("MissingTranslation", "InvalidPackage")
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
    namespace = "org.andstatus.todoagenda"
    buildFeatures {
        buildConfig = true
    }

    providers.gradleProperty("todoagendaStoreFile").orNull?.let { storeFileProperty ->
        val releaseConfig = signingConfigs.create("releaseConfig") {
            storeFile = file(storeFileProperty)
            storePassword = providers.gradleProperty("todoagendaStorePassword").get()
            keyAlias = providers.gradleProperty("todoagendaKeyAlias").get()
            keyPassword = providers.gradleProperty("todoagendaKeyPassword").get()
        }
        buildTypes.getByName("release").signingConfig = releaseConfig
    }

    // See https://www.timroes.de/2013/09/22/handling-signing-configs-with-gradle/
    providers.gradleProperty("todoagenda.signing").orNull?.let { signingScriptBase ->
        val signingScript = file("$signingScriptBase.gradle")
        if (signingScript.exists()) {
            apply(from = signingScript)
        }
    }
}

dependencies {
    implementation("joda-time:joda-time:$jodaTimeVersion")
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("androidx.preference:preference:$preferenceVersion")
    implementation(project(":colorpicker"))
    implementation("androidx.core:core-ktx:$ktxVersion")

    androidTestImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test:core:$testCoreVersion")
    androidTestImplementation("androidx.test:rules:$testRulesVersion")
    androidTestImplementation("androidx.test:runner:$testRunnerVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoCoreVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitJupiterVersion")
}
