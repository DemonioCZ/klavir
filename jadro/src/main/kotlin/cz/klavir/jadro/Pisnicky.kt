package cz.klavir.jadro

/**
 * Jedna nota písničky.
 *
 * @param midi tón (60 = c1)
 * @param doby délka v dobách (1 = čtvrťová, 2 = půlová, 0.5 = osminová)
 * @param prst číslo prstu pravé ruky (1 = palec … 5 = malíček)
 * @param slabika slabika textu pod notou
 * @param posun nápověda, když se u této noty posouvá ruka (jinak null)
 * @param novyTakt true, pokud nota začíná nový takt (pro kreslení taktové čáry)
 */
data class Nota(
    val midi: Int,
    val doby: Float,
    val prst: Int,
    val slabika: String,
    val posun: String? = null,
    val novyTakt: Boolean = false,
)

/**
 * Písnička se vším, co aplikace potřebuje: noty s prstokladem, text, tempo
 * a kolik hvězdiček je potřeba na odemčení.
 */
data class Pisnicka(
    val id: String,
    val nazev: String,
    val obrazek: String,
    val taktDoby: Int,
    val tempo: Int,
    val poloha: String,
    val potrebaHvezd: Int,
    val noty: List<Nota>,
    val zdroj: String,
)

/**
 * Malý pomocník pro zápis písniček po taktech.
 * Zápis tónu: písmeno (C D E F G A H), volitelně číslice oktávy (výchozí 4),
 * za dvojtečkou délka v dobách, např. "G", "E:2", "C:0.5".
 */
private class Zapis {
    val noty = mutableListOf<Nota>()

    fun takt(toky: String, prsty: String, slabiky: String, posuny: Map<Int, String> = emptyMap()) {
        val t = toky.trim().split(Regex("\\s+"))
        val p = prsty.trim().split(Regex("\\s+")).map { it.toInt() }
        val s = slabiky.trim().split(Regex("\\s+")).map { if (it == "_") "" else it }
        require(t.size == p.size && t.size == s.size) {
            "Takt '$toky' má ${t.size} not, ${p.size} prstů a ${s.size} slabik"
        }
        t.forEachIndexed { i, zapis ->
            val (ton, delka) = zapis.split(":").let { it[0] to (it.getOrNull(1)?.toFloat() ?: 1f) }
            noty += Nota(
                midi = prevedTon(ton),
                doby = delka,
                prst = p[i],
                slabika = s[i],
                posun = posuny[i],
                novyTakt = i == 0,
            )
        }
    }

    private fun prevedTon(ton: String): Int {
        val zaklad = when (ton[0]) {
            'C' -> 0; 'D' -> 2; 'E' -> 4; 'F' -> 5; 'G' -> 7; 'A' -> 9; 'H' -> 11
            else -> error("Neznámý tón $ton")
        }
        val oktava = ton.drop(1).toIntOrNull() ?: 4
        return (oktava + 1) * 12 + zaklad
    }
}

private fun pisnicka(
    id: String, nazev: String, obrazek: String, taktDoby: Int, tempo: Int,
    poloha: String, potrebaHvezd: Int, zdroj: String, telo: Zapis.() -> Unit,
) = Pisnicka(id, nazev, obrazek, taktDoby, tempo, poloha, potrebaHvezd, Zapis().apply(telo).noty, zdroj)

/**
 * Lidové písničky (volné dílo). Melodie jsou přepsané z notových zápisů pro začátečníky
 * (pavelhokr.cz, prodeticokoliv.cz), všechny v C dur.
 *
 * Prstoklad je pro pravou ruku. Poloha D = palec na D, malíček na A.
 * Poloha C = palec na C, malíček na G.
 */
object Pisnicky {

