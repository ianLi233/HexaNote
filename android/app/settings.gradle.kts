pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack for WhisperKit
        maven { url = uri("https://jitpack.io") }
        // Add Qualcomm AI Hub repository for QNN dependencies
        maven { url = uri("https://qaihub-public-assets.s3.us-west-2.amazonaws.com/maven-repo/") }
    }
}

rootProject.name = "HexaNoteVoiceAssistant"
include(":app")
