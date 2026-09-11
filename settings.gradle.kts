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

rootProject.name = "kite"

include(":app")

// :core — infrastructure. Features depend on these and never on each other.
include(":core:designsystem")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:player")
include(":core:data")

// :feature — screens. Anything shared by two features moves down into :core.
include(":feature:feed")
include(":feature:watch")
include(":feature:library")
include(":feature:playlists")
include(":feature:shorts")
include(":feature:settings")
