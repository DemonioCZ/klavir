package cz.klavir.app.zvuk

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Předčítá texty nahlas česky (pro děti, které ještě neumí číst).
 * Používá hlas zabudovaný v Androidu; pokud chybí český hlas, [dostupny] je false.
 */
class Predcitac(context: Context) {
    var pripraven = false; private set
    var dostupny = false; private set
    private var cekajici: String? = null

    private var tts: TextToSpeech? = null

    /** Právě mluví – mikrofon mezitím nehlásí údery, aby hlas nepovažoval za tón z piana. */
    @Volatile
    var mluvi = false
        private set

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
            t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { mluvi = true }
                override fun onDone(id: String?) { mluvi = false }
                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) { mluvi = false }
                override fun onStop(id: String?, interrupted: Boolean) { mluvi = false }
            })
            pripraven = true
            cekajici?.let { precti(it) }
            cekajici = null
        }
    }

    /** @param pridat true = přečte až po tom, co doznějí předchozí texty (jinak je přeruší) */
    fun precti(text: String, pridat: Boolean = false) {
        if (!pripraven) {
            cekajici = text
            return
        }
        if (!dostupny) return
        // Emoji a symboly by hlas četl divně, tak je vynecháme.
        val cisty = text.replace(Regex("[^\\p{L}\\p{N}\\s.,!?:()–-]"), " ")
        tts?.speak(cisty, if (pridat) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH, null, "klavir")
    }

    fun zastav() { tts?.stop(); mluvi = false }

    fun uvolni() { tts?.shutdown() }
}
