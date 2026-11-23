package cz.klavir.app.zvuk

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import cz.klavir.jadro.Noty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Jednoduchý „klavírní“ zvuk pro ťukání na klávesy na displeji.
 * Tón = základní frekvence + několik vyšších harmonických s doznívající obálkou.
 * Vzorky se pro každý tón spočítají jen jednou a uloží do paměti.
 */
class Syntezator {
    private val fs = 44100
    private val delka = 1.3
    private val cache = HashMap<Int, ShortArray>()
    private val rozsah = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun vzorky(midi: Int): ShortArray = synchronized(cache) {
        cache.getOrPut(midi) {
            val f = Noty.frekvence(midi)
            val n = (fs * delka).toInt()
            val sily = doubleArrayOf(1.0, 0.5, 0.25, 0.14, 0.08)
            ShortArray(n) { i ->
                val t = i.toDouble() / fs
                val nabeh = minOf(1.0, t / 0.004)
                val dozvuk = if (t > delka - 0.1) (delka - t) / 0.1 else 1.0
                val obalka = nabeh * exp(-t * 3.2) * dozvuk
                var s = 0.0
                for (h in sily.indices) s += sily[h] * exp(-t * h * 1.5) * sin(2 * PI * f * (h + 1) * t)
                (s / 2.0 * obalka * 0.6 * Short.MAX_VALUE).toInt().toShort()
            }
        }
    }

    /** Zahraje tón (nečeká na dokončení). */
    fun zahraj(midi: Int) {
        rozsah.launch {
            val data = vzorky(midi)
            val stopa = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(fs)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(data.size * 2)
                .build()
            try {
                stopa.write(data, 0, data.size)
                stopa.play()
                delay((delka * 1000).toLong() + 100)
            } finally {
                stopa.release()
            }
        }
    }

    /** Předpočítá zvuky, aby první ťuknutí nemělo zpoždění. */
    fun pripravit(tony: IntRange) {
        rozsah.launch { tony.forEach { vzorky(it) } }
    }
}
