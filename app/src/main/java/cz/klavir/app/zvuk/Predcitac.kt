package cz.klavir.app.zvuk

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Předčítá texty nahlas česky (pro děti, které ještě neumí číst).
 * Používá hlas zabudovaný v Androidu; pokud chybí český hlas, [dostupny] je false.
 */
class Predcitac(context: Context) {
    private var pripraven = false
    var dostupny = false; private set
    private var cekajici: String? = null

    private var tts: TextToSpeech? = null

    init {
        tts = try {
            TextToSpeech(context.applicationContext) { stav -> priInicializaci(stav) }
        } catch (e: Exception) {
            null // např. při vykreslování náhledů bez systému Android
        }
    }

    private fun priInicializaci(stav: Int) {
        if (stav == TextToSpeech.SUCCESS) {
            val t = tts ?: return
            val vysledek = t.setLanguage(Locale("cs", "CZ"))
            dostupny = vysledek != TextToSpeech.LANG_MISSING_DATA && vysledek != TextToSpeech.LANG_NOT_SUPPORTED
            t.setSpeechRate(0.9f)
            pripraven = true
            cekajici?.let { precti(it) }
            cekajici = null
        }
    }

    fun precti(text: String) {
        if (!pripraven) {
            cekajici = text
            return
        }
        if (!dostupny) return
        // Emoji a symboly by hlas četl divně, tak je vynecháme.
        val cisty = text.replace(Regex("[^\\p{L}\\p{N}\\s.,!?:()–-]"), " ")
        tts?.speak(cisty, TextToSpeech.QUEUE_FLUSH, null, "klavir")
    }

    fun zastav() { tts?.stop() }

    fun uvolni() { tts?.shutdown() }
}
