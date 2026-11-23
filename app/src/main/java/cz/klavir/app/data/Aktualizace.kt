package cz.klavir.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Aktualizace aplikace přímo z nastavení.
 *
 * Nové verze sestavuje GitHub Actions a vydává je jako „Release“ s APK souborem
 * v repozitáři DemonioCZ/klavir. Značka vydání má tvar „build-<číslo sestavení>“.
 */
object Aktualizace {
    private const val API = "https://api.github.com/repos/DemonioCZ/klavir/releases/latest"

    data class Info(val cislo: Int, val nazev: String, val poznamky: String, val urlApk: String)

    /** Vrátí novější verzi, pokud existuje; null = aplikace je aktuální. Při chybě vyhodí výjimku. */
    suspend fun zkontroluj(aktualniCislo: Int): Info? = withContext(Dispatchers.IO) {
        val spojeni = (URL(API).openConnection() as HttpURLConnection).apply {
            setRequestProperty("Accept", "application/vnd.github+json")
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        try {
            if (spojeni.responseCode == 404) return@withContext null
            if (spojeni.responseCode != 200) error("GitHub odpověděl kódem ${spojeni.responseCode}")
            val json = JSONObject(spojeni.inputStream.bufferedReader().readText())
            val cislo = json.optString("tag_name").removePrefix("build-").toIntOrNull() ?: return@withContext null
            val assets = json.optJSONArray("assets") ?: return@withContext null
            val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                .firstOrNull { it.optString("name").endsWith(".apk") } ?: return@withContext null
            if (cislo <= aktualniCislo) null
            else Info(cislo, json.optString("name"), json.optString("body"), apk.getString("browser_download_url"))
        } finally {
            spojeni.disconnect()
        }
    }

    /** Stáhne APK do mezipaměti aplikace. [postup] dostává hodnoty 0.0–1.0. */
    suspend fun stahni(context: Context, url: String, postup: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val slozka = File(context.cacheDir, "aktualizace").apply { mkdirs() }
        val soubor = File(slozka, "klavir.apk")
        var adresa = url
        var spojeni: HttpURLConnection
        // GitHub přesměrovává na úložiště souborů – projdeme až 5 přesměrování.
        var pokusy = 0
        while (true) {
            spojeni = (URL(adresa).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15_000
                readTimeout = 30_000
            }
            val kod = spojeni.responseCode
            if (kod in 300..399 && pokusy++ < 5) {
                adresa = spojeni.getHeaderField("Location")
                spojeni.disconnect()
                continue
            }
            if (kod != 200) error("Stahování selhalo (kód $kod)")
            break
        }
        try {
            val celkem = spojeni.contentLengthLong
            var stazeno = 0L
            spojeni.inputStream.use { vstup ->
                soubor.outputStream().use { vystup ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = vstup.read(buf)
                        if (n < 0) break
                        vystup.write(buf, 0, n)
                        stazeno += n
                        if (celkem > 0) postup(stazeno.toFloat() / celkem)
                    }
                }
            }
        } finally {
            spojeni.disconnect()
        }
        soubor
    }

    /** Smí aplikace instalovat aktualizace? (Android to musí jednou povolit rodič.) */
    fun muzeInstalovat(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    /** Otevře nastavení Androidu, kde se povolí instalace z této aplikace. */
    fun otevriPovoleni(context: Context) {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /** Spustí systémovou instalaci staženého APK. */
    fun nainstaluj(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.soubory", apk)
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
