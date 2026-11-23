package cz.klavir.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.klavir.app.R

/** Barvy aplikace. Každý prst má svou barvu – stejnou na ruce, na klávese i nad notou. */
object Barvy {
    val pozadi = Color(0xFFFFF7E8)
    val text = Color(0xFF2B2A4C)
    val tlumeny = Color(0xFF7A7896)
    val karta = Color.White
    val linka = Color(0xFFE9DFC9)
    val modra = Color(0xFF3D7BF2)
    val modraStin = Color(0xFF2858B8)
    val zelena = Color(0xFF2DB36B)
    val zelenaSvetla = Color(0xFFE8F8EF)
    val oranzova = Color(0xFFF59E2B)
    val oranzovaSvetla = Color(0xFFFFF1DC)
    val zluta = Color(0xFFFFC52E)
    val zlutaSvetla = Color(0xFFFFF4D6)
    val fialova = Color(0xFF8A6CF0)
    val klaviaturaRam = Color(0xFF2B2A4C)
    val cernaKlavesa = Color(0xFF1D1C33)

    private val prsty = listOf(
        Color.Gray,
        Color(0xFFEF4E5A), // 1 palec – červená
        Color(0xFFF5922B), // 2 ukazováček – oranžová
        Color(0xFF2DB36B), // 3 prostředníček – zelená
        Color(0xFF3D7BF2), // 4 prsteníček – modrá
        Color(0xFF9A5CF0), // 5 malíček – fialová
    )

    fun prst(cislo: Int?): Color = prsty.getOrElse(cislo ?: 0) { Color.Gray }
}

val Baloo = FontFamily(
    Font(R.font.baloo_semibold, FontWeight.SemiBold),
    Font(R.font.baloo_extrabold, FontWeight.ExtraBold),
)
val NotoMusic = FontFamily(Font(R.font.noto_music))

fun styl(velikost: Int, tucne: Boolean = true, barva: Color = Barvy.text) = TextStyle(
    fontFamily = Baloo,
    fontWeight = if (tucne) FontWeight.ExtraBold else FontWeight.SemiBold,
    fontSize = velikost.sp,
    lineHeight = (velikost * 1.15).sp,
    color = barva,
)

/** Bílá zaoblená karta se „spodním stínem“ jako hrací kartička. */
fun Modifier.karta(barva: Color = Barvy.karta, stin: Color = Barvy.linka, zaobleni: Dp = 28.dp): Modifier =
    this
        .drawBehind {
            drawRoundRect(stin, topLeft = Offset(0f, 5.dp.toPx()), size = Size(size.width, size.height), cornerRadius = CornerRadius(zaobleni.toPx()))
        }
        .clip(RoundedCornerShape(zaobleni))
        .background(barva)

@Composable
fun Tlacitko(
    text: String,
    modifier: Modifier = Modifier,
    hlavni: Boolean = true,
    velikost: Int = 26,
    povoleno: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .karta(
                barva = if (!povoleno) Barvy.linka else if (hlavni) Barvy.modra else Barvy.karta,
                stin = if (hlavni && povoleno) Barvy.modraStin else Barvy.linka,
                zaobleni = 22.dp,
            )
            .clickable(enabled = povoleno, onClick = onClick)
            .padding(horizontal = 30.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = styl(velikost, barva = if (hlavni) Color.White else Barvy.text))
    }
}

