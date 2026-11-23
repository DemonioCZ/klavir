package cz.klavir.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import cz.klavir.app.ui.Barvy
import cz.klavir.app.ui.ObrazovkaDomu
import cz.klavir.app.ui.ObrazovkaLekce
import cz.klavir.app.ui.ObrazovkaOdznaky
import cz.klavir.app.ui.ObrazovkaPisnicka
import cz.klavir.app.ui.ObrazovkaProRodice
import cz.klavir.app.ui.ObrazovkaProfily
import cz.klavir.app.ui.ObrazovkaVysledek

class MainActivity : ComponentActivity() {
    private lateinit var stav: AppStav

    private val zadostOMikrofon = registerForActivityResult(ActivityResultContracts.RequestPermission()) { povoleno ->
        stav.mikrofonPovolen = povoleno
        if (!povoleno) stav.prepniVstup(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stav = AppStav(this)
        stav.mikrofonPovolen = maMikrofon()
        if (stav.hrajeNaPiano && !stav.mikrofonPovolen) pozadatOMikrofon()

        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(Barvy.pozadi).safeDrawingPadding()) {
                    KlavirApp(stav, ::pozadatOMikrofon, onKonec = { finish() })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stav.mikrofonPovolen = maMikrofon()
    }

    override fun onDestroy() {
        super.onDestroy()
        stav.uvolni()
    }

    private fun maMikrofon() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun pozadatOMikrofon() {
        if (!maMikrofon()) zadostOMikrofon.launch(Manifest.permission.RECORD_AUDIO)
    }
}

/** Přepínání obrazovek podle stavu aplikace. */
@Composable
fun KlavirApp(stav: AppStav, pozadatOMikrofon: () -> Unit, onKonec: () -> Unit) {
    BackHandler {
        if (!stav.zpet()) onKonec()
    }
    AnimatedContent(
        targetState = stav.obrazovka,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { it::class to (it as? Obrazovka.Lekce)?.id to (it as? Obrazovka.Pisnicka)?.id },
        label = "obrazovky",
    ) { obr ->
        when (obr) {
            Obrazovka.Profily -> ObrazovkaProfily(stav)
            Obrazovka.Domu -> ObrazovkaDomu(stav)
            is Obrazovka.Lekce -> ObrazovkaLekce(stav, obr.id, pozadatOMikrofon)
            is Obrazovka.Pisnicka -> ObrazovkaPisnicka(stav, obr.id, pozadatOMikrofon)
            is Obrazovka.Vysledek -> ObrazovkaVysledek(stav, obr)
            Obrazovka.Odznaky -> ObrazovkaOdznaky(stav)
            Obrazovka.ProRodice -> ObrazovkaProRodice(stav)
        }
    }
}

/** Číslo a název nainstalované verze (bez generované třídy BuildConfig). */
object BuildConfigInfo {
    private fun info(context: Context) = context.packageManager.getPackageInfo(context.packageName, 0)

    fun cisloVerze(context: Context): Int {
        val i = info(context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) i.longVersionCode.toInt() else @Suppress("DEPRECATION") i.versionCode
    }

    fun nazevVerze(context: Context): String = "${info(context).versionName} (sestavení ${cisloVerze(context)})"
}
