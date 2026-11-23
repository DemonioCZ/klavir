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

data class Lekce(
    val id: String,
    val cislo: Int,
    val nazev: String,
    val obrazek: String,
    val popis: String,
    val ukoly: List<Ukol>,
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

    val cde = Lekce(
        "cde", 2, "C, D, E", "🎹",
        "C najdeš vždycky vlevo od dvou černých kláves.",
        listOf(Noty.C4, Noty.D4, Noty.E4, Noty.D4, Noty.C4, Noty.E4, Noty.C4, Noty.D4, Noty.E4, Noty.C4).map { ton ->
            Ukol(
                cil = ton, prst = ton.let { POLOHA_C.indexOf(it) },
                text = "Zahraj ${Noty.nazev(ton)}",
                ukazNotu = false, ukazNazvyKlaves = true, zvyraznitKlavesu = false,
                napoveda = "C je hned vlevo od dvojice černých kláves.",
            )
        },
    )

    val petPrstiku = Lekce(
        "pet_prstiku", 3, "Pět prstíků C–G", "🖐️",
        "Ruka leží v poloze C. Hraj nahoru a dolů jako po schodech.",
        listOf(1, 2, 3, 4, 5, 4, 3, 2, 1, 3, 5, 3, 1).map { prst ->
            val ton = POLOHA_C[prst]
            Ukol(
                cil = ton, prst = prst,
                text = "${Noty.nazev(ton)} – ${prstemSlovy(prst)} ($prst)",
                ukazNotu = false, ukazNazvyKlaves = true, zvyraznitKlavesu = true,
            )
        },
    )

    val notyAKlavesy = Lekce(
        "noty_klavesy", 4, "Noty a klávesy", "🎼",
        "Každá nota v osnově patří k jedné klávese.",
        listOf(Noty.E4, Noty.G4, Noty.C4, Noty.F4, Noty.D4, Noty.E4, Noty.G4, Noty.C4, Noty.F4, Noty.D4).map { ton ->
            Ukol(
                cil = ton, prst = POLOHA_C.indexOf(ton),
                text = "Najdi tuhle notu na klavíru a zahraj ji",
                ukazNotu = true, ukazNazvyKlaves = true, zvyraznitKlavesu = false,
                napoveda = popisPolohy(ton),
            )
        },
    )

    val notyVOsnove = Lekce(
        "noty_osnova", 5, "Noty v osnově", "📖",
        "Teď už bez písmenek na klávesách. Zvládneš to!",
        listOf(Noty.G4, Noty.E4, Noty.A4, Noty.D4, Noty.F4, Noty.C4, Noty.A4, Noty.G4, Noty.E4, Noty.D4, Noty.F4, Noty.C4).map { ton ->
            Ukol(
                cil = ton, prst = null,
                text = "Která je to nota? Zahraj ji",
                ukazNotu = true, ukazNazvyKlaves = false, zvyraznitKlavesu = false,
                napoveda = popisPolohy(ton),
            )
        },
    )

    val vsechny: List<Lekce> = listOf(prstiky, cde, petPrstiku, notyAKlavesy, notyVOsnove)

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
