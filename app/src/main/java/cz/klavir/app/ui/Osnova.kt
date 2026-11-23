package cz.klavir.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import cz.klavir.jadro.Nota
import cz.klavir.jadro.Noty

/** Stav noty v písničce – podle toho se obarví. */
enum class StavNoty { CEKA, AKTUALNI, SPRAVNE, S_CHYBOU }

/**
 * Notová osnova s houslovým klíčem.
 * Kreslí linky, klíč a noty; volitelně čísla prstů nad notami a slabiky textu pod nimi.
 */
object Osnova {

    /** Mezera mezi linkami v px a svislá pozice spodní linky. */
    class Rozmery(val mezera: Float, val spodniLinka: Float) {
        fun y(pozice: Int) = spodniLinka - pozice * mezera / 2f
        val horniLinka get() = spodniLinka - 4 * mezera
    }

    fun DrawScope.linkyAKlic(r: Rozmery, mereni: TextMeasurer, odX: Float = 20f, doX: Float = size.width - 20f) {
        for (i in 0..4) {
            val y = r.spodniLinka - i * r.mezera
            drawLine(Barvy.text, Offset(odX, y), Offset(doX, y), strokeWidth = 2.dp.toPx())
        }
        // Houslový klíč z písma Noto Music; „pata“ klíče sedí na lince G.
        val styl = styl(10, barva = Barvy.text).copy(
            fontFamily = NotoMusic,
            fontSize = androidx.compose.ui.unit.TextUnit(r.mezera * 4.2f / density / fontScale, androidx.compose.ui.unit.TextUnitType.Sp),
        )
        val klic = mereni.measure("𝄞", styl)
        drawText(klic, topLeft = Offset(odX + 4f, r.spodniLinka - klic.size.height * 0.68f))
    }

    /**
     * Nakreslí jednu notu.
     * @param doby délka (2 = půlová – prázdná hlavička)
     */
    fun DrawScope.nota(r: Rozmery, x: Float, midi: Int, doby: Float, barva: Color) {
        val poz = Noty.poziceVOsnove(midi)
        val y = r.y(poz)
        // pomocné linky pod osnovou (C4 a níž) a nad ní (A5 a výš)
        var p = -2
        while (p >= poz) {
            drawLine(Barvy.text, Offset(x - r.mezera * 1.2f, r.y(p)), Offset(x + r.mezera * 1.2f, r.y(p)), strokeWidth = 2.dp.toPx())
            p -= 2
        }
        p = 10
        while (p <= poz) {
            drawLine(Barvy.text, Offset(x - r.mezera * 1.2f, r.y(p)), Offset(x + r.mezera * 1.2f, r.y(p)), strokeWidth = 2.dp.toPx())
            p += 2
        }
        val rx = r.mezera * 0.68f
        val ry = r.mezera * 0.5f
        rotate(-20f, Offset(x, y)) {
            if (doby >= 2f) {
                drawOval(barva, Offset(x - rx, y - ry), Size(2 * rx, 2 * ry), style = Stroke(3.5.dp.toPx()))
            } else {
                drawOval(barva, Offset(x - rx, y - ry), Size(2 * rx, 2 * ry))
            }
        }
        if (doby < 4f) {
            val nahoru = poz < 4
            val sx = if (nahoru) x + rx - 2f else x - rx + 2f
            val konec = if (nahoru) y - r.mezera * 3.4f else y + r.mezera * 3.4f
            drawLine(barva, Offset(sx, y), Offset(sx, konec), strokeWidth = 3.dp.toPx())
            if (doby <= 0.5f) {
                // osminová nota: praporek
                val smer = if (nahoru) 1f else -1f
                drawLine(barva, Offset(sx, konec), Offset(sx + r.mezera * 0.9f, konec + smer * r.mezera * 1.3f), strokeWidth = 3.dp.toPx())
            }
        }
    }
}

/** Osnova s jednou velkou notou (lekce čtení not). */
@Composable
fun OsnovaJednaNota(midi: Int?, barva: Color, modifier: Modifier = Modifier, pismeno: String? = null) {
    val mereni = rememberTextMeasurer()
    Canvas(modifier) {
        val mezera = size.height / 11f
        val r = Osnova.Rozmery(mezera, size.height * 0.68f)
        with(Osnova) {
            linkyAKlic(r, mereni)
            if (midi != null) nota(r, size.width * 0.62f, midi, 1f, barva)
            // písmeno noty pod osnovou
            if (midi != null && pismeno != null) {
                popisek(mereni, pismeno, Offset(size.width * 0.62f, r.spodniLinka + mezera * 2.6f), Barvy.zelena, 44)
            }
        }
    }
}

/**
 * Osnova písničky: zobrazí okno not kolem aktuální noty, nad notami čísla prstů,
 * pod nimi slabiky. Taktové čáry podle [Nota.novyTakt].
 */
