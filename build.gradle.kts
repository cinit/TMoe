// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("build-logic.root-project")
    alias(libs.plugins.kotlin.jvm) apply false
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}

true // Needed to make the Suppress annotation work for the plugins block
