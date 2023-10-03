import java.io.FileInputStream
import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

/**
 * Loads local user-specific build properties that are not checked into source control.
 */
val userProperties = Properties().apply {
    val buildPropertiesFile = File(rootDir, "user.properties")
    if (buildPropertiesFile.exists()) {
        FileInputStream(buildPropertiesFile).use { load(it) }
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages (Bitwarden)"
            url = uri("https://maven.pkg.github.com/bitwarden/sdk")
            credentials {
                username = ""
                password = userProperties["gitHubToken"] as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "Bitwarden"
include(":app")