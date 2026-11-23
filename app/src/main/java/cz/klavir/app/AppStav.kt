package cz.klavir.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cz.klavir.app.data.UlozisteProfilu
import cz.klavir.app.zvuk.Mikrofon
import cz.klavir.app.zvuk.Predcitac
import cz.klavir.app.zvuk.Syntezator
import cz.klavir.jadro.Hodnoceni
import cz.klavir.jadro.Noty
import cz.klavir.jadro.Odznak
import cz.klavir.jadro.Profil
import java.time.LocalDate

/** Obrazovky aplikace. */
sealed interface Obrazovka {
    data object Profily : Obrazovka
    data object Domu : Obrazovka
    data class Lekce(val id: String) : Obrazovka
    data class Pisnicka(val id: String) : Obrazovka
    data class Vysledek(
        val nazev: String,
        val podnazev: String,
        val hodnoceni: Hodnoceni,
        val noveOdznaky: List<Odznak>,
        val znovu: Obrazovka,
        val dalsi: Obrazovka?,
    ) : Obrazovka
    data object Odznaky : Obrazovka
    data object ProRodice : Obrazovka
}

/**
 * Stav celé aplikace: profily, aktuální obrazovka a zvukové služby.
 * Hodnoty v mutableStateOf automaticky překreslí obrazovku při změně.
 */
class AppStav(context: Context) {
    val uloziste = UlozisteProfilu(context)
    val mikrofon = Mikrofon()
    val syntezator = Syntezator().apply { pripravit(Noty.C4 - 12..Noty.C4 + 12) }
    val predcitac = Predcitac(context)
    private val nastaveni = context.getSharedPreferences("nastaveni", Context.MODE_PRIVATE)

    var profily by mutableStateOf(uloziste.nacti())
        private set
    var aktivniId by mutableStateOf<String?>(null)
        private set
    val aktivni: Profil? get() = profily.firstOrNull { it.id == aktivniId }

    var obrazovka by mutableStateOf<Obrazovka>(Obrazovka.Profily)
        private set
    private val historie = ArrayDeque<Obrazovka>()

    /** true = poslouchat skutečné piano mikrofonem; false = jen ťukání na displej. */
    var hrajeNaPiano by mutableStateOf(nastaveni.getBoolean("piano", true))
        private set

    /** Bylo uděleno oprávnění k mikrofonu? Nastavuje MainActivity. */
    var mikrofonPovolen by mutableStateOf(false)

    fun prepniVstup(piano: Boolean) {
        hrajeNaPiano = piano
        nastaveni.edit()?.putBoolean("piano", piano)?.apply()
    }

    fun jdi(kam: Obrazovka) {
        predcitac.zastav()
        historie.addLast(obrazovka)
        obrazovka = kam
    }

    /** Přejde na obrazovku a vymaže historii (např. po výsledku zpět domů). */
    fun jdiNaHlavni(kam: Obrazovka) {
        predcitac.zastav()
        historie.clear()
        if (kam != Obrazovka.Profily) historie.addLast(Obrazovka.Profily)
        obrazovka = kam
    }

    /** Krok zpět; vrací false, když už není kam (aplikace se zavře). */
    fun zpet(): Boolean {
        predcitac.zastav()
        val predchozi = historie.removeLastOrNull() ?: return false
        obrazovka = if (predchozi is Obrazovka.Vysledek) Obrazovka.Domu else predchozi
        return true
    }

    fun vyberProfil(id: String) {
        aktivniId = id
        uloziste.posledniProfil = id
        jdi(Obrazovka.Domu)
    }

    fun pridejProfil(jmeno: String, zvire: String) {
        val p = uloziste.novy(jmeno, zvire)
        ulozProfily(profily + p)
        vyberProfil(p.id)
    }

    fun upravProfil(p: Profil) = ulozProfily(profily.map { if (it.id == p.id) p else it })

    fun smazProfil(id: String) {
        ulozProfily(profily.filterNot { it.id == id })
        if (aktivniId == id) {
            aktivniId = null
            jdiNaHlavni(Obrazovka.Profily)
        }
    }

    private fun ulozProfily(novy: List<Profil>) {
        profily = novy
        uloziste.uloz(novy)
    }

    /** Uloží výsledek a ukáže obrazovku s hvězdičkami. */
    fun dokonceno(
        nazev: String,
        lekceId: String?,
        pisnickaId: String?,
        h: Hodnoceni,
        znovu: Obrazovka,
        dalsi: Obrazovka?,
        podnazev: String,
    ) {
        val p = aktivni ?: return
        val (novy, odznaky) = p.zapisVysledek(lekceId, pisnickaId, h, LocalDate.now().toEpochDay())
        upravProfil(novy)
        obrazovka = Obrazovka.Vysledek(nazev, podnazev, h, odznaky, znovu, dalsi)
    }

    /** Přečte text nahlas. */
    fun precti(text: String) = predcitac.precti(text)

    /** Přečte text jen pokud má žák zapnuté automatické předčítání. */
    fun prectiAutomaticky(text: String) {
        if (aktivni?.predcitatAutomaticky == true) predcitac.precti(text)
    }

    fun uvolni() {
        mikrofon.zastav()
        predcitac.uvolni()
    }
}
