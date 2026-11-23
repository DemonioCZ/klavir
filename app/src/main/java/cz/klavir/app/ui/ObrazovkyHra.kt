package cz.klavir.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.klavir.app.AppStav
import cz.klavir.app.Obrazovka
import cz.klavir.jadro.Hodnoceni
import cz.klavir.jadro.Lekce1az5
import cz.klavir.jadro.Noty
import cz.klavir.jadro.Pisnicky
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Krátká zpětná vazba po zahrání tónu. */
private data class Odezva(val text: String, val podtext: String, val dobre: Boolean)

private val POCHVALY = listOf("Výborně!", "Super!", "Paráda!", "Přesně tak!", "Skvělé!")

// ───────────────────────── Lekce ─────────────────────────

@Composable
fun ObrazovkaLekce(stav: AppStav, id: String, onPozadatOMikrofon: () -> Unit) {
    val lekce = Lekce1az5.podleId(id) ?: return
    val h = remember(id) { Hodnoceni() }
    var index by remember(id) { mutableIntStateOf(0) }
    var odezva by remember(id) { mutableStateOf<Odezva?>(null) }
    var chybaVUkolu by remember(id) { mutableStateOf(false) }
    var prechod by remember(id) { mutableStateOf(false) }
    var spatnaKlavesa by remember(id) { mutableStateOf<Int?>(null) }
    val rozsah = rememberCoroutineScope()
    val ukol = lekce.ukoly[index.coerceAtMost(lekce.ukoly.lastIndex)]

    // Zadání se čte jen tehdy, když se změnila nota i text (stejná nota ani stále stejná věta se neopakují).
    LaunchedEffect(id, index) {
        val pred = lekce.ukoly.getOrNull(index - 1)
        if (pred == null || pred.cil != ukol.cil && pred.text != ukol.text) stav.prectiAutomaticky(ukol.text)
    }

    val vstup = napojVstup(stav) { midi, zMikrofonu ->
        if (prechod) return@napojVstup
        if (Noty.stejnyNazev(midi, ukol.cil)) {
            val body = h.spravne(zMikrofonu)
            odezva = Odezva(POCHVALY[index % POCHVALY.size] + " To je ${Noty.nazev(ukol.cil)}", "+$body bodů", true)
            spatnaKlavesa = null
            prechod = true
            rozsah.launch {
                delay(800)
                if (index + 1 >= lekce.ukoly.size) {
                    val dalsi = Lekce1az5.vsechny.getOrNull(lekce.cislo)?.let { Obrazovka.Lekce(it.id) }
                    stav.dokonceno(lekce.nazev, lekce.id, null, h, Obrazovka.Lekce(lekce.id), dalsi ?: Obrazovka.Domu, "Lekce ${lekce.cislo}")
                } else {
                    index++
                    odezva = null
                    chybaVUkolu = false
                    prechod = false
                }
            }
        } else {
            if (!chybaVUkolu) h.chyba()
            chybaVUkolu = true
            spatnaKlavesa = midi
            odezva = Odezva("Zkus to znovu", "To bylo ${Noty.nazev(midi)}. Hledáme ${if (ukol.ukazNotu && !ukol.ukazNazvyKlaves) "jinou notu" else Noty.nazev(ukol.cil)}.", false)
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 20.dp)) {
        HorniLista("Lekce ${lekce.cislo} · ${lekce.nazev}", onZpet = { stav.zpet() }) {
            MikrofonPrepinac(stav, onPozadatOMikrofon)
            if (h.serie >= 2) Pilulka("Série ×${h.serie} 🔥")
            Pilulka("🎵 +${h.body}")
        }
        Postup(index.toFloat() / lekce.ukoly.size, Modifier.fillMaxWidth().padding(top = 8.dp))
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // obrázek vlevo
            Box(Modifier.width(520.dp).fillMaxHeight().karta(), contentAlignment = Alignment.Center) {
                when {
                    lekce.notyVOsnove.isNotEmpty() -> {
                        val barva = if (odezva?.dobre == true) Barvy.zelena else Barvy.prst(ukol.prst ?: 3)
                        OsnovaVyberNot(lekce.notyVOsnove, ukol.cil, barva, Modifier.fillMaxSize().padding(12.dp))
                    }
                    ukol.ukazNotu -> {
                        val barva = if (odezva?.dobre == true) Barvy.zelena else Barvy.text
                        // V lekci „Noty a klávesy“ je písmeno rovnou pod notou.
                        val pismenoPodNotou = lekce.id == "noty_klavesy"
                        OsnovaJednaNota(ukol.cil, barva, Modifier.fillMaxSize().padding(12.dp), pismeno = if (pismenoPodNotou) Noty.nazev(ukol.cil) else null)
                        if (!pismenoPodNotou && (odezva?.dobre == true || ukol.ukazNazvyKlaves && chybaVUkolu)) {
                            Text(Noty.nazev(ukol.cil), style = styl(48, barva = Barvy.zelena), modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
                        }
                    }
                    ukol.prst != null -> Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Ruka(prava = false, aktivniPrst = null, velikost = 190.dp)
                            Text("levá ruka", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Ruka(prava = true, aktivniPrst = ukol.prst, velikost = 190.dp)
                            Text("pravá ruka", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                        }
                    }
                    else -> Text(Noty.nazev(ukol.cil), style = styl(160, barva = Barvy.modra))
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // u lekcí s notou v osnově se nápověda ukáže až po chybě
                val napoveda = if (chybaVUkolu || ukol.ukazNazvyKlaves && !ukol.ukazNotu) ukol.napoveda else ""
                ZadaniKarta(
                    nadpis = "Úkol ${index + 1} z ${lekce.ukoly.size}",
                    text = ukol.text,
                    napoveda = napoveda,
                    modifier = Modifier.fillMaxWidth(),
                    barvaTextu = Barvy.text,
                ) { stav.precti(ukol.text + ". " + napoveda) }
                OdezvaKarta(odezva) { stav.precti(it) }
            }
        }
        Spacer(Modifier.height(16.dp))
        // klaviatura s nápovědou
        val oznaceni = buildMap {
            if (ukol.prst != null && lekce.id in setOf("prstiky", "pet_prstiku")) {
                listOf(Noty.C4, Noty.D4, Noty.E4, Noty.F4, Noty.G4).forEachIndexed { i, m -> put(m, OznaceniKlavesy(prst = i + 1)) }
            }
            if (ukol.zvyraznitKlavesu) put(ukol.cil, OznaceniKlavesy(Barvy.prst(ukol.prst ?: 3), ukol.prst, Color.White, tmavyPopis = true))
            spatnaKlavesa?.let { put(it, OznaceniKlavesy(Barvy.oranzovaSvetla, obrys = Barvy.oranzova)) }
            if (odezva?.dobre == true) put(ukol.cil, OznaceniKlavesy(Barvy.zelena, tmavyPopis = true))
        }
        Klaviatura(vyska = 250.dp, oznaceni = oznaceni, ukazNazvy = ukol.ukazNazvyKlaves, onKlavesa = vstup)
    }
}

@Composable
private fun OdezvaKarta(odezva: Odezva?, onPrecti: (String) -> Unit) {
    if (odezva == null) return
    Row(
        Modifier
            .fillMaxWidth()
            .karta(
                barva = if (odezva.dobre) Barvy.zelenaSvetla else Barvy.oranzovaSvetla,
                stin = if (odezva.dobre) Color(0xFFBFE8D0) else Color(0xFFF5D9AE),
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(if (odezva.dobre) "🎉" else "🤔", style = styl(48))
        Column(Modifier.weight(1f)) {
            Text(odezva.text, style = styl(30, barva = if (odezva.dobre) Barvy.zelena else Barvy.oranzova))
            Text(odezva.podtext, style = styl(20, tucne = false, barva = Barvy.tlumeny))
        }
        Reproduktor { onPrecti(odezva.text + ". " + odezva.podtext) }
    }
}

// ───────────────────────── Písnička ─────────────────────────

private enum class Rezim { CEKAM, TEMPO }

@Composable
fun ObrazovkaPisnicka(stav: AppStav, id: String, onPozadatOMikrofon: () -> Unit) {
    val pisnicka = Pisnicky.podleId(id) ?: return
    val noty = pisnicka.noty
    var rezim by remember(id) { mutableStateOf(Rezim.CEKAM) }
    var kolo by remember(id) { mutableIntStateOf(0) } // zvýšením se písnička začne znovu
    val h = remember(id, kolo, rezim) { Hodnoceni() }
    val stavy = remember(id, kolo, rezim) { mutableStateListOf<StavNoty>().apply { repeat(noty.size) { add(StavNoty.CEKA) } } }
    var index by remember(id, kolo, rezim) { mutableIntStateOf(0) }
    var chybaVNote by remember(id, kolo, rezim) { mutableStateOf(false) }
    var spatnaKlavesa by remember(id, kolo, rezim) { mutableStateOf<Int?>(null) }
    var ukazka by remember(id) { mutableStateOf<Job?>(null) }
    var tempoBezi by remember(id, kolo, rezim) { mutableStateOf(false) }
    var odpocet by remember(id, kolo, rezim) { mutableStateOf<String?>(null) }
    var hotovo by remember(id, kolo, rezim) { mutableStateOf(false) }
    val rozsah = rememberCoroutineScope()
    val nota = noty[index.coerceIn(0, noty.lastIndex)]
    LaunchedEffect(id, index, kolo, rezim) {
        if (stavy.getOrNull(index) == StavNoty.CEKA && !hotovo) stavy[index] = StavNoty.AKTUALNI
    }

    val msNaDobu = 60_000.0 / pisnicka.tempo

    fun dokonci() {
        if (hotovo) return
        hotovo = true
        val dalsi = Pisnicky.vsechny.getOrNull(Pisnicky.vsechny.indexOf(pisnicka) + 1)
        val p = stav.aktivni
        val dalsiObr: Obrazovka = if (dalsi != null && p != null && p.pisnickaOdemcena(dalsi)) Obrazovka.Pisnicka(dalsi.id) else Obrazovka.Domu
        rozsah.launch {
            delay(600)
            stav.dokonceno(
                pisnicka.nazev, null, pisnicka.id, h, Obrazovka.Pisnicka(pisnicka.id), dalsiObr,
                if (rezim == Rezim.CEKAM) "režim „Počkám na tebe“" else "hra v tempu",
            )
        }
    }

    val textUkolu = "Teď zahraj ${Noty.nazev(nota.midi)} ${Lekce1az5.prstemSlovy(nota.prst)} (${nota.prst})"
    LaunchedEffect(id, index, kolo, rezim) {
        val zmena = index == 0 || noty[index - 1].midi != nota.midi || nota.posun != null
        if (rezim == Rezim.CEKAM && ukazka == null && zmena) stav.prectiAutomaticky(textUkolu + (nota.posun?.let { ". $it" } ?: ""))
    }

    val vstup = napojVstup(stav) { midi, zMikrofonu ->
        if (hotovo || ukazka != null) return@napojVstup
        when (rezim) {
            Rezim.CEKAM -> {
                if (Noty.stejnyNazev(midi, nota.midi)) {
                    h.spravne(zMikrofonu)
                    stavy[index] = if (chybaVNote) StavNoty.S_CHYBOU else StavNoty.SPRAVNE
                    spatnaKlavesa = null
                    chybaVNote = false
                    if (index + 1 >= noty.size) dokonci() else index++
                } else {
                    if (!chybaVNote) h.chyba()
                    chybaVNote = true
                    spatnaKlavesa = midi
                }
            }
            Rezim.TEMPO -> {
                if (!tempoBezi) return@napojVstup
                if (stavy[index] == StavNoty.AKTUALNI && Noty.stejnyNazev(midi, nota.midi)) {
                    h.spravne(zMikrofonu)
                    stavy[index] = StavNoty.SPRAVNE
                    spatnaKlavesa = null
                } else if (!Noty.stejnyNazev(midi, nota.midi)) {
                    spatnaKlavesa = midi
                }
            }
        }
    }

    fun spustTempo() {
        rozsah.launch {
            for (t in listOf("3", "2", "1")) {
                odpocet = t
                stav.syntezator.zahraj(Noty.C4 + 24)
                stav.mikrofon.ignoruj(400)
                delay((msNaDobu / 0.8).toLong())
            }
            odpocet = null
            tempoBezi = true
            for (i in noty.indices) {
                index = i
                if (stavy[i] == StavNoty.CEKA) stavy[i] = StavNoty.AKTUALNI
                delay((noty[i].doby * msNaDobu / 0.8).toLong()) // dětské tempo: o 20 % pomaleji
                if (stavy[i] == StavNoty.AKTUALNI) {
                    stavy[i] = StavNoty.S_CHYBOU
                    h.chyba()
                }
            }
            tempoBezi = false
            dokonci()
        }
    }

    fun prehrajUkazku() {
        ukazka?.cancel()
        ukazka = rozsah.launch {
            try {
                for (i in noty.indices) {
                    index = i
                    stav.mikrofon.ignoruj(1200)
                    stav.syntezator.zahraj(noty[i].midi)
                    delay((noty[i].doby * msNaDobu).toLong())
                }
            } finally {
                index = 0
                ukazka = null
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 20.dp)) {
        HorniLista(pisnicka.nazev, onZpet = { ukazka?.cancel(); stav.zpet() }) {
            Pilulka(
                if (rezim == Rezim.CEKAM) "🐢 Počkám na tebe" else "🐇 V tempu",
                velikost = 18,
            ) {
                ukazka?.cancel()
                rezim = if (rezim == Rezim.CEKAM) Rezim.TEMPO else Rezim.CEKAM
            }
            Pilulka(if (ukazka != null) "⏹ Zastavit" else "👂 Poslechni si", velikost = 18) {
                if (ukazka != null) ukazka?.cancel() else prehrajUkazku()
            }
            if (rezim == Rezim.CEKAM && index > 0 && !hotovo) Pilulka("↺ Od začátku", velikost = 18) { kolo++ }
            MikrofonPrepinac(stav, onPozadatOMikrofon)
            if (h.serie >= 2) Pilulka("Série ×${h.serie} 🔥")
            Pilulka("🎵 +${h.body}")
        }
        Postup(stavy.count { it == StavNoty.SPRAVNE || it == StavNoty.S_CHYBOU }.toFloat() / noty.size, Modifier.fillMaxWidth().padding(top = 8.dp))
        Box(Modifier.fillMaxWidth().height(290.dp).padding(top = 12.dp).karta()) {
            OsnovaPisnicky(noty, stavy, index, Modifier.fillMaxSize())
            odpocet?.let {
                Box(Modifier.align(Alignment.Center).clip(RoundedCornerShape(30.dp)).background(Barvy.modra).padding(horizontal = 40.dp, vertical = 6.dp)) {
                    Text(it, style = styl(80, barva = Color.White))
                }
            }
            if (rezim == Rezim.TEMPO && !tempoBezi && odpocet == null && !hotovo) {
                Tlacitko("▶ Start", modifier = Modifier.align(Alignment.Center)) { spustTempo() }
            }
        }
        // klaviatura: poloha ruky s čísly prstů, cílová klávesa v barvě prstu
        val oznaceni = buildMap {
            val bilyPalec = bilaKlavesa(nota.midi, -(nota.prst - 1))
            for (prst in 1..5) bilaKlavesa(bilyPalec, prst - 1).let { put(it, OznaceniKlavesy(prst = prst)) }
            put(nota.midi, OznaceniKlavesy(Barvy.prst(nota.prst), nota.prst, Color.White, tmavyPopis = true))
            spatnaKlavesa?.let { put(it, OznaceniKlavesy(Barvy.oranzovaSvetla, obrys = Barvy.oranzova)) }
        }
        // klávesy vyplní celé zbývající místo pod osnovou
        Klaviatura(Modifier.weight(1f).padding(top = 12.dp), vyska = Dp.Unspecified, oznaceni = oznaceni, onKlavesa = vstup)
    }
}

/** Posune se o [o] bílých kláves od tónu [midi] (záporně = doleva). */
private fun bilaKlavesa(midi: Int, o: Int): Int {
    var m = midi
    var zbyva = o
    while (zbyva != 0) {
        m += if (zbyva > 0) 1 else -1
        if (Noty.jeBila(m)) zbyva += if (zbyva > 0) -1 else 1
    }
    return m
}