@Composable
fun Pilulka(text: String, modifier: Modifier = Modifier, barva: Color = Barvy.karta, velikost: Int = 22, onClick: (() -> Unit)? = null) {
    Box(
        modifier
            .karta(barva = barva, zaobleni = 999.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        Text(text, style = styl(velikost))
    }
}

@Composable
fun TlacitkoZpet(onClick: () -> Unit) {
    Box(
        Modifier
            .size(56.dp)
            .karta(zaobleni = 18.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text("‹", style = styl(36)) }
}

/** Kulaté tlačítko s reproduktorem – přečte text nahlas. */
@Composable
fun Reproduktor(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(52.dp)
            .karta(barva = Color(0xFFEAF1FF), stin = Color(0xFFC9D8F7), zaobleni = 26.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text("🔊", style = styl(24)) }
}

/**
 * Okénko se zadáním. Reproduktor je v pravém dolním rohu (přání pana učitele),
 * aby si ho dítě našlo vždy na stejném místě.
 */
@Composable
fun ZadaniKarta(
    nadpis: String?,
    text: String,
    napoveda: String?,
    modifier: Modifier = Modifier,
    barvaTextu: Color = Barvy.text,
    onPrecti: () -> Unit,
) {
    Box(modifier.karta().padding(start = 26.dp, top = 18.dp, end = 18.dp, bottom = 14.dp)) {
        Column(Modifier.padding(end = 60.dp)) {
            if (nadpis != null) Text(nadpis, style = styl(20, tucne = false, barva = Barvy.tlumeny))
            Text(text, style = styl(32, barva = barvaTextu))
            if (!napoveda.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(napoveda, style = styl(20, tucne = false, barva = Barvy.tlumeny))
            }
        }
        Reproduktor(Modifier.align(Alignment.BottomEnd), onPrecti)
    }
}

/** Horní lišta obrazovky: zpět, nadpis a vpravo libovolné „pilulky“. */
@Composable
fun HorniLista(nadpis: String, onZpet: (() -> Unit)?, vpravo: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (onZpet != null) TlacitkoZpet(onZpet)
        Text(nadpis, style = styl(32))
        Spacer(Modifier.weight(1f))
        vpravo()
    }
}

/** Ukazatel mikrofonu: zelený, když poslouchá, s „tančícími“ sloupečky hlasitosti. */
@Composable
fun MikrofonUkazatel(posloucha: Boolean, hlasitost: Float, slysim: String?, onClick: () -> Unit) {
    Row(
        Modifier
            .karta(zaobleni = 999.dp)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 16.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(if (posloucha) Barvy.zelena else Barvy.linka),
            contentAlignment = Alignment.Center,
        ) { Text(if (posloucha) "🎤" else "👆", style = styl(20)) }
        Text(
            when {
                !posloucha -> "Ťukám na displej"
                slysim != null -> "Slyším: $slysim"
                else -> "Poslouchám piano"
            },
            style = styl(20),
        )
        if (posloucha) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(26.dp)) {
                val v = (hlasitost * 30f).coerceIn(0.05f, 1f)
                listOf(0.35f, 0.7f, 1f, 0.6f, 0.3f).forEach { k ->
                    Box(Modifier.width(5.dp).height((26 * v * k).coerceAtLeast(3f).dp).clip(RoundedCornerShape(3.dp)).background(Barvy.zelena))
                }
            }
        }
    }
}

/** Kroužek s číslem prstu v barvě prstu. */
@Composable
fun CisloPrstu(prst: Int, velikost: Dp = 40.dp) {
    Box(
        Modifier.size(velikost).clip(CircleShape).background(Barvy.prst(prst)),
        contentAlignment = Alignment.Center,
    ) { Text("$prst", style = styl((velikost.value * 0.55f).toInt(), barva = Color.White)) }
}

/** Ukazatel postupu (zelený pruh). */
@Composable
fun Postup(podil: Float, modifier: Modifier = Modifier, barva: Color = Barvy.zelena) {
    Box(
        modifier
            .height(12.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(Color.White)
            .border(2.dp, Barvy.linka, RoundedCornerShape(99.dp)),
    ) {
        Box(Modifier.fillMaxWidth(podil.coerceIn(0f, 1f)).height(12.dp).clip(RoundedCornerShape(99.dp)).background(barva))
    }
}

/** Malé posunutí pro „poskočení“ prvku – používá se u zpětné vazby. */
fun Modifier.poskok(px: Float): Modifier = this.offset(y = (-px).dp)
