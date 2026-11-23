// Android aplikace Klavírní samouk (Kotlin + Jetpack Compose).
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "cz.klavir.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "cz.klavir.samouk"
        minSdk = 26
        targetSdk = 35
        // Číslo sestavení dodá GitHub Actions (-PcisloSestaveni=…); podle něj aplikace pozná novější verzi.
        val cislo = (project.findProperty("cisloSestaveni") as String?)?.toIntOrNull() ?: 1
        versionCode = cislo
        versionName = "0.1.$cislo"
    }

    // Všechny verze podepisujeme stejným klíčem, jinak by Android aktualizaci odmítl.
    // Klíč prototypu je v repozitáři (podpis/prototyp.jks); pro ostrý provoz ho nahraďte
    // tajným klíčem uloženým v GitHub Secrets.
    signingConfigs {
        create("prototyp") {
            storeFile = rootProject.file("podpis/prototyp.jks")
            storePassword = "klavir-prototyp"
            keyAlias = "klavir"
            keyPassword = "klavir-prototyp"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("prototyp")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("prototyp")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":jadro"))

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
