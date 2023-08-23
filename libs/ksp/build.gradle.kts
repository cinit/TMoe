plugins {
    kotlin("jvm")
}

dependencies {
    // ksp
    implementation(libs.ksp)
    // kotlinpoet
    implementation(libs.kotlinpoet.ksp)
}

kotlin {
    jvmToolchain(Versions.java.toString().toInt())
}

tasks.withType<JavaCompile> {
    sourceCompatibility = Versions.java.toString()
    targetCompatibility = Versions.java.toString()
}
