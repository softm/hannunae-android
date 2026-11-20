pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content { includeGroup("com.github.softm") }
        }
    }
}
rootProject.name = "hannunae-android"
include(":app", ":hannun-webview")
val sharedDirectory = providers.gradleProperty("hannunWebViewDir").orNull
    ?.let { file(it) } ?: file("libraries/hannun-webview-android")
check(sharedDirectory.resolve("library/build.gradle.kts").isFile) {
    "Run git submodule update --init --recursive to fetch hannun-webview-android."
}
project(":hannun-webview").projectDir = sharedDirectory.resolve("library")
