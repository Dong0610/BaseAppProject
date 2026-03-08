pluginManagement {
    val mavenUser: String by settings
    val mavenPassword: String by settings
    repositories {
        maven(url = "https://maven.pkg.github.com/azuraglobal/plugin") {
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
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
    val mavenUser: String by settings
    val mavenPassword: String by settings
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://artifact.bytedance.com/repository/pangle/")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven(url = "https://maven.pkg.github.com/azuraglobal/AzModuleAds") {
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
}


rootProject.name = "Base Project"
include(":app")
include(":lib")
