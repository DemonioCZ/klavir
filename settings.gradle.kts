// Nastavení projektu Klavírní samouk
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "klavir"

// Modul „jadro“ je čistý Kotlin (noty, písničky, rozpoznání tónu, body) a dá se testovat bez Android SDK.
include(":jadro")
// Aplikaci zahrneme jen tehdy, pokud nestavíme samotné jádro (-PjenJadro).
if (providers.gradleProperty("jenJadro").isPresent) {
    rootProject.buildFileName = "jadro-root.gradle.kts"
} else {
    include(":app")
}
