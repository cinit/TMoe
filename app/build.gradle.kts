@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.UUID

plugins {
    id("build-logic.android.application")
    alias(libs.plugins.ksp)
}

val currentBuildUuid = UUID.randomUUID().toString()
println("Current build ID is $currentBuildUuid")

android {

    buildToolsVersion = "35.0.0"

    namespace = "cc.ioctl.tmoe"

    defaultConfig {

        applicationId = "cc.ioctl.tmoe"

        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk

        versionCode = Common.getBuildVersionCode(rootProject)
        versionName = Common.getBuildVersionName(rootProject)

        buildConfigField("String", "BUILD_UUID", "\"$currentBuildUuid\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")

        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        resourceConfigurations += setOf("en", "zh-rCN", "zh-rTW", "ru", "es")

        externalNativeBuild {
            cmake {
                arguments += arrayOf("-DTMOE_VERSION=$versionName", "-DANDROID_PLATFORM=android-" + Versions.minSdk)
                cppFlags += "-std=c++20"
                arguments += "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
            }
        }
    }

    buildTypes {
        release {
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
            ndk {
                debugSymbolLevel = "full"
            }
        }
        debug {
            isDebuggable = true
            isJniDebuggable = true
            isShrinkResources = false
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }


    externalNativeBuild {
        cmake {
            path = File(projectDir, "src/main/cpp/CMakeLists.txt")
            version = Versions.getCMakeVersion(project)
        }
    }
    androidResources {
        additionalParameters += setOf("--allow-reserved-package-id", "--package-id", "0x73")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    if (System.getenv("KEYSTORE_PATH") != null) {
        signingConfigs {
            create("release") {
                storeFile = file(System.getenv("KEYSTORE_PATH"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                enableV2Signing = true
            }
        }
        buildTypes {
            release {
                signingConfig = signingConfigs.findByName("release")
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += "-Xcontext-receivers"
    }
}

kotlin {
    sourceSets.configureEach {
        kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
    }
}

dependencies {
    ksp(projects.libs.ksp)
    implementation(libs.mmkv.static)
    implementation(libs.core)
    implementation(libs.hiddenapibypass)
    implementation(libs.dexlib2)
    implementation(libs.google.guava)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.ezxhelper)
    compileOnly(libs.xposed)
    compileOnly(libs.javax.annotation)
}
