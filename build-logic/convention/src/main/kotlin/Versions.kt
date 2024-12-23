import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.io.File
import java.util.Properties

object Versions {

    val java = JavaVersion.VERSION_11
    const val compileSdkVersion = 35
    const val minSdk = 24
    const val targetSdk = 34
    const val versionName = "1.0"

    private const val defaultNdkVersion = "27.2.12479018"
    private const val defaultCMakeVersion = "3.31.1"

    fun getNdkVersion(project: Project): String {
        val prop = getLocalProperty(project, "tmoe.override.ndk.version")
        val env = getEnvVariable("TMOE_OVERRIDE_NDK_VERSION")
        if (!prop.isNullOrEmpty() && !env.isNullOrEmpty()) {
            throw IllegalStateException("Cannot set both TMOE_OVERRIDE_NDK_VERSION and tmoe.override.ndk.version")
        }
        return prop ?: env ?: defaultNdkVersion
    }

    fun getCMakeVersion(project: Project): String {
        val prop = getLocalProperty(project, "tmoe.override.cmake.version")
        val env = getEnvVariable("TMOE_OVERRIDE_CMAKE_VERSION")
        if (!prop.isNullOrEmpty() && !env.isNullOrEmpty()) {
            throw IllegalStateException("Cannot set both TMOE_OVERRIDE_CMAKE_VERSION and tmoe.override.cmake.version")
        }
        return prop ?: env ?: defaultCMakeVersion
    }

    private fun getLocalProperty(project: Project, propertyName: String): String? {
        val rootProject = project.rootProject
        val localProp = File(rootProject.projectDir, "local.properties")
        if (!localProp.exists()) {
            return null
        }
        val localProperties = Properties()
        localProp.inputStream().use {
            localProperties.load(it)
        }
        return localProperties.getProperty(propertyName, null)
    }

    private fun getEnvVariable(name: String): String? {
        return System.getenv(name)
    }

}
