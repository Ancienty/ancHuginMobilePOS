//  ── 1️⃣  Plugin resolution (catalog not available yet) ───────────────
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

//  ── 2️⃣  Standard repo block – NO extra `from()` call ────────────────
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // Nothing else here – Gradle already found gradle/libs.versions.toml
}

rootProject.name = "huginProject"
include(":app")
