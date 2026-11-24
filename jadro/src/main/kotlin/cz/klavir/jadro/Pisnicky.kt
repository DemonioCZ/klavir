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
 * @param slovo celé slovo, které tato nota dokončuje (přečte se nahlas po zahrání); jinak prázdné
 */
data class Nota(
    val midi: Int,
    val doby: Float,
    val prst: Int,
    val slabika: String,
    val posun: String? = null,
    val novyTakt: Boolean = false,
    val slovo: String = "",
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
 * za dvojtečkou délka v dobách, např. "G", "E:2", "C:0.5", "G:1.5" (s tečkou).
 *
 * Slabiky: pomlčka na konci znamená, že slovo pokračuje další slabikou („Ská- kal“),
 * vlnovka je mezera uvnitř slabiky („k~Tá-“), podtržítko = bez textu.
 * Slovo může pokračovat i přes taktovou čáru.
 */
private class Zapis {
    val noty = mutableListOf<Nota>()
    private val rozepsaneSlovo = StringBuilder()

    fun takt(toky: String, prsty: String, slabiky: String, posuny: Map<Int, String> = emptyMap()) {
        val t = toky.trim().split(Regex("\\s+"))
        val p = prsty.trim().split(Regex("\\s+")).map { it.toInt() }
        val s = slabiky.trim().split(Regex("\\s+")).map { if (it == "_") "" else it.replace('~', ' ') }
        require(t.size == p.size && t.size == s.size) {
            "Takt '$toky' má ${t.size} not, ${p.size} prstů a ${s.size} slabik"
        }
        t.forEachIndexed { i, zapis ->
            val (ton, delka) = zapis.split(":").let { it[0] to (it.getOrNull(1)?.toFloat() ?: 1f) }
            val pokracuje = s[i].endsWith("-")
            val slabika = s[i].removeSuffix("-")
            rozepsaneSlovo.append(slabika)
            val slovo = if (pokracuje) "" else rozepsaneSlovo.toString().also { rozepsaneSlovo.clear() }
            noty += Nota(
                midi = prevedTon(ton),
                doby = delka,
                prst = p[i],
                slabika = slabika,
                slovo = slovo,
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
        takt("G G E:2", "4 4 2", "Ská- kal pes")
        takt("G G E:2", "4 4 2", "přes o- ves")
        takt("G G A G", "4 4 5 4", "přes ze- le- nou")
        takt("G:2 F:2", "4 3", "lou- ku")
        takt("F F D:2", "3 3 1", "šel za ním")
        takt("F F D:2", "3 3 1", "mys- li- vec")
        takt("F F G F", "3 3 4 3", "pé- ro na klo-")
        takt("F:2 E:2", "3 2", "bou- ku")
    }

    // Rozsah C–A = šest tónů, jeden posun ruky na dlouhé notě E („na lu-ka“).
    val prsiPrsi = pisnicka(
        "prsi_prsi", "Prší, prší", "🌧️", 4, 100,
        "Začátek v poloze D, pak posun do polohy C", 3, "pavelhokr.cz",
    ) {
        takt("G G A G", "4 4 5 4", "Pr- ší pr- ší")
        takt("G G A G", "4 4 5 4", "jen se le- je")
        takt("G G A G", "4 4 5 4", "kam ko- níč- ky")
        takt("G G A G", "4 4 5 4", "po- je- de- me")
        takt("F F F F", "3 3 3 3", "po- je- de- me")
        takt(
            "E E E:2", "2 2 2", "na lu- ka",
            posuny = mapOf(2 to "Během dlouhé noty posuň ruku o klávesu doleva: palec na C"),
        )
        takt("D D G G", "2 2 5 5", "až ku- kač- ka")
        takt("C C C:2", "1 1 1", "za- ku- ká")
    }

    // Rozsah C–A, posun na opakovaném G výměnou prstu 5 → 4, návrat na konci.
    val kockaLeze = pisnicka(
        "kocka_leze", "Kočka leze dírou", "🐈", 2, 92,
        "Začátek v poloze C, na opakovaném G posun do polohy D", 6, "prodeticokoliv.cz",
    ) {
        takt("C:0.5 D:0.5 E:0.5 F:0.5", "1 2 3 4", "Koč- ka le- ze")
        takt(
            "G G", "5 4", "dí- rou",
            posuny = mapOf(1 to "Stejné G zahraj prsteníčkem (4): ruka se posune do polohy D"),
        )
        takt("A A", "5 5", "pes ok-")
        takt("G:2", "4", "nem")
        takt("A A", "5 5", "pes ok-")
        takt("G:2", "4", "nem")
        repeat(2) {
            takt("F:0.5 F:0.5 F:0.5 F:0.5", "3 3 3 3", "ne- bu- de- li")
            takt("E E", "2 2", "pr- šet")
            takt("D D", "1 1", "nez- mok-")
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
        takt("G E E E", "4 2 2 2", "Pec nám spad- la")
        takt("G E E E", "4 2 2 2", "pec nám spad- la")
        takt("G G A G", "4 4 5 4", "kdo- pak nám ji")
        takt("G F F:2", "4 3 3", "po- sta- ví")
        takt("F D D D", "3 1 1 1", "sta- rý pe- cař")
        takt("F D D D", "3 1 1 1", "ne- ní do- ma")
        takt("F F G F", "3 3 4 3", "a mla- dý to")
        takt("F E E:2", "3 2 2", "ne- u- mí")
    }

    // Rozsah C–G, ruka celou dobu v poloze C – nejjednodušší písnička.
    val travickaZelena = pisnicka(
        "travicka_zelena", "Travička zelená", "🌿", 4, 100,
        "Poloha C: palec na C, malíček na G", 2, "pavelhokr.cz",
    ) {
        takt("E E F:2", "3 3 4", "Tra- vič- ka")
        takt("G G E:2", "5 5 3", "ze- le- ná")
        takt("F F D D", "4 4 2 2", "to je mo- je")
        takt("G G E E", "5 5 3 3", "po- tě- še- ní")
        takt("E E F:2", "3 3 4", "Tra- vič- ka")
        takt("G G E:2", "5 5 3", "ze- le- ná")
        takt("F F D D", "4 4 2 2", "to je mo- je")
        takt("C E C:2", "1 3 1", "pe- ři- na")
        takt("F F D D", "4 4 2 2", "Když si smys- lím")
        takt("G G E E", "5 5 3 3", "na ní sed- nu")
        takt("F F D D", "4 4 2 2", "když si smys- lím")
        takt("G G E E", "5 5 3 3", "na ní leh- nu")
        takt("E E F:2", "3 3 4", "Tra- vič- ka")
        takt("G G E:2", "5 5 3", "ze- le- ná")
        takt("F F D D", "4 4 2 2", "to je mo- je")
        takt("C E C:2", "1 3 1", "pe- ři- na")
    }

    // Rozsah C–G, poloha C bez posunu. Noty jsou společné s „Když jsem husy pásala“.
    val beziLiska = pisnicka(
        "bezi_liska", "Běží liška k Táboru", "🦊", 4, 104,
        "Poloha C: palec na C, malíček na G", 5, "pavelhokr.cz",
    ) {
        takt("C E C E", "1 3 1 3", "Bě- ží liš- ka")
        takt("G:2 G G", "5 5 5", "k~Tá- bo- ru")
        takt("C E C E", "1 3 1 3", "ne- se py- tel")
        takt("D:2 D D", "2 2 2", "záz- vo- ru")
        takt("C E G E", "1 3 5 3", "je- žek za ní")
        takt("D D E:2", "2 2 3", "pos- pí- chá")
        takt("C E G E", "1 3 5 3", "že jí py- tel")
        takt("D D C:2", "2 2 1", "roz- pí- chá")
    }

    // Rozsah D–A, ruka celou dobu v poloze D.
    val holkaModrooka = pisnicka(
        "holka_modrooka", "Holka modrooká", "💙", 4, 100,
        "Poloha D: palec na D, malíček na A", 12, "pavelhokr.cz",
    ) {
        takt("G:2 A:2", "4 5", "Hol- ka")
        takt("G G E G", "4 4 2 4", "mod- ro- o- ká")
        takt("F F D F", "3 3 1 3", "ne- se- dá- vej")
        takt("G G E F", "4 4 2 3", "u po- to- ka")
        takt("G:2 A:2", "4 5", "Hol- ka")
        takt("G G E G", "4 4 2 4", "mod- ro- o- ká")
        takt("F F D F", "3 3 1 3", "ne- se- dá- vej")
        takt("E:4", "2", "tam")
        takt("F F D F", "3 3 1 3", "V~po- to- ce se")
        takt("G G E G", "4 4 2 4", "vo- da to- čí")
        takt("F F D F", "3 3 1 3", "po- de- me- le")
        takt("G G E F", "4 4 2 3", "tvo- je o- či")
        takt("G:2 A:2", "4 5", "Hol- ka")
        takt("G G E G", "4 4 2 4", "mod- ro- o- ká")
        takt("F F D F", "3 3 1 3", "ne- se- dá- vej")
        takt("E:4", "2", "tam")
    }

    // Rozsah D–A, poloha D bez posunu; jedna čtvrťová nota s tečkou.
    val kdybyBylBavorov = pisnicka(
        "kdyby_byl_bavorov", "Kdyby byl Bavorov", "🏡", 4, 96,
        "Poloha D: palec na D, malíček na A", 15, "pavelhokr.cz",
    ) {
        takt("E E E:2", "2 2 2", "Kdy- by byl")
        takt("G A G:2", "4 5 4", "Ba- vo- rov")
        takt("G F D F", "4 3 1 3", "co jsou Vod- ňa-")
        takt("E:4", "2", "ny")
        takt("E E E:2", "2 2 2", "dal bych ti")
        takt("G A G:2", "4 5 4", "hu- bič- ku")
        takt("G F D F", "4 3 1 3", "na o- bě stra-")
        takt("E:4", "2", "ny")
        takt("A A A A", "5 5 5 5", "A- le že je")
        takt("A G G:2", "5 4 4", "za vo- dou")
        takt("G:1.5 A:0.5 G F", "4 5 4 3", "za vo- dič- kou")
        takt("F E E:2", "3 2 2", "stu- de- nou")
        takt("E E E:2", "2 2 2", "ne- dám ti")
        takt("G A G:2", "4 5 4", "má mi- lá")
        takt("G F D F", "4 3 1 3", "a- ni je- di-")
        takt("E:4", "2", "nou")
    }

    /** Pořadí podle obtížnosti – v tomto pořadí se písničky odemykají. */
    val vsechny: List<Pisnicka> = listOf(
        skakalPes, travickaZelena, prsiPrsi, beziLiska, kockaLeze, pecNamSpadla, holkaModrooka, kdybyBylBavorov,
    )

    fun podleId(id: String): Pisnicka? = vsechny.firstOrNull { it.id == id }
}
