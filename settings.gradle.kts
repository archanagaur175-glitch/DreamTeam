pluginManagement {
    repositories {
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
        google()
        mavenCentral()
    }
}

rootProject.name = "DreamTeam"

include(":app")
include(":core:core-common")
include(":core:core-database")
include(":core:core-ui")
include(":feature:sleepdebt")
include(":feature:circadian")
include(":feature:smartalarm")
include(":feature:logger")
include(":feature:dashboard")
