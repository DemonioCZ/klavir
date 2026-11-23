// Kořenový build pro samotné jádro (gradle -PjenJadro :jadro:test).
// Nepotřebuje Android SDK ani plugin pro Android.
plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
}
