package cz.klavir.jadro

/**
 * Jeden úkol v lekci: zahraj daný tón (případně daným prstem).
 *
 * @param cil tón, který má žák zahrát
 * @param prst kterým prstem (null = na prstu nezáleží)
 * @param text zadání, které se zobrazí a může se nahlas přečíst
 * @param ukazNotu zobrazit notu v osnově
 * @param ukazNazvyKlaves psát na klávesy písmena
 * @param zvyraznitKlavesu rozsvítit cílovou klávesu (nápověda)
 * @param napoveda doplňující věta pod zadáním
 */
data class Ukol(
    val cil: Int,
    val prst: Int?,
    val text: String,
    val ukazNotu: Boolean,
    val ukazNazvyKlaves: Boolean,
    val zvyraznitKlavesu: Boolean,
    val napoveda: String = "",
)

/**
 * @param notyVOsnove noty, které se v lekci ukazují v osnově (hraná nota se rozsvítí);
 *   prázdné = lekce osnovu tímto způsobem nepoužívá
 */
data class Lekce(
    val id: String,
    val cislo: Int,
    val nazev: String,
    val obrazek: String,
    val popis: String,
    val ukoly: List<Ukol>,
    val notyVOsnove: List<Int> = emptyList(),
)

/** Lekce pro začátečníky. Pořadí úkolů je pevné, aby se dalo lekci opakovat stejně. */
object Lekce1az5 {

    private val JMENA_PRSTU = arrayOf("", "palcem", "ukazováčkem", "prostředníčkem", "prsteníčkem", "malíčkem")
    private val JMENA_PRSTU_1P = arrayOf("", "palec", "ukazováček", "prostředníček", "prsteníček", "malíček")

    fun jmenoPrstu(prst: Int): String = JMENA_PRSTU_1P[prst]
    fun prstemSlovy(prst: Int): String = JMENA_PRSTU[prst]

    /** Klávesy polohy C: palec na C … malíček na G. */
    private val POLOHA_C = intArrayOf(0, Noty.C4, Noty.D4, Noty.E4, Noty.F4, Noty.G4)

    private fun ukolPrst(prst: Int) = Ukol(
        cil = POLOHA_C[prst],
        prst = prst,
        text = "Zahraj ${prstemSlovy(prst)} ($prst) pravé ruky",
        ukazNotu = false,
        ukazNazvyKlaves = true,
        zvyraznitKlavesu = true,
        napoveda = "Palec (1) leží na C. Každý prstík má svou klávesu.",
    )

    val prstiky = Lekce(
        "prstiky", 1, "Prstíky mají čísla", "✋",
        "Palec je 1, ukazováček 2, prostředníček 3, prsteníček 4 a malíček 5.",
        listOf(1, 2, 3, 4, 5, 3, 1, 5, 2, 4).map { ukolPrst(it) },
    )

    /** Úkol „zahraj tuto notu“ pro lekce, které ukazují několik not v osnově. */
    private fun ukolNota(ton: Int) = Ukol(
        cil = ton, prst = POLOHA_C.indexOf(ton),
        text = "Zahraj ${Noty.nazev(ton)}",
        ukazNotu = false, ukazNazvyKlaves = true, zvyraznitKlavesu = false,
    )

    // Každá nota se nejdřív několikrát zopakuje, aby si ji dítě stihlo zapamatovat; střídání přijde až na konci.
    val cde = Lekce(
        "cde", 2, "C, D, E", "🎹",
        "C najdeš vždycky vlevo od dvou černých kláves.",
        listOf(
            Noty.C4, Noty.C4, Noty.C4, Noty.C4,
            Noty.D4, Noty.D4, Noty.D4, Noty.D4,
            Noty.E4, Noty.E4, Noty.E4, Noty.E4,
            Noty.C4, Noty.C4, Noty.D4, Noty.D4, Noty.E4, Noty.E4,
            Noty.C4, Noty.D4, Noty.E4,
        ).map { ton ->
            ukolNota(ton)
        },
        notyVOsnove = listOf(Noty.C4, Noty.D4, Noty.E4),
    )

    val fg = Lekce(
        "fg", 3, "F, G", "🎶",
        "F najdeš vlevo od trojice černých kláves, G hned vedle něj.",
        listOf(
            Noty.F4, Noty.F4, Noty.F4, Noty.F4,
            Noty.G4, Noty.G4, Noty.G4, Noty.G4,
            Noty.F4, Noty.F4, Noty.G4, Noty.G4, Noty.F4, Noty.F4, Noty.G4, Noty.G4,
            Noty.F4, Noty.G4, Noty.F4, Noty.G4,
        ).map { ton ->
            ukolNota(ton)
        },
        notyVOsnove = listOf(Noty.F4, Noty.G4),
    )

    val petPrstiku = Lekce(
        "pet_prstiku", 4, "Pět prstíků C–G", "🖐️",
        "Ruka leží v poloze C. Hraj nahoru a dolů jako po schodech.",
        listOf(1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 4, 3, 2, 1, 1).map { prst ->
            val ton = POLOHA_C[prst]
            Ukol(
                cil = ton, prst = prst,
                text = "${Noty.nazev(ton)} – ${prstemSlovy(prst)} ($prst)",
                ukazNotu = false, ukazNazvyKlaves = true, zvyraznitKlavesu = true,
            )
        },
    )

    val notyAKlavesy = Lekce(
        "noty_klavesy", 5, "Noty a klávesy", "🎼",
        "Každá nota v osnově patří k jedné klávese.",
        listOf(Noty.E4, Noty.G4, Noty.C4, Noty.F4, Noty.D4, Noty.E4, Noty.G4, Noty.C4, Noty.F4, Noty.D4).map { ton ->
            Ukol(
                cil = ton, prst = POLOHA_C.indexOf(ton),
                text = "Najdi tuhle notu na klavíru a zahraj ji",
                ukazNotu = true, ukazNazvyKlaves = true, zvyraznitKlavesu = false,
            )
        },
    )

    val notyVOsnove = Lekce(
        "noty_osnova", 6, "Noty v osnově", "📖",
        "Poznej notu podle toho, kde sedí v osnově. Zvládneš to!",
        listOf(Noty.G4, Noty.E4, Noty.A4, Noty.D4, Noty.F4, Noty.C4, Noty.A4, Noty.G4, Noty.E4, Noty.D4, Noty.F4, Noty.C4).map { ton ->
            Ukol(
                cil = ton, prst = null,
                text = "Která je to nota? Zahraj ji",
                ukazNotu = true, ukazNazvyKlaves = true, zvyraznitKlavesu = false,
                napoveda = popisPolohy(ton),
            )
        },
    )

    val vsechny: List<Lekce> = listOf(prstiky, cde, fg, petPrstiku, notyAKlavesy, notyVOsnove)

    fun podleId(id: String): Lekce? = vsechny.firstOrNull { it.id == id }

    /** Slovní popis, kde nota v osnově sedí (nápověda pro děti). */
    fun popisPolohy(midi: Int): String = when (Noty.poziceVOsnove(midi)) {
        -2 -> "Nota má vlastní malou linku pod osnovou: to je C"
        -1 -> "Nota visí pod první linkou: to je D"
        0 -> "Nota sedí na první lince: to je E"
        1 -> "Nota je v první mezeře: to je F"
        2 -> "Nota sedí na druhé lince: to je G"
        3 -> "Nota je ve druhé mezeře: to je A"
        4 -> "Nota sedí na třetí lince: to je H"
        else -> ""
    }
}
