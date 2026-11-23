package cz.klavir.jadro

import kotlin.math.sqrt

/**
 * Z proudu oken zvuku pozná, kdy žák udeřil do klávesy a jaký tón to byl.
 *
 * Proč je to potřeba: piano zní ještě dlouho po úderu (dozvuk), takže nestačí
 * hlásit „slyším G“ v každém okně. Úder hlásíme jen tehdy, když:
 *  - je tón stabilní aspoň [stabilnichOken] oken za sebou, a zároveň
 *  - je to jiný tón než naposledy nahlášený, NEBO hlasitost znovu prudce stoupla
 *    (stejná klávesa zahraná znovu, např. „G G“ ve Skákal pes).
 *
 * Třída je čistý Kotlin, takže ji lze testovat bez mikrofonu.
 */
class DetektorUderu(
    private val yin: Yin,
    private val minHlasitost: Float = 0.01f,
    private val minJistota: Float = 0.8f,
    private val stabilnichOken: Int = 2,
    private val narustProOpakovani: Float = 1.8f,
) {
    /** Co detektor vidí v aktuálním okně (pro ukazatel na obrazovce). */
    data class Stav(val hlasitost: Float, val midi: Int?, val hz: Double)

    var posledniStav = Stav(0f, null, 0.0); private set

    private var kandidat: Int? = null
    private var kandidatPocet = 0
    private var nahlaseny: Int? = null
    private var vrchol = 0f
    private var minimumPoVrcholu = Float.MAX_VALUE
    private var oknaTicha = 0

    /**
     * Zpracuje jedno okno vzorků (hodnoty −1..1).
     * @return tón MIDI, pokud právě nastal nový úder, jinak null
     */
    fun zpracuj(okno: FloatArray): Int? {
        val hlasitost = rms(okno)

        if (hlasitost < minHlasitost) {
            oknaTicha++
            if (oknaTicha >= 3) {
                // Dlouhé ticho: další stejný tón už je nový úder.
                nahlaseny = null
                kandidat = null
                kandidatPocet = 0
            }
            sledujObalku(hlasitost)
            posledniStav = Stav(hlasitost, null, 0.0)
            return null
        }
        oknaTicha = 0

        val v = yin.odhadni(okno)
        val midi = if (v.platny && v.jistota >= minJistota) Noty.zFrekvence(v.hz) else null
        posledniStav = Stav(hlasitost, midi, if (midi != null) v.hz else 0.0)

        // Nový úder (i na stejnou klávesu) poznáme tak, že zvuk po předchozím úderu
        // nejdřív zeslábl a pak zase prudce zesílil.
        val doznivalo = minimumPoVrcholu < vrchol * 0.7f
        val novyUder = doznivalo && hlasitost > minimumPoVrcholu * narustProOpakovani
        if (novyUder) {
            kandidat = null
            kandidatPocet = 0
            nahlaseny = null
            vrchol = hlasitost
            minimumPoVrcholu = hlasitost
        } else {
            sledujObalku(hlasitost)
        }

        if (midi == null) return null

        if (midi == kandidat) kandidatPocet++ else {
            kandidat = midi
            kandidatPocet = 1
        }

        if (kandidatPocet >= stabilnichOken && midi != nahlaseny) {
            nahlaseny = midi
            vrchol = hlasitost
            minimumPoVrcholu = hlasitost
            return midi
        }
        return null
    }

    fun reset() {
        kandidat = null
        kandidatPocet = 0
        nahlaseny = null
        vrchol = 0f
        minimumPoVrcholu = Float.MAX_VALUE
        oknaTicha = 0
    }

    /** Sleduje průběh hlasitosti: dokud zvuk sílí, posouvá vrchol; pak hlídá nejnižší bod. */
    private fun sledujObalku(hlasitost: Float) {
        if (hlasitost > vrchol && minimumPoVrcholu >= vrchol * 0.7f) {
            vrchol = hlasitost
            minimumPoVrcholu = hlasitost
        } else {
            minimumPoVrcholu = minOf(minimumPoVrcholu, hlasitost)
        }
    }

    companion object {
        fun rms(okno: FloatArray): Float {
            var s = 0.0
            for (x in okno) s += x * x
            return sqrt(s / okno.size).toFloat()
        }
    }
}
