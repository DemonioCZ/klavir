package cz.klavir.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cz.klavir.app.AppStav
import cz.klavir.app.BuildConfigInfo
import cz.klavir.app.Obrazovka
import cz.klavir.app.data.Aktualizace
import cz.klavir.jadro.Lekce1az5
import cz.klavir.jadro.Odznaky
import cz.klavir.jadro.Pisnicky
import cz.klavir.jadro.Profil
import kotlinx.coroutines.launch

// ───────────────────────── Kdo dnes hraje? ─────────────────────────

@Composable
fun ObrazovkaProfily(stav: AppStav) {
    var novyDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { if (stav.profily.isEmpty()) novyDialog = true }

    Box(Modifier.fillMaxSize().padding(32.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            Sova(velikost = 120.dp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Kdo dnes hraje?", style = styl(48))
                Reproduktor { stav.precti("Kdo dnes hraje? Ťukni na svůj obrázek.") }
            }
            Text("Ťukni na svůj obrázek", style = styl(24, tucne = false, barva = Barvy.tlumeny))
            Spacer(Modifier.height(32.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                stav.profily.forEach { p ->
                    KartaProfilu(p.zvire, p.jmeno, "Úroveň ${p.uroven} · ⭐ ${p.hvezdyCelkem}") { stav.vyberProfil(p.id) }
                }
                Column(
                    Modifier
                        .size(240.dp, 290.dp)
                        .border(4.dp, Barvy.linka, RoundedCornerShape(28.dp))
                        .clickable { novyDialog = true },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.size(140.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Text("+", style = styl(72, barva = Barvy.modra))
                    }
                    Text("Nový hráč", style = styl(28))
                    Text("jméno a zvířátko", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                }
            }
        }
    }
    if (novyDialog) {
        DialogNovyProfil(onZrusit = { novyDialog = false }) { jmeno, zvire ->
            novyDialog = false
            stav.pridejProfil(jmeno, zvire)
        }
    }
}

@Composable
private fun KartaProfilu(zvire: String, jmeno: String, info: String, onClick: () -> Unit) {
    Column(
        Modifier.size(240.dp, 290.dp).karta().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(140.dp).clip(CircleShape).background(Barvy.zlutaSvetla), contentAlignment = Alignment.Center) {
            Text(zvire, style = styl(80))
        }
        Spacer(Modifier.height(8.dp))
        Text(jmeno, style = styl(32))
        Text(info, style = styl(18, tucne = false, barva = Barvy.tlumeny))
    }
}

@Composable
fun DialogNovyProfil(
    puvodniJmeno: String = "",
    puvodniZvire: String = Profil.ZVIRATKA.first(),
    nadpis: String = "Nový hráč",
    onZrusit: () -> Unit,
    onUlozit: (String, String) -> Unit,
) {
    var jmeno by remember { mutableStateOf(puvodniJmeno) }
    var zvire by remember { mutableStateOf(puvodniZvire) }
    Dialog(onDismissRequest = onZrusit) {
        Column(Modifier.width(620.dp).karta().padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(nadpis, style = styl(34))
            Text("Jak se jmenuješ?", style = styl(20, tucne = false, barva = Barvy.tlumeny))
            BasicTextField(
                value = jmeno,
                onValueChange = { jmeno = it.take(20) },
                singleLine = true,
                textStyle = styl(30),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Barvy.linka, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
            Text("Vyber si zvířátko", style = styl(20, tucne = false, barva = Barvy.tlumeny))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Profil.ZVIRATKA.chunked(6).forEach { radek ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        radek.forEach { z ->
                            Box(
                                Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(if (z == zvire) Barvy.zlutaSvetla else Color(0xFFF6F1E6))
                                    .then(if (z == zvire) Modifier.border(4.dp, Barvy.zluta, CircleShape) else Modifier)
                                    .clickable { zvire = z },
                                contentAlignment = Alignment.Center,
                            ) { Text(z, style = styl(44)) }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.align(Alignment.End)) {
                Tlacitko("Zrušit", hlavni = false, onClick = onZrusit)
                Tlacitko("Hotovo", povoleno = jmeno.isNotBlank()) { onUlozit(jmeno, zvire) }
            }
        }
    }
}

// ───────────────────────── Domů ─────────────────────────

@Composable
fun ObrazovkaDomu(stav: AppStav) {
    val p = stav.aktivni ?: return
    val dalsiLekce = Lekce1az5.vsechny.firstOrNull { (p.hvezdyLekci[it.id] ?: 0) == 0 && p.lekceOdemcena(it) }
    LaunchedEffect(p.id) {
        stav.prectiAutomaticky("Ahoj ${p.jmeno}! " + (dalsiLekce?.let { "Dnes tě čeká lekce ${it.nazev}." } ?: "Vyber si písničku."))
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Barvy.zlutaSvetla).clickable { stav.jdiNaHlavni(Obrazovka.Profily) },
                contentAlignment = Alignment.Center,
            ) { Text(p.zvire, style = styl(38)) }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ahoj, ${p.jmeno}!", style = styl(34))
                    Reproduktor { stav.precti("Ahoj ${p.jmeno}! " + (dalsiLekce?.let { "Dnes tě čeká lekce ${it.nazev}." } ?: "Vyber si písničku.")) }
                }
                Text(
                    dalsiLekce?.let { "Dnes tě čeká lekce „${it.nazev}“" } ?: "Všechny lekce máš hotové – zahraj si písničku!",
                    style = styl(20, tucne = false, barva = Barvy.tlumeny),
                )
            }
            Spacer(Modifier.weight(1f))
            if (p.dnuVRade > 0) Pilulka("🔥 ${p.dnuVRade} ${dny(p.dnuVRade)} v řadě")
            Pilulka("⭐ ${p.hvezdyCelkem}")
            Pilulka("🎵 ${p.body} bodů")
        }
        Spacer(Modifier.height(18.dp))
        Text("Lekce", style = styl(26))
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Lekce1az5.vsechny.forEach { l ->
                val hvezdy = p.hvezdyLekci[l.id] ?: 0
                val odemcena = p.lekceOdemcena(l)
                val aktualni = l == dalsiLekce
                Column(
                    Modifier
                        .size(228.dp, 186.dp)
                        .alpha(if (odemcena) 1f else 0.5f)
                        .then(if (aktualni) Modifier.border(5.dp, Barvy.modra, RoundedCornerShape(28.dp)) else Modifier)
                        .karta()
                        .clickable(enabled = odemcena) { stav.jdi(Obrazovka.Lekce(l.id)) }
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text(l.obrazek, style = styl(42))
                    Text("Lekce ${l.cislo}", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                    Text(l.nazev, style = styl(23))
                    Spacer(Modifier.weight(1f))
                    Text(
                        when {
                            !odemcena -> "🔒"
                            hvezdy > 0 -> "⭐".repeat(hvezdy)
                            aktualni -> "▶ teď hraješ"
                            else -> "nová!"
                        },
                        style = styl(20, barva = if (aktualni && hvezdy == 0) Barvy.modra else Barvy.text),
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Písničky", style = styl(26))
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Pisnicky.vsechny.forEach { s ->
                val odemcena = p.pisnickaOdemcena(s)
                val hvezdy = p.hvezdyPisnicek[s.id] ?: 0
                Row(
                    Modifier
                        .size(290.dp, 150.dp)
                        .alpha(if (odemcena) 1f else 0.5f)
                        .karta()
                        .clickable(enabled = odemcena) { stav.jdi(Obrazovka.Pisnicka(s.id)) }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(s.obrazek, style = styl(60))
                    Column {
                        Text(s.nazev, style = styl(25))
                        Text(
                            when {
                                !odemcena -> "🔒 za ${s.potrebaHvezd} ⭐"
                                hvezdy > 0 -> "⭐".repeat(hvezdy)
                                else -> "nová!"
                            },
                            style = styl(20, tucne = hvezdy > 0, barva = if (hvezdy > 0) Barvy.text else Barvy.tlumeny),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Tlacitko("🏅 Odznaky", hlavni = false) { stav.jdi(Obrazovka.Odznaky) }
            Tlacitko("⚙️ Pro rodiče", hlavni = false) { stav.jdi(Obrazovka.ProRodice) }
            val pokracovat: Obrazovka = dalsiLekce?.let { Obrazovka.Lekce(it.id) }
                ?: Pisnicky.vsechny.firstOrNull { p.pisnickaOdemcena(it) && (p.hvezdyPisnicek[it.id] ?: 0) < 3 }?.let { Obrazovka.Pisnicka(it.id) }
                ?: Obrazovka.Pisnicka(Pisnicky.skakalPes.id)
            Tlacitko("▶ Pokračovat") { stav.jdi(pokracovat) }
        }
    }
}

private fun dny(n: Int) = when (n) { 1 -> "den"; in 2..4 -> "dny"; else -> "dní" }

// ───────────────────────── Výsledek ─────────────────────────

@Composable
fun ObrazovkaVysledek(stav: AppStav, v: Obrazovka.Vysledek) {
    val h = v.hodnoceni
    val hvezdy = h.hvezdy()
    val pochvala = when (hvezdy) { 3 -> "Skvěle zahráno!"; 2 -> "Moc dobře!"; else -> "Hotovo! Příště to bude ještě lepší." }
    LaunchedEffect(Unit) {
        stav.prectiAutomaticky("$pochvala Máš $hvezdy ${if (hvezdy == 1) "hvězdičku" else "hvězdičky"}.")
    }
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Sova(velikost = 240.dp)
        Spacer(Modifier.width(40.dp))
        Column(
            Modifier.width(660.dp).karta().padding(horizontal = 40.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("⭐".repeat(hvezdy) + "☆".repeat(3 - hvezdy), style = styl(60))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pochvala, style = styl(40), textAlign = TextAlign.Center)
                Reproduktor { stav.precti("$pochvala Máš $hvezdy ${if (hvezdy == 1) "hvězdičku" else "hvězdičky"}.") }
            }
            Text("${v.nazev} · ${v.podnazev}", style = styl(22, tucne = false, barva = Barvy.tlumeny))
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Cislo("${h.spravne}/${h.spravne + h.chyby}", "správně", Barvy.zelena)
                Cislo("${h.nejdelsiSerie}", "nejdelší série", Barvy.oranzova)
                Cislo("+${h.body}", "bodů", Barvy.modra)
            }
            v.noveOdznaky.forEach { o ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(20.dp)).background(Barvy.zlutaSvetla).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(o.obrazek, style = styl(44))
                    Column {
                        Text("Nový odznak: ${o.nazev}!", style = styl(24))
                        Text(o.popis, style = styl(18, tucne = false, barva = Barvy.tlumeny))
                    }
                }
            }
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Tlacitko("🏠", hlavni = false) { stav.jdiNaHlavni(Obrazovka.Domu) }
                Tlacitko("↻ Znovu", hlavni = false) { stav.jdiNaHlavni(v.znovu) }
                if (v.dalsi != null) Tlacitko("Další ›") { stav.jdiNaHlavni(v.dalsi) }
                else Tlacitko("Domů") { stav.jdiNaHlavni(Obrazovka.Domu) }
            }
        }
    }
}

