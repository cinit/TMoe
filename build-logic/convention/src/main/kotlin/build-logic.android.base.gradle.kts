@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.BaseExtension

plugins {
    id("com.android.base")
    kotlin("android")
}

extensions.findByType(BaseExtension::class)?.run {
    compileSdkVersion(Versions.compileSdkVersion)

    ndkVersion = Versions.getNdkVersion(project)

    defaultConfig {
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }

    packagingOptions.jniLibs.useLegacyPackaging = false
}

kotlin {
    jvmToolchain(Versions.java.toString().toInt())
}
