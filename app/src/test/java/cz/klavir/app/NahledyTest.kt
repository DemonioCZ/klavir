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

    private fun stav(): AppStav {
        val s = AppStav(paparazzi.context)
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

    private fun obrazovka(obsah: @Composable () -> Unit) = paparazzi.snapshot {
        Box(Modifier.fillMaxSize().background(Barvy.pozadi)) { obsah() }
    }

    @Test fun profily() { val s = stav(); obrazovka { ObrazovkaProfily(s) } }
    @Test fun domu() { val s = stav(); obrazovka { ObrazovkaDomu(s) } }
    @Test fun lekcePrstiky() { val s = stav(); obrazovka { ObrazovkaLekce(s, "prstiky") {} } }
    @Test fun lekceNoty() { val s = stav(); obrazovka { ObrazovkaLekce(s, "noty_klavesy") {} } }
    @Test fun pisnicka() { val s = stav(); obrazovka { ObrazovkaPisnicka(s, "skakal_pes") {} } }
    @Test fun pisnickaKocka() { val s = stav(); obrazovka { ObrazovkaPisnicka(s, "kocka_leze") {} } }
    @Test fun odznaky() { val s = stav(); obrazovka { ObrazovkaOdznaky(s) } }
    @Test fun proRodice() { val s = stav(); obrazovka { ObrazovkaProRodice(s) } }
    @Test fun vysledek() {
        val s = stav()
        val h = Hodnoceni().apply { repeat(23) { spravne() }; chyba() }
        obrazovka {
            ObrazovkaVysledek(
                s,
                Obrazovka.Vysledek("Skákal pes", "režim „Počkám na tebe“", h, listOf(Odznaky.vsechny[3]), Obrazovka.Domu, Obrazovka.Domu),
            )
        }
    }
}
