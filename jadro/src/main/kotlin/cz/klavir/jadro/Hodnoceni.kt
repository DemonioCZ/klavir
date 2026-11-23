package cz.klavir.jadro

import kotlin.math.min

/**
 * Počítá body během jedné lekce nebo písničky.
 *
 * Za správnou notu je 10 bodů a bonus za sérii (každá další správná nota v řadě +1, nejvýš +10).
 * Chyba body nebere, jen přeruší sérii, aby děti neodrazovala.
 */
class Hodnoceni {
    var body = 0; private set
    var spravne = 0; private set
    var chyby = 0; private set
    var serie = 0; private set
    var nejdelsiSerie = 0; private set
    var zMikrofonu = 0; private set

    /** Zaznamená správnou notu a vrátí, kolik bodů přibylo. */
    fun spravne(zMikrofonu: Boolean = false): Int {
        spravne++
        serie++
        if (serie > nejdelsiSerie) nejdelsiSerie = serie
        if (zMikrofonu) this.zMikrofonu++
        val pridano = BODY_ZA_NOTU + min(serie - 1, MAX_BONUS)
        body += pridano
        return pridano
    }

    fun chyba() {
        chyby++
        serie = 0
    }

    /** Přesnost 0.0–1.0 (počítá se každý pokus). */
    val presnost: Double
        get() = if (spravne + chyby == 0) 0.0 else spravne.toDouble() / (spravne + chyby)

    /** Hvězdičky po dokončení: 3 = skoro bez chyby, 2 = dobře, 1 = dokončeno. */
    fun hvezdy(): Int = when {
        presnost >= 0.95 -> 3
        presnost >= 0.8 -> 2
        else -> 1
    }

    companion object {
        const val BODY_ZA_NOTU = 10
        const val MAX_BONUS = 10
    }
}