@Composable
private fun Cislo(hodnota: String, popis: String, barva: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(hodnota, style = styl(40, barva = barva))
        Text(popis, style = styl(18, tucne = false, barva = Barvy.tlumeny))
    }
}

// ───────────────────────── Odznaky ─────────────────────────

@Composable
fun ObrazovkaOdznaky(stav: AppStav) {
    val p = stav.aktivni ?: return
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 22.dp)) {
        HorniLista("Moje odznaky", onZpet = { stav.zpet() }) {
            Pilulka("🏅 ${p.odznaky.size} z ${Odznaky.vsechny.size}")
            Pilulka("🎵 ${p.body} bodů")
        }
        Spacer(Modifier.height(18.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(Odznaky.vsechny) { o ->
                val ma = o.id in p.odznaky
                Column(
                    Modifier.height(190.dp).alpha(if (ma) 1f else 0.45f).karta().clickable { stav.precti(o.nazev + ". " + o.popis) }.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(if (ma) Barvy.zlutaSvetla else Color(0xFFEEEEEE))
                            .then(if (ma) Modifier.border(5.dp, Barvy.zluta, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) { Text(if (ma) o.obrazek else "🔒", style = styl(48)) }
                    Text(o.nazev, style = styl(19), textAlign = TextAlign.Center)
                    Text(o.popis, style = styl(13, tucne = false, barva = Barvy.tlumeny), textAlign = TextAlign.Center)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().karta().padding(horizontal = 26.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Do další úrovně", style = styl(22))
            Postup(p.postupUrovne, Modifier.weight(1f), Barvy.zluta)
            Text("Úroveň ${p.uroven} · ${p.nazevUrovne}", style = styl(22))
        }
    }
}

// ───────────────────────── Pro rodiče (nastavení) ─────────────────────────

@Composable
fun ObrazovkaProRodice(stav: AppStav) {
    val p = stav.aktivni ?: return
    val kontext = LocalContext.current
    val rozsah = rememberCoroutineScope()
    var upravit by remember { mutableStateOf(false) }
    var potvrditSmazani by remember { mutableStateOf(false) }
    var aktualizace by remember { mutableStateOf("") }
    var nalezena by remember { mutableStateOf<Aktualizace.Info?>(null) }
    var stahuje by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 22.dp)) {
        HorniLista("Pro rodiče", onZpet = { stav.zpet() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Profil žáka
            Column(Modifier.fillMaxWidth().karta().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Profil žáka", style = styl(26))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(p.zvire, style = styl(56))
                    Column(Modifier.weight(1f)) {
                        Text(p.jmeno, style = styl(30))
                        Text("Úroveň ${p.uroven} · ${p.body} bodů · ⭐ ${p.hvezdyCelkem}", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                    }
                    Tlacitko("Upravit jméno a zvířátko", hlavni = false, velikost = 20) { upravit = true }
                }
                Radek(
                    "Automaticky předčítat texty",
                    "Pro děti, které ještě neumí číst. Zadání se přečte samo, jakmile se objeví.",
                ) {
                    Switch(checked = p.predcitatAutomaticky, onCheckedChange = { stav.upravProfil(p.copy(predcitatAutomaticky = it)) })
                }
                if (stav.predcitac.pripraven && !stav.predcitac.dostupny) {
                    Text(
                        "Český hlas pro předčítání není v tabletu nainstalovaný. Doinstalujete ho v Nastavení Androidu → Přístupnost → Převod textu na řeč.",
                        style = styl(16, tucne = false, barva = Barvy.oranzova),
                    )
                }
                Tlacitko("Smazat profil", hlavni = false, velikost = 18) { potvrditSmazani = true }
            }
            // Ovládání
            Column(Modifier.fillMaxWidth().karta().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Hra", style = styl(26))
                Radek(
                    "Poslouchat skutečné piano (mikrofon)",
                    "Když je vypnuto, hraje se jen ťukáním na klávesy na displeji.",
                ) {
                    Switch(checked = stav.hrajeNaPiano, onCheckedChange = { stav.prepniVstup(it) })
                }
            }
            // Aktualizace
            Column(Modifier.fillMaxWidth().karta().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Aktualizace aplikace", style = styl(26))
                Text("Nainstalovaná verze: ${BuildConfigInfo.nazevVerze(kontext)}", style = styl(18, tucne = false, barva = Barvy.tlumeny))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Tlacitko("Zkontrolovat aktualizace", hlavni = nalezena == null, velikost = 20, povoleno = !stahuje) {
                        aktualizace = "Hledám novou verzi…"
                        nalezena = null
                        rozsah.launch {
                            aktualizace = try {
                                val info = Aktualizace.zkontroluj(BuildConfigInfo.cisloVerze(kontext))
                                nalezena = info
                                if (info == null) "Máte nejnovější verzi." else "Je k dispozici nová verze ${info.nazev}."
                            } catch (e: Exception) {
                                "Nepodařilo se spojit s GitHubem. Je tablet připojený k internetu?"
                            }
                        }
                    }
                    val info = nalezena
                    if (info != null) {
                        Tlacitko("Stáhnout a nainstalovat", velikost = 20, povoleno = !stahuje) {
                            if (!Aktualizace.muzeInstalovat(kontext)) {
                                aktualizace = "Nejdřív povolte instalaci aplikací z Klavírního samouka a pak klepněte znovu."
                                Aktualizace.otevriPovoleni(kontext)
                            } else {
                                stahuje = true
                                rozsah.launch {
                                    try {
                                        val apk = Aktualizace.stahni(kontext, info.urlApk) { aktualizace = "Stahuji… ${(it * 100).toInt()} %" }
                                        aktualizace = "Staženo, spouštím instalaci."
                                        Aktualizace.nainstaluj(kontext, apk)
                                    } catch (e: Exception) {
                                        aktualizace = "Stahování se nepovedlo: ${e.message}"
                                    } finally {
                                        stahuje = false
                                    }
                                }
                            }
                        }
                    }
                }
                if (aktualizace.isNotEmpty()) Text(aktualizace, style = styl(18, tucne = false))
                nalezena?.poznamky?.takeIf { it.isNotBlank() }?.let { Text(it, style = styl(16, tucne = false, barva = Barvy.tlumeny)) }
            }
            Text(
                "Postup žáků se ukládá jen v tomto tabletu. Melodie písniček jsou lidové (volné dílo).",
                style = styl(16, tucne = false, barva = Barvy.tlumeny),
            )
        }
    }
    if (upravit) {
        DialogNovyProfil(p.jmeno, p.zvire, "Upravit profil", onZrusit = { upravit = false }) { jmeno, zvire ->
            upravit = false
            stav.upravProfil(p.copy(jmeno = jmeno.trim(), zvire = zvire))
        }
    }
    if (potvrditSmazani) {
        Dialog(onDismissRequest = { potvrditSmazani = false }) {
            Column(Modifier.width(520.dp).karta().padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Smazat profil ${p.jmeno}?", style = styl(30))
                Text("Smažou se i body, hvězdičky a odznaky. Nedá se to vrátit.", style = styl(20, tucne = false))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.align(Alignment.End)) {
                    Tlacitko("Ponechat", hlavni = false) { potvrditSmazani = false }
                    Tlacitko("Smazat") {
                        potvrditSmazani = false
                        stav.smazProfil(p.id)
                    }
                }
            }
        }
    }
}

@Composable
private fun Radek(nadpis: String, popis: String, ovladac: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(nadpis, style = styl(22))
            Text(popis, style = styl(17, tucne = false, barva = Barvy.tlumeny))
        }
        ovladac()
    }
}
