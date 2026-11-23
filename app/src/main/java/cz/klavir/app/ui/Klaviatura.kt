package cz.klavir.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cz.klavir.jadro.Noty

/** Jak má být klávesa zvýrazněná. */
data class OznaceniKlavesy(
    val vypln: Color? = null,
    val prst: Int? = null,
    val obrys: Color? = null,
    val tmavyPopis: Boolean = false,
)

/**
 * Klaviatura na obrazovce (výchozí rozsah c–c2, střední C = c1 je uprostřed). Ťuknutím na klávesu se volá [onKlavesa].
 * Nejdřív se testují černé klávesy, protože leží nad bílými.
 */
@Composable
fun Klaviatura(
    modifier: Modifier = Modifier,
    od: Int = Noty.C4 - 12,
    po: Int = Noty.C4 + 12,
    vyska: Dp = 250.dp,
    oznaceni: Map<Int, OznaceniKlavesy> = emptyMap(),
    ukazNazvy: Boolean = true,
    onKlavesa: (Int) -> Unit,
) {
    val bile = remember(od, po) { (od..po).filter { Noty.jeBila(it) } }
    val cerne = remember(od, po) { (od..po).filter { !Noty.jeBila(it) } }
    val mereni = rememberTextMeasurer()

    Canvas(
        modifier
            .fillMaxWidth()
            .height(vyska)
            .pointerInput(od, po) {
                detectTapGestures { bod ->
                    val g = Geometrie(size.width.toFloat(), size.height.toFloat(), bile.size, 14.dp.toPx())
                    // černé klávesy
                    for (m in cerne) {
                        val r = g.cerna(bile.indexOf(m - 1))
                        if (bod.x in r.first.x..(r.first.x + r.second.width) && bod.y in r.first.y..(r.first.y + r.second.height)) {
                            onKlavesa(m); return@detectTapGestures
                        }
                    }
                    val i = ((bod.x - g.okraj) / g.sirkaBile).toInt().coerceIn(0, bile.lastIndex)
                    onKlavesa(bile[i])
                }
            },
    ) {
        val g = Geometrie(size.width, size.height, bile.size, 14.dp.toPx())
        drawRoundRect(Barvy.klaviaturaRam, cornerRadius = CornerRadius(18.dp.toPx()))

        bile.forEachIndexed { i, m ->
            val o = oznaceni[m]
            val (poz, vel) = g.bila(i)
            drawRoundRect(o?.vypln ?: Color.White, poz, vel, CornerRadius(10.dp.toPx()))
            o?.obrys?.let { drawRoundRect(it, poz, vel, CornerRadius(10.dp.toPx()), style = Stroke(5.dp.toPx())) }
            if (ukazNazvy) {
                popisek(mereni, Noty.nazev(m), Offset(poz.x + vel.width / 2, poz.y + vel.height - 26.dp.toPx()),
                    if (o?.tmavyPopis == true) Color.White else Color(0xFFB8B4C8), 22)
            }
            if (m == Noty.C4) {
                // Střední C (c1) – orientační bod uprostřed klaviatury.
                drawCircle(Barvy.zluta, 6.dp.toPx(), Offset(poz.x + vel.width / 2, poz.y + vel.height * 0.66f))
            }
            o?.prst?.let { prst ->
                val stred = Offset(poz.x + vel.width / 2, poz.y + vel.height - 70.dp.toPx())
                val r = minOf(22.dp.toPx(), vel.width * 0.36f)
                drawCircle(if (o.tmavyPopis) Color.White else Barvy.prst(prst), r, stred)
                popisek(mereni, "$prst", stred, if (o.tmavyPopis) Barvy.prst(prst) else Color.White, 24)
            }
        }
        cerne.forEach { m ->
            val (poz, vel) = g.cerna(bile.indexOf(m - 1))
            val o = oznaceni[m]
            drawRoundRect(o?.vypln ?: Barvy.cernaKlavesa, poz, vel, CornerRadius(7.dp.toPx()))
            o?.obrys?.let { drawRoundRect(it, poz, vel, CornerRadius(7.dp.toPx()), style = Stroke(4.dp.toPx())) }
        }
    }
}

/** Rozměry kláves v pixelech. */
private class Geometrie(sirka: Float, val vyska: Float, pocetBilych: Int, val horni: Float) {
    val okraj = 4f
    val sirkaBile = (sirka - 2 * okraj) / pocetBilych
    private val vyskaKlaves = vyska - horni - 6f

    fun bila(i: Int) = Offset(okraj + i * sirkaBile + 3f, horni) to Size(sirkaBile - 6f, vyskaKlaves)

    /** Černá klávesa ležící vpravo od bílé s indexem [iBile]. */
    fun cerna(iBile: Int): Pair<Offset, Size> {
        val w = sirkaBile * 0.6f
        val x = okraj + (iBile + 1) * sirkaBile - w / 2
        return Offset(x, horni) to Size(w, vyskaKlaves * 0.6f)
    }
}

/** Vykreslí text vycentrovaný na daný bod. */
fun DrawScope.popisek(mereni: TextMeasurer, text: String, stred: Offset, barva: Color, velikostSp: Int, notovy: Boolean = false) {
    val styl = styl(velikostSp, barva = barva).let { if (notovy) it.copy(fontFamily = NotoMusic) else it }
    val vysledek = mereni.measure(text, styl)
    drawText(vysledek, topLeft = Offset(stred.x - vysledek.size.width / 2f, stred.y - vysledek.size.height / 2f))
}
