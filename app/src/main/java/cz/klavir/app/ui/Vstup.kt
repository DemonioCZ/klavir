package cz.klavir.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import cz.klavir.app.AppStav
import cz.klavir.jadro.Noty

/**
 * Napojí obrazovku hry na vstup: mikrofon (skutečné piano) a ťukání na displej.
 * Vrací funkci, kterou má klaviatura zavolat při ťuknutí.
 *
 * @param onTon voláno s tónem MIDI a informací, zda přišel z mikrofonu
 */
@Composable
fun napojVstup(stav: AppStav, onTon: (midi: Int, zMikrofonu: Boolean) -> Unit): (Int) -> Unit {
    val aktualniOnTon by rememberUpdatedState(onTon)
    val poslouchat = stav.hrajeNaPiano && stav.mikrofonPovolen

    DisposableEffect(poslouchat) {
        if (poslouchat) stav.mikrofon.spust() else stav.mikrofon.zastav()
        onDispose { stav.mikrofon.zastav() }
    }
    LaunchedEffect(poslouchat) {
        if (poslouchat) stav.mikrofon.udery.collect {
            // hlas předčítání by mikrofon mohl slyšet jako tón
            if (!stav.predcitac.mluvi) aktualniOnTon(it, true)
        }
    }
    return { midi ->
        // Tón z reproduktoru tabletu by mikrofon slyšel jako zahraný – chvíli ho ignorujeme.
        stav.mikrofon.ignoruj(700)
        stav.syntezator.zahraj(midi)
        aktualniOnTon(midi, false)
    }
}

/** Ukazatel mikrofonu napojený na stav aplikace; ťuknutím se přepíná piano / displej. */
@Composable
fun MikrofonPrepinac(stav: AppStav, onPozadatOMikrofon: () -> Unit) {
    val detektor by stav.mikrofon.stav.collectAsState()
    val posloucha = stav.hrajeNaPiano && stav.mikrofonPovolen
    MikrofonUkazatel(
        posloucha = posloucha,
        hlasitost = detektor.hlasitost,
        slysim = detektor.midi?.let { Noty.nazevSOktavou(it) },
    ) {
        if (!stav.hrajeNaPiano && !stav.mikrofonPovolen) {
            stav.prepniVstup(true)
            onPozadatOMikrofon()
        } else {
            stav.prepniVstup(!stav.hrajeNaPiano)
        }
    }
}