@Composable
fun OsnovaPisnicky(
    noty: List<Nota>,
    stavy: List<StavNoty>,
    aktualni: Int,
    modifier: Modifier = Modifier,
) {
    val mereni = rememberTextMeasurer()
    Canvas(modifier) {
        // Při nízké kartě se osnova i popisky úměrně zmenší.
        val mezera = minOf(20.dp.toPx(), size.height / 13f)
        val k = mezera / 20.dp.toPx()
        val r = Osnova.Rozmery(mezera, size.height * 0.55f)
        val startX = 150.dp.toPx()
        val krokDoby = 62.dp.toPx()
        val mezeraTaktu = 26.dp.toPx()

        // x pozice všech not v jedné dlouhé řadě
        val xs = FloatArray(noty.size)
        val taktX = mutableListOf<Float>()
        var x = 0f
        noty.forEachIndexed { i, n ->
            if (n.novyTakt && i > 0) {
                taktX += x + mezeraTaktu / 2 - krokDoby * 0.35f
                x += mezeraTaktu
            }
            xs[i] = x
            x += krokDoby * maxOf(n.doby, 0.75f)
        }
        // posun tak, aby aktuální nota byla asi ve třetině šířky
        val cil = startX + (size.width - startX) * 0.3f
        val posun = if (noty.isEmpty()) 0f else minOf(startX + 20.dp.toPx(), cil - xs[aktualni.coerceIn(0, noty.lastIndex)])

        with(Osnova) {
            linkyAKlic(r, mereni)
            clipRectSafe(startX - 10.dp.toPx()) {
                taktX.forEach { tx ->
                    val px = tx + posun
                    drawLine(Barvy.text, Offset(px, r.horniLinka), Offset(px, r.spodniLinka), strokeWidth = 2.dp.toPx())
                }
                noty.forEachIndexed { i, n ->
                    val px = xs[i] + posun
                    if (px < startX - 40.dp.toPx() || px > size.width + 40.dp.toPx()) return@forEachIndexed
                    val stav = stavy.getOrElse(i) { StavNoty.CEKA }
                    val barvaPrstu = Barvy.prst(n.prst)
                    if (stav == StavNoty.AKTUALNI) {
                        drawRoundRect(
                            barvaPrstu.copy(alpha = 0.13f),
                            Offset(px - krokDoby * 0.45f, r.horniLinka - 62.dp.toPx() * k),
                            Size(krokDoby * 0.9f, r.spodniLinka - r.horniLinka + 128.dp.toPx() * k),
                            androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                        )
                    }
                    val barva = when (stav) {
                        StavNoty.CEKA -> Barvy.text
                        StavNoty.AKTUALNI -> barvaPrstu
                        StavNoty.SPRAVNE -> Barvy.zelena
                        StavNoty.S_CHYBOU -> Barvy.oranzova
                    }
                    nota(r, px, n.midi, n.doby, barva)
                    // číslo prstu nad osnovou
                    val stredPrstu = Offset(px, r.horniLinka - 34.dp.toPx() * k)
                    val alfa = if (stav == StavNoty.SPRAVNE || stav == StavNoty.S_CHYBOU) 0.35f else 1f
                    drawCircle(barvaPrstu.copy(alpha = alfa), 15.dp.toPx() * k, stredPrstu)
                    popisek(mereni, "${n.prst}", stredPrstu, Color.White, 18)
                    if (n.posun != null) {
                        popisek(mereni, "↔", Offset(px, r.horniLinka - 62.dp.toPx() * k), Barvy.oranzova, 22)
                    }
                    // slabika
                    if (n.slabika.isNotEmpty()) {
                        popisek(
                            mereni, n.slabika, Offset(px, r.spodniLinka + 48.dp.toPx() * k),
                            if (stav == StavNoty.AKTUALNI) barvaPrstu else Barvy.text, 22,
                        )
                    }
                    if (stav == StavNoty.SPRAVNE) popisek(mereni, "✓", Offset(px, r.spodniLinka + 76.dp.toPx() * k), Barvy.zelena, 16)
                }
            }
        }
    }
}

/** Ořízne kreslení zleva (aby noty nepřejely přes houslový klíč). */
private inline fun DrawScope.clipRectSafe(odX: Float, blok: DrawScope.() -> Unit) {
    drawContext.canvas.save()
    drawContext.canvas.clipRect(odX, 0f, size.width, size.height)
    blok()
    drawContext.canvas.restore()
}

/**
 * Osnova s několika notami vedle sebe (např. C, D, E). Nota, kterou má žák zahrát,
 * svítí barvou, pod každou notou je její písmeno.
 */
@Composable
fun OsnovaVyberNot(noty: List<Int>, cil: Int, barvaCile: Color, modifier: Modifier = Modifier) {
    val mereni = rememberTextMeasurer()
    Canvas(modifier) {
        val mezera = size.height / 12f
        val r = Osnova.Rozmery(mezera, size.height * 0.62f)
        val odX = size.width * 0.34f
        val krok = (size.width * 0.92f - odX) / maxOf(noty.size, 1)
        with(Osnova) {
            linkyAKlic(r, mereni)
            noty.forEachIndexed { i, m ->
                val x = odX + krok * (i + 0.5f)
                val jeCil = m == cil
                if (jeCil) {
                    drawRoundRect(
                        barvaCile.copy(alpha = 0.15f),
                        Offset(x - krok * 0.42f, r.horniLinka - mezera * 1.5f),
                        Size(krok * 0.84f, r.spodniLinka - r.horniLinka + mezera * 6f),
                        androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                    )
                }
                nota(r, x, m, 1f, if (jeCil) barvaCile else Barvy.text.copy(alpha = 0.45f))
                popisek(
                    mereni, Noty.nazev(m), Offset(x, r.spodniLinka + mezera * 2.8f),
                    if (jeCil) barvaCile else Barvy.tlumeny, if (jeCil) 40 else 28,
                )
            }
        }
    }
}
