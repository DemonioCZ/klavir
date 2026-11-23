package cz.klavir.jadro

/**
 * Odhad výšky tónu algoritmem YIN (de Cheveigné & Kawahara, 2002).
 *
 * Postup: rozdílová funkce → kumulativní normalizace → první minimum pod prahem
 * → parabolické zpřesnění. Vrací frekvenci v Hz a „jistotu“ 0–1.
 *
 * @param vzorkovaciFrekvence např. 44100 Hz
 * @param velikostOkna počet vzorků v jednom okně (např. 2048)
 * @param minHz nejnižší hledaný tón (výchozí ~C3)
 * @param maxHz nejvyšší hledaný tón (výchozí ~C7)
 * @param prah práh YIN; menší = přísnější (doporučeno 0.10–0.20)
 */
class Yin(
    private val vzorkovaciFrekvence: Int,
    private val velikostOkna: Int,
    minHz: Double = 120.0,
    maxHz: Double = 2200.0,
    private val prah: Float = 0.15f,
) {
    data class Vysledek(val hz: Double, val jistota: Float) {
        val platny: Boolean get() = hz > 0
    }

    private val tauMin = maxOf(2, (vzorkovaciFrekvence / maxHz).toInt())
    private val tauMax = minOf(velikostOkna / 2, (vzorkovaciFrekvence / minHz).toInt() + 1)
    private val d = FloatArray(tauMax + 1)

    fun odhadni(okno: FloatArray): Vysledek {
        require(okno.size >= velikostOkna) { "Okno je kratší než $velikostOkna vzorků" }
        val w = velikostOkna / 2

        // 1) Rozdílová funkce d(tau)
        for (tau in 0..tauMax) {
            var soucet = 0f
            for (i in 0 until w) {
                val r = okno[i] - okno[i + tau]
                soucet += r * r
            }
            d[tau] = soucet
        }

        // 2) Kumulativní normalizace d'(tau)
        d[0] = 1f
        var bezici = 0f
        for (tau in 1..tauMax) {
            bezici += d[tau]
            d[tau] = if (bezici == 0f) 1f else d[tau] * tau / bezici
        }

        // 3) Absolutní práh: první tau pod prahem, pak dojdeme na lokální minimum
        var tau = tauMin
        var nalezeno = -1
        while (tau <= tauMax) {
            if (d[tau] < prah) {
                while (tau + 1 <= tauMax && d[tau + 1] < d[tau]) tau++
                nalezeno = tau
                break
            }
            tau++
        }
        if (nalezeno < 0) return Vysledek(-1.0, 0f)

        // 4) Parabolická interpolace kolem minima pro přesnější periodu
        val presneTau = if (nalezeno in (tauMin + 1) until tauMax) {
            val s0 = d[nalezeno - 1]
            val s1 = d[nalezeno]
            val s2 = d[nalezeno + 1]
            val jmenovatel = 2f * (2f * s1 - s2 - s0)
            if (jmenovatel != 0f) nalezeno + (s2 - s0) / jmenovatel else nalezeno.toFloat()
        } else {
            nalezeno.toFloat()
        }

        return Vysledek(vzorkovaciFrekvence / presneTau.toDouble(), 1f - d[nalezeno])
    }
}
