package cz.klavir.jadro

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pomocné funkce pro práci s tóny.
 *
 * Tóny označujeme čísly MIDI: 60 = c1 (střední C na klavíru), 61 = cis1, 62 = d1 …
 * České názvy používají H místo anglického B.
 */
object Noty {
    const val C4 = 60
    const val D4 = 62
    const val E4 = 64
    const val F4 = 65
    const val G4 = 67
    const val A4 = 69
    const val H4 = 71
    const val C5 = 72

    private val NAZVY = arrayOf("C", "Cis", "D", "Dis", "E", "F", "Fis", "G", "Gis", "A", "B", "H")

    /** Český název tónu bez oktávy, např. 64 → „E“. */
    fun nazev(midi: Int): String = NAZVY[((midi % 12) + 12) % 12]

    /** Název s číslem oktávy jako na ladičce, např. 64 → „E4“. */
    fun nazevSOktavou(midi: Int): String = nazev(midi) + (midi / 12 - 1)

    /** Je to bílá klávesa? */
    fun jeBila(midi: Int): Boolean = (((midi % 12) + 12) % 12) in intArrayOf(0, 2, 4, 5, 7, 9, 11)

    /** Frekvence tónu v Hz (ladění a1 = 440 Hz). */
    fun frekvence(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    /** Nejbližší tón k zadané frekvenci. */
    fun zFrekvence(hz: Double): Int = (69 + 12 * log2(hz / 440.0)).roundToInt()

    /** Odchylka frekvence od nejbližšího tónu v centech (−50 až +50). */
    fun odchylkaCentu(hz: Double): Double {
        val presne = 69 + 12 * log2(hz / 440.0)
        return (presne - presne.roundToInt()) * 100
    }

    /**
     * Pozice noty v osnově s houslovým klíčem: 0 = první (spodní) linka = E4,
     * 1 = první mezera = F4, 2 = druhá linka = G4 … −2 = C4 s pomocnou linkou.
     * Pro černé klávesy vrací pozici bílé klávesy pod ní (kreslí se s křížkem).
     */
    fun poziceVOsnove(midi: Int): Int {
        val stupne = intArrayOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6) // C C# D D# E F F# G G# A A# H
        val oktava = midi / 12 - 1
        val diatonicky = oktava * 7 + stupne[((midi % 12) + 12) % 12]
        val e4 = 4 * 7 + 2
        return diatonicky - e4
    }

    /** Stejný název noty (bez ohledu na oktávu)? */
    fun stejnyNazev(a: Int, b: Int): Boolean = ((a - b) % 12 + 12) % 12 == 0
}
