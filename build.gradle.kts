buildscript {
    val kotlinVersion = "2.2.10"
    val targetSdkVersion = 37

    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:9.4.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }

    // https://github.com/facebook/flipper/issues/146#issuecomment-463667556
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx" && !requested.name.startsWith("multidex")) {
                useVersion("$targetSdkVersion.+")
            }
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.10"
}

extra["compileSdkVersion"] = 37
extra["buildToolsVersion"] = "37.0.0"
extra["minSdkVersion"] = 24
extra["targetSdkVersion"] = 37

// Lookup the latest here: https://mvnrepository.com/
extra["annotationVersion"] = "1.0.1"
extra["appCompatVersion"] = "1.8.0" // https://mvnrepository.com/artifact/androidx.appcompat/appcompat
extra["espressoCoreVersion"] = "3.7.0" // https://mvnrepository.com/artifact/androidx.test.espresso/espresso-core
extra["jodaTimeVersion"] = "2.15.0" // https://github.com/JodaOrg/joda-time/releases
extra["junitVersion"] = "4.13.2"
extra["junitJupiterVersion"] = "6.1.3"
extra["preferenceVersion"] = "1.2.1"
extra["testCoreVersion"] = "1.7.0"
extra["testRulesVersion"] = "1.7.0"
extra["testRunnerVersion"] = "1.7.0"
extra["kotlinVersion"] = "2.2.10" // https://kotlinlang.org/docs/releases.html#release-details
extra["ktxVersion"] = "1.19.1" // https://androidx.tech/artifacts/core/core-ktx/

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
