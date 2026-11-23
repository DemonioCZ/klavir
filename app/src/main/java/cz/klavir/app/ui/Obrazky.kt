package cz.klavir.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val KUZE = Color(0xFFFFD9B8)
private val KUZE_SVETLA = Color(0xFFFFE9D6)

/**
 * Ruka shora (dlaní dolů) s očíslovanými prsty.
 * Pravá ruka má palec vlevo, levá vpravo – tak, jak je dítě vidí nad klávesami.
 */
@Composable
fun Ruka(prava: Boolean, aktivniPrst: Int?, modifier: Modifier = Modifier, velikost: Dp = 240.dp) {
    val mereni = rememberTextMeasurer()
    Canvas(modifier.size(velikost, velikost * 280f / 240f)) {
        // kreslíme v souřadnicích 240 × 280 a pak zvětšíme
        val k = size.width / 240f
        scale(k, k, pivot = Offset.Zero) {
            val W = 240f
            val prsty = listOf(2 to 110f, 3 to 124f, 4 to 112f, 5 to 88f)
            var xs = listOf(66f, 100f, 134f, 166f)
            if (!prava) xs = xs.map { W - it - 28f }
            drawRoundRect(KUZE, Offset(if (prava) 56f else 46f, 128f), Size(138f, 132f), CornerRadius(52f))
            prsty.forEachIndexed { i, (prst, delka) ->
                val x = xs[i]
                val y = 150f - delka
                val aktivni = aktivniPrst == prst
                drawRoundRect(if (aktivni) KUZE_SVETLA else KUZE, Offset(x, y), Size(28f, delka + 30f), CornerRadius(14f))
                if (aktivni) drawRoundRect(Barvy.prst(prst), Offset(x, y), Size(28f, delka + 30f), CornerRadius(14f), style = Stroke(6f))
                val stred = Offset(x + 14f, y + 22f)
                drawCircle(Barvy.prst(prst), if (aktivni) 19f else 15f, stred)
                popisek(mereni, "$prst", stred, Color.White, if (aktivni) 18 else 15)
            }
            // palec
            val tx = if (prava) 50f else W - 50f
            val uhel = if (prava) -28f else 28f
            val aktivni = aktivniPrst == 1
            rotate(uhel, Offset(tx, 240f)) {
                drawRoundRect(if (aktivni) KUZE_SVETLA else KUZE, Offset(tx - 16f, 150f), Size(32f, 96f), CornerRadius(16f))
                if (aktivni) drawRoundRect(Barvy.prst(1), Offset(tx - 16f, 150f), Size(32f, 96f), CornerRadius(16f), style = Stroke(6f))
            }
            val stred = Offset(tx + if (prava) -32f else 32f, 180f)
            drawCircle(Barvy.prst(1), if (aktivni) 19f else 15f, stred)
            popisek(mereni, "1", stred, Color.White, if (aktivni) 18 else 15)
        }
    }
}

/** Maskot aplikace – fialová sovička Notička (vlastní kresba). */
@Composable
fun Sova(modifier: Modifier = Modifier, velikost: Dp = 72.dp, mrka: Boolean = false) {
    Canvas(modifier.size(velikost)) {
        val k = size.width / 100f
        scale(k, k, pivot = Offset.Zero) {
            val fialova = Color(0xFF8A6CF0)
            val brisko = Color(0xFFCBBEFF)
            drawPath(Path().apply { moveTo(18f, 30f); lineTo(28f, 14f); lineTo(36f, 28f); close() }, fialova)
            drawPath(Path().apply { moveTo(82f, 30f); lineTo(72f, 14f); lineTo(64f, 28f); close() }, fialova)
            drawOval(fialova, Offset(14f, 20f), Size(72f, 76f))
            drawOval(brisko, Offset(28f, 44f), Size(44f, 48f))
            drawCircle(Color.White, 14f, Offset(36f, 42f))
            drawCircle(Color.White, 14f, Offset(64f, 42f))
            if (mrka) {
                drawLine(Barvy.text, Offset(31f, 44f), Offset(41f, 44f), strokeWidth = 3f)
            } else {
                drawCircle(Barvy.text, 6f, Offset(38f, 43f))
            }
            drawCircle(Barvy.text, 6f, Offset(62f, 43f))
            drawPath(Path().apply { moveTo(45f, 52f); lineTo(55f, 52f); lineTo(50f, 60f); close() }, Barvy.zluta)
        }
    }
}
