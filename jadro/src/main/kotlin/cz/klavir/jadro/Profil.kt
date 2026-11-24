package cz.klavir.jadro

/**
 * Postup jednoho žáka. Ukládá se lokálně v tabletu (bez účtu a bez internetu).
 */
data class Profil(
    val id: String,
    val jmeno: String,
    val zvire: String,
    val body: Int = 0,
    val hvezdyLekci: Map<String, Int> = emptyMap(),
    val hvezdyPisnicek: Map<String, Int> = emptyMap(),
    val nejdelsiSerie: Int = 0,
    val spravnychNot: Int = 0,
    val notZMikrofonu: Int = 0,
    val pisnickyBezChyby: Set<String> = emptySet(),
    val odznaky: Set<String> = emptySet(),
    val dnuVRade: Int = 0,
    val posledniDen: Long = -1,
    val predcitatAutomaticky: Boolean = false,
) {
    val hvezdyCelkem: Int get() = hvezdyLekci.values.sum() + hvezdyPisnicek.values.sum()

    val uroven: Int get() = 1 + body / BODY_NA_UROVEN

    /** Kolik procent zbývá do další úrovně (0.0–1.0). */
    val postupUrovne: Float get() = (body % BODY_NA_UROVEN) / BODY_NA_UROVEN.toFloat()

    val nazevUrovne: String
        get() = NAZVY_UROVNI[minOf(uroven - 1, NAZVY_UROVNI.lastIndex)]

    fun lekceOdemcena(lekce: Lekce): Boolean {
        val i = Lekce1az5.vsechny.indexOf(lekce)
        if (i <= 0) return true
        // lekce, kterou už žák hrál, zůstane otevřená, i když se před ni později vloží nová
        if ((hvezdyLekci[lekce.id] ?: 0) > 0) return true
        return (hvezdyLekci[Lekce1az5.vsechny[i - 1].id] ?: 0) > 0
    }

    fun pisnickaOdemcena(p: Pisnicka): Boolean = hvezdyCelkem >= p.potrebaHvezd

    /**
     * Zapíše výsledek lekce nebo písničky a vrátí nový profil a seznam nově získaných odznaků.
     * Hvězdičky se ukládají jen lepší, než byly.
     */
    fun zapisVysledek(
        lekceId: String?,
        pisnickaId: String?,
        h: Hodnoceni,
        dnes: Long,
    ): Pair<Profil, List<Odznak>> {
        val hvezdy = h.hvezdy()
        var p = copy(
            body = body + h.body,
            nejdelsiSerie = maxOf(nejdelsiSerie, h.nejdelsiSerie),
            spravnychNot = spravnychNot + h.spravne,
            notZMikrofonu = notZMikrofonu + h.zMikrofonu,
        )
        if (lekceId != null) {
            p = p.copy(hvezdyLekci = p.hvezdyLekci + (lekceId to maxOf(hvezdy, p.hvezdyLekci[lekceId] ?: 0)))
        }
        if (pisnickaId != null) {
            p = p.copy(hvezdyPisnicek = p.hvezdyPisnicek + (pisnickaId to maxOf(hvezdy, p.hvezdyPisnicek[pisnickaId] ?: 0)))
            if (h.chyby == 0) p = p.copy(pisnickyBezChyby = p.pisnickyBezChyby + pisnickaId)
        }
        p = p.zapisDen(dnes)
        val nove = Odznaky.vsechny.filter { it.id !in p.odznaky && it.splneno(p) }
        p = p.copy(odznaky = p.odznaky + nove.map { it.id })
        return p to nove
    }

    /** Počítá „dny v řadě“: včera hrál → +1, dnes už hrál → beze změny, jinak znovu od 1. */
    fun zapisDen(dnes: Long): Profil = when (posledniDen) {
        dnes -> this
        dnes - 1 -> copy(dnuVRade = dnuVRade + 1, posledniDen = dnes)
        else -> copy(dnuVRade = 1, posledniDen = dnes)
    }

    companion object {
        const val BODY_NA_UROVEN = 500
        val NAZVY_UROVNI = listOf(
            "Zvídavé ouško", "Ťukálek", "Malý klavírista", "Notový detektiv",
            "Pianista", "Mistr kláves", "Koncertní hvězda",
        )
        val ZVIRATKA = listOf("🦊", "🐻", "🐰", "🐱", "🐶", "🐼", "🐸", "🦉", "🐯", "🐨", "🦄", "🐧")
    }
}

/** Odznak, který žák získá za splnění podmínky. */
data class Odznak(
    val id: String,
    val nazev: String,
    val obrazek: String,
    val popis: String,
    val splneno: (Profil) -> Boolean,
)

object Odznaky {
    val vsechny = listOf(
        Odznak("prvni_ton", "První tón", "🎹", "Zahraj první správnou notu") { it.spravnychNot >= 1 },
        Odznak("prstiky", "Prstíky", "✋", "Dokonči lekci Prstíky mají čísla") { (it.hvezdyLekci["prstiky"] ?: 0) > 0 },
        Odznak("ctenar_not", "Čtenář not", "🎼", "Dokonči lekci Noty v osnově") { (it.hvezdyLekci["noty_osnova"] ?: 0) > 0 },
        Odznak("prvni_pisnicka", "První písnička", "🎵", "Zahraj celou písničku") { it.hvezdyPisnicek.isNotEmpty() },
        Odznak("dny5", "5 dní v řadě", "🔥", "Hraj 5 dní za sebou") { it.dnuVRade >= 5 },
        Odznak("bez_chyby", "Bez chyby", "💯", "Zahraj písničku úplně bez chyby") { it.pisnickyBezChyby.isNotEmpty() },
        Odznak("opravdove_piano", "Opravdové piano", "🎤", "Zahraj 50 not na opravdové piano") { it.notZMikrofonu >= 50 },
        Odznak("destovy_mistr", "Dešťový mistr", "🌧️", "Získej 3 hvězdičky za Prší, prší") { (it.hvezdyPisnicek["prsi_prsi"] ?: 0) >= 3 },
        Odznak("kocici_tlapka", "Kočičí tlapka", "🐈", "Získej 3 hvězdičky za Kočka leze dírou") { (it.hvezdyPisnicek["kocka_leze"] ?: 0) >= 3 },
        Odznak("serie50", "Série 50", "⚡", "Zahraj 50 správných not v řadě") { it.nejdelsiSerie >= 50 },
        Odznak("vsechny_pisnicky", "Všechny písničky", "🏆", "Zahraj všechny písničky") { p -> Pisnicky.vsechny.all { (p.hvezdyPisnicek[it.id] ?: 0) > 0 } },
        Odznak("dny10", "10 dní v řadě", "🌙", "Hraj 10 dní za sebou") { it.dnuVRade >= 10 },
        Odznak("tisic_not", "1000 not", "👑", "Zahraj celkem 1000 správných not") { it.spravnychNot >= 1000 },
    )

    fun podleId(id: String) = vsechny.firstOrNull { it.id == id }
}
