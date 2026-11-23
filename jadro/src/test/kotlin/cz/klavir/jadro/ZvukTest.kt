package cz.klavir.jadro

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Testy rozpoznání tónu na uměle vytvořeném „klavírním“ zvuku. */
class ZvukTest {
    private val fs = 44100
    private val okno = 2048
    private val krok = 1024

    /** Tón podobný klavíru: základní frekvence + vyšší harmonické, s doznívající obálkou. */
    private fun klavir(midi: Int, sekund: Double, hlasitost: Double = 0.5): FloatArray {
        val f = Noty.frekvence(midi)
        val n = (sekund * fs).toInt()
        val sily = doubleArrayOf(1.0, 0.6, 0.35, 0.2, 0.12, 0.08)
        return FloatArray(n) { i ->
            val t = i.toDouble() / fs
            val nabeh = minOf(1.0, t / 0.005)
            val obalka = nabeh * exp(-t * 2.5)
            var s = 0.0
            for (h in sily.indices) s += sily[h] * sin(2 * PI * f * (h + 1) * t)
            (hlasitost * obalka * s / 2.0).toFloat()
        }
    }

    private fun ticho(sekund: Double) = FloatArray((sekund * fs).toInt())

    private fun spoj(vararg casti: FloatArray): FloatArray {
        val vysledek = FloatArray(casti.sumOf { it.size })
        var o = 0
        for (c in casti) { c.copyInto(vysledek, o); o += c.size }
        return vysledek
    }

    private fun prehraj(signal: FloatArray, d: DetektorUderu): List<Int> {
        val udery = mutableListOf<Int>()
        var i = 0
        while (i + okno <= signal.size) {
            d.zpracuj(signal.copyOfRange(i, i + okno))?.let { udery += it }
            i += krok
        }
        return udery
    }

    @Test
    fun yinPoznaTonyVRozsahuAplikace() {
        val yin = Yin(fs, okno)
        for (midi in 48..84) {
            val s = klavir(midi, 0.2)
            val v = yin.odhadni(s.copyOfRange(2000, 2000 + okno))
            assertTrue(v.platny, "Tón ${Noty.nazevSOktavou(midi)} nebyl rozpoznán")
            assertEquals(midi, Noty.zFrekvence(v.hz), "Špatně rozpoznán ${Noty.nazevSOktavou(midi)}: ${v.hz} Hz")
            assertTrue(abs(Noty.odchylkaCentu(v.hz)) < 10, "Velká odchylka u ${Noty.nazevSOktavou(midi)}")
        }
    }

    @Test
    fun yinNepoznaTonVSumu() {
        val r = Random(1)
        val sum = FloatArray(okno) { (r.nextFloat() - 0.5f) * 0.4f }
        val v = Yin(fs, okno).odhadni(sum)
        assertTrue(!v.platny || v.jistota < 0.8f, "V šumu by neměl být tón")
    }

    @Test
    fun detektorPoznaRuzneTony() {
        val signal = spoj(klavir(67, 0.4), klavir(64, 0.4), klavir(69, 0.4), ticho(0.2))
        assertEquals(listOf(67, 64, 69), prehraj(signal, DetektorUderu(Yin(fs, okno))))
    }

    @Test
    fun detektorPoznaOpakovanouKlavesu() {
        // „Ská-kal pes“: G G E – dvakrát stejná klávesa za sebou bez ticha mezi nimi.
        val signal = spoj(klavir(67, 0.35), klavir(67, 0.35), klavir(64, 0.6), ticho(0.2))
        assertEquals(listOf(67, 67, 64), prehraj(signal, DetektorUderu(Yin(fs, okno))))
    }

    @Test
    fun dlouhyTonSeHlasiJenJednou() {
        val signal = spoj(klavir(60, 1.5), ticho(0.2))
        assertEquals(listOf(60), prehraj(signal, DetektorUderu(Yin(fs, okno))))
    }

    @Test
    fun tichyHlukNicNehlasi() {
        val r = Random(2)
        val signal = FloatArray(fs) { (r.nextFloat() - 0.5f) * 0.01f }
        assertEquals(emptyList(), prehraj(signal, DetektorUderu(Yin(fs, okno))))
    }
}