    // Rozsah D–A = přesně pět tónů, ruka celou dobu v poloze D, bez posunu.
    val skakalPes = pisnicka(
        "skakal_pes", "Skákal pes", "🐕", 4, 96,
        "Poloha D: palec na D, malíček na A", 0, "pavelhokr.cz",
    ) {
        takt("G G E:2", "4 4 2", "Ská kal pes")
        takt("G G E:2", "4 4 2", "přes o ves")
        takt("G G A G", "4 4 5 4", "přes ze le nou")
        takt("G:2 F:2", "4 3", "lou ku")
        takt("F F D:2", "3 3 1", "šel za ním")
        takt("F F D:2", "3 3 1", "mys li vec")
        takt("F F G F", "3 3 4 3", "pé ro na klo")
        takt("F:2 E:2", "3 2", "bou ku")
    }

    // Rozsah C–A = šest tónů, jeden posun ruky na dlouhé notě E („na lu-ka“).
    val prsiPrsi = pisnicka(
        "prsi_prsi", "Prší, prší", "🌧️", 4, 100,
        "Začátek v poloze D, pak posun do polohy C", 3, "pavelhokr.cz",
    ) {
        takt("G G A G", "4 4 5 4", "Pr ší pr ší")
        takt("G G A G", "4 4 5 4", "jen se le je")
        takt("G G A G", "4 4 5 4", "kam ko níč ky")
        takt("G G A G", "4 4 5 4", "po je de me")
        takt("F F F F", "3 3 3 3", "po je de me")
        takt(
            "E E E:2", "2 2 2", "na lu ka",
            posuny = mapOf(2 to "Během dlouhé noty posuň ruku o klávesu doleva: palec na C"),
        )
        takt("D D G G", "2 2 5 5", "až ku kač ka")
        takt("C C C:2", "1 1 1", "za ku ká")
    }

    // Rozsah C–A, posun na opakovaném G výměnou prstu 5 → 4, návrat na konci.
    val kockaLeze = pisnicka(
        "kocka_leze", "Kočka leze dírou", "🐈", 2, 92,
        "Začátek v poloze C, na opakovaném G posun do polohy D", 6, "prodeticokoliv.cz",
    ) {
        takt("C:0.5 D:0.5 E:0.5 F:0.5", "1 2 3 4", "Koč ka le ze")
        takt(
            "G G", "5 4", "dí rou",
            posuny = mapOf(1 to "Stejné G zahraj prsteníčkem (4): ruka se posune do polohy D"),
        )
        takt("A A", "5 5", "pes ok")
        takt("G:2", "4", "nem")
        takt("A A", "5 5", "pes ok")
        takt("G:2", "4", "nem")
        repeat(2) {
            takt("F:0.5 F:0.5 F:0.5 F:0.5", "3 3 3 3", "ne bu de li")
            takt("E E", "2 2", "pr šet")
            takt("D D", "1 1", "nez mok")
            takt("G:2", "4", "nem")
        }
        takt(
            "C:2", "1", "_",
            posuny = mapOf(0 to "Posuň ruku zpátky do polohy C: palec na C"),
        )
    }

    // Rozsah D–A, ruka celou dobu v poloze D.
    val pecNamSpadla = pisnicka(
        "pec_nam_spadla", "Pec nám spadla", "🔥", 4, 100,
        "Poloha D: palec na D, malíček na A", 9, "pavelhokr.cz",
    ) {
        takt("G E E E", "4 2 2 2", "Pec nám spad la")
        takt("G E E E", "4 2 2 2", "pec nám spad la")
        takt("G G A G", "4 4 5 4", "kdo pak nám ji")
        takt("G F F:2", "4 3 3", "po sta ví")
        takt("F D D D", "3 1 1 1", "sta rý pe cař")
        takt("F D D D", "3 1 1 1", "ne ní do ma")
        takt("F F G F", "3 3 4 3", "a mla dý to")
        takt("F E E:2", "3 2 2", "ne u mí")
    }

    val vsechny: List<Pisnicka> = listOf(skakalPes, prsiPrsi, kockaLeze, pecNamSpadla)

    fun podleId(id: String): Pisnicka? = vsechny.firstOrNull { it.id == id }
}
