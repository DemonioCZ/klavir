package cz.klavir.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import cz.klavir.app.ui.Barvy
import cz.klavir.app.ui.ObrazovkaDomu
import cz.klavir.app.ui.ObrazovkaLekce
import cz.klavir.app.ui.ObrazovkaOdznaky
import cz.klavir.app.ui.ObrazovkaPisnicka
import cz.klavir.app.ui.ObrazovkaProRodice
import cz.klavir.app.ui.ObrazovkaProfily
import cz.klavir.app.ui.ObrazovkaVysledek
import cz.klavir.jadro.Hodnoceni
import cz.klavir.jadro.Odznaky
import org.junit.Rule
import org.junit.Test

/**
 * Vykreslí obrazovky aplikace do PNG (tablet 10" na šířku, 1280 × 800 dp).
 * Spuštění: ./gradlew :app:recordPaparazziRelease – obrázky jsou v app/src/test/snapshots.
 */
class NahledyTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = 2560,
            screenHeight = 1600,
            density = Density.XHIGH,
            orientation = ScreenOrientation.LANDSCAPE,
            softButtons = false,
        ),
        showSystemUi = false,
    )

    private fun stav(kontext: android.content.Context): AppStav {
        val s = AppStav(kontext)
        if (s.profily.isEmpty()) {
            s.pridejProfil("Anička", "🦊")
            s.pridejProfil("Tonda", "🐻")
        }
        val p = s.profily.first()
        s.upravProfil(
            p.copy(
                body = 1240, dnuVRade = 5,
                hvezdyLekci = mapOf("prstiky" to 3, "cde" to 3),
                hvezdyPisnicek = mapOf("skakal_pes" to 2),
                odznaky = setOf("prvni_ton", "prstiky", "prvni_pisnicka", "dny5"),
            ),
        )
        s.vyberProfil(p.id)
        s.prepniVstup(false)
        return s
    }

    private fun obrazovka(obsah: @Composable (AppStav) -> Unit) = paparazzi.snapshot {
        val kontext = androidx.compose.ui.platform.LocalContext.current
        val s = androidx.compose.runtime.remember { stav(kontext) }
        Box(Modifier.fillMaxSize().background(Barvy.pozadi)) { obsah(s) }
    }

    @Test fun profily() = obrazovka { s -> ObrazovkaProfily(s) }
    @Test fun domu() = obrazovka { s -> ObrazovkaDomu(s) }
    @Test fun lekcePrstiky() = obrazovka { s -> ObrazovkaLekce(s, "prstiky") {} }
    @Test fun lekceCde() = obrazovka { s -> ObrazovkaLekce(s, "cde") {} }
    @Test fun lekceFg() = obrazovka { s -> ObrazovkaLekce(s, "fg") {} }
    @Test fun lekceNoty() = obrazovka { s -> ObrazovkaLekce(s, "noty_klavesy") {} }
    @Test fun pisnicka() = obrazovka { s -> ObrazovkaPisnicka(s, "skakal_pes") {} }
    @Test fun pisnickaKocka() = obrazovka { s -> ObrazovkaPisnicka(s, "kocka_leze") {} }
    @Test fun odznaky() = obrazovka { s -> ObrazovkaOdznaky(s) }
    @Test fun proRodice() = obrazovka { s -> ObrazovkaProRodice(s) }
    @Test fun vysledek() {
        val h = Hodnoceni().apply { repeat(23) { spravne() }; chyba() }
        obrazovka { s ->
            ObrazovkaVysledek(
                s,
                Obrazovka.Vysledek("Skákal pes", "režim „Počkám na tebe“", h, listOf(Odznaky.vsechny[3]), Obrazovka.Domu, Obrazovka.Domu),
            )
        }
    }
}
