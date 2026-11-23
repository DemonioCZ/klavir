package cz.klavir.jadro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HudbaTest {

    @Test
    fun ceskeNazvyNot() {
        assertEquals("C", Noty.nazev(60))
        assertEquals("H", Noty.nazev(71))
        assertEquals("B", Noty.nazev(70))
        assertEquals("A4", Noty.nazevSOktavou(69))
        assertEquals(440.0, Noty.frekvence(69), 1e-9)
    }

    @Test
    fun poziceVOsnove() {
        assertEquals(-2, Noty.poziceVOsnove(60)) // C4 na pomocné lince
        assertEquals(0, Noty.poziceVOsnove(64)) // E4 na první lince
        assertEquals(2, Noty.poziceVOsnove(67)) // G4 na druhé lince
        assertEquals(5, Noty.poziceVOsnove(72)) // C5 ve třetí mezeře
    }

    /** Index bílé klávesy (C4 = 0, D4 = 1 …) – pro kontrolu polohy ruky. */
    private fun bila(midi: Int): Int {
        val st = intArrayOf(0, -1, 1, -1, 2, 3, -1, 4, -1, 5, -1, 6)[midi % 12]
        require(st >= 0) { "Černá klávesa v písničce" }
        return (midi / 12) * 7 + st
    }

    @Test
    fun prstokladDavaSmysl() {
        for (p in Pisnicky.vsechny) {
            var predchoziPoloha: Int? = null
            var predchoziMelaPosun = false
            p.noty.forEachIndexed { i, n ->
                assertTrue(n.prst in 1..5, "${p.nazev}: prst ${n.prst}")
                // Poloha ruky = bílá klávesa pod palcem.
                val poloha = bila(n.midi) - (n.prst - 1)
                if (predchoziPoloha != null && poloha != predchoziPoloha) {
                    assertTrue(
                        n.posun != null || predchoziMelaPosun,
                        "${p.nazev}, nota ${i + 1} (${n.slabika}): ruka se posouvá bez nápovědy",
                    )
                }
                predchoziPoloha = poloha
                predchoziMelaPosun = n.posun != null
            }
        }
    }

    @Test
    fun taktyMajiSpravnyPocetDob() {
        for (p in Pisnicky.vsechny) {
            var doby = 0f
            p.noty.forEachIndexed { i, n ->
                if (n.novyTakt && i > 0) {
                    assertEquals(p.taktDoby.toFloat(), doby, "${p.nazev}: takt před notou ${i + 1}")
                    doby = 0f
                }
                doby += n.doby
            }
            assertEquals(p.taktDoby.toFloat(), doby, "${p.nazev}: poslední takt")
        }
    }

    @Test
    fun melodieOdpovidajiZapisum() {
        fun jmena(p: Pisnicka) = p.noty.joinToString(" ") { Noty.nazev(it.midi) }
        assertEquals(
            "G G E G G E G G A G G F F F D F F D F F G F F E",
            jmena(Pisnicky.skakalPes),
        )
        assertEquals(
            "G G A G G G A G G G A G G G A G F F F F E E E D D G G C C C",
            jmena(Pisnicky.prsiPrsi),
        )
        assertEquals(
            "G E E E G E E E G G A G G F F F D D D F D D D F F G F F E E",
            jmena(Pisnicky.pecNamSpadla),
        )
        assertTrue(jmena(Pisnicky.kockaLeze).startsWith("C D E F G G A A G A A G F F F F E E D D G"))
    }

    @Test
    fun bodyASerie() {
        val h = Hodnoceni()
        assertEquals(10, h.spravne())
        assertEquals(11, h.spravne())
        h.chyba()
        assertEquals(10, h.spravne())
        assertEquals(2, h.nejdelsiSerie)
        assertEquals(31, h.body)
    }

    @Test
    fun hvezdicky() {
        val h = Hodnoceni()
        repeat(19) { h.spravne() }
        h.chyba()
        assertEquals(3, h.hvezdy()) // 95 %
        val h2 = Hodnoceni()
        repeat(3) { h2.spravne() }
        h2.chyba()
        assertEquals(1, h2.hvezdy()) // 75 %
    }

    @Test
    fun profilOdznakyADny() {
        var p = Profil("1", "Anička", "🦊")
        val h = Hodnoceni().apply { repeat(24) { spravne() } }
        val (p2, nove) = p.zapisVysledek(null, "skakal_pes", h, dnes = 100)
        p = p2
        assertEquals(3, p.hvezdyPisnicek["skakal_pes"])
        assertTrue(nove.any { it.id == "prvni_pisnicka" })
        assertTrue(nove.any { it.id == "bez_chyby" })
        assertEquals(1, p.dnuVRade)
        assertEquals(2, p.zapisDen(101).dnuVRade)
        assertEquals(1, p.zapisDen(103).dnuVRade)
        assertTrue(p.pisnickaOdemcena(Pisnicky.prsiPrsi))
        assertTrue(!p.pisnickaOdemcena(Pisnicky.pecNamSpadla))
    }
}
