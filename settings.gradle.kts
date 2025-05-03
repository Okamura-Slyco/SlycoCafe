pluginManagement {
    repositories {
        maven(url = "https://jitpack.io")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://jitpack.io")
        google()
        mavenCentral()
        maven (
            url = "https://s3.amazonaws.com/sdk.clover.com/android/maven"
        )
    }
}

rootProject.name = "Slyco Café"
include(":app")
 