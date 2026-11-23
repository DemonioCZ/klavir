package cz.klavir.app.zvuk

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import cz.klavir.jadro.DetektorUderu
import cz.klavir.jadro.Yin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Poslouchá mikrofon a hlásí údery do kláves skutečného piana.
 *
 * Zvuk čte po oknech 2048 vzorků s posunem 1024 (≈ 23 ms při 44,1 kHz),
 * každé okno pošle do [DetektorUderu] (algoritmus YIN v modulu jádro).
 */
class Mikrofon {
    private val rozsah = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var uloha: Job? = null

    private val _udery = MutableSharedFlow<Int>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    /** Nově zahrané tóny (čísla MIDI). */
    val udery: SharedFlow<Int> = _udery

    private val _stav = MutableStateFlow(DetektorUderu.Stav(0f, null, 0.0))
    /** Aktuální hlasitost a slyšený tón – pro ukazatel na obrazovce. */
    val stav: StateFlow<DetektorUderu.Stav> = _stav

    private val _bezi = MutableStateFlow(false)
    val bezi: StateFlow<Boolean> = _bezi

    @Volatile
    private var ignorovatDo = 0L

    /**
     * Na chvíli přestane hlásit údery – používá se, když aplikace sama hraje tón
     * (ťuknutí na displej, ukázka písničky), aby se nepočítal jako zahraný žákem.
     */
    fun ignoruj(ms: Long) {
        ignorovatDo = maxOf(ignorovatDo, SystemClock.elapsedRealtime() + ms)
    }

    @SuppressLint("MissingPermission") // oprávnění kontroluje MainActivity před spuštěním
    fun spust() {
        if (uloha?.isActive == true) return
        uloha = rozsah.launch {
            val fs = 44100
            val okno = 2048
            val krok = 1024
            val minBuffer = AudioRecord.getMinBufferSize(fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val zaznam = try {
                AudioRecord(
                    // VOICE_RECOGNITION vypíná automatické zesílení a potlačení šumu – lepší pro hudbu.
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    fs, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
                    maxOf(minBuffer, okno * 4 * 2),
                )
            } catch (e: Exception) {
                _bezi.value = false
                return@launch
            }
            if (zaznam.state != AudioRecord.STATE_INITIALIZED) {
                zaznam.release()
                _bezi.value = false
                return@launch
            }
            val detektor = DetektorUderu(Yin(fs, okno))
            val buffer = FloatArray(okno)
            val novy = FloatArray(krok)
            zaznam.startRecording()
            _bezi.value = true
            try {
                // Nejdřív naplníme celé okno, pak vždy posuneme o „krok“.
                var naplneno = 0
                while (isActive && naplneno < okno) {
                    val n = zaznam.read(buffer, naplneno, okno - naplneno, AudioRecord.READ_BLOCKING)
                    if (n <= 0) break
                    naplneno += n
                }
                while (isActive) {
                    var precteno = 0
                    while (precteno < krok) {
                        val n = zaznam.read(novy, precteno, krok - precteno, AudioRecord.READ_BLOCKING)
                        if (n <= 0) break
                        precteno += n
                    }
                    System.arraycopy(buffer, krok, buffer, 0, okno - krok)
                    System.arraycopy(novy, 0, buffer, okno - krok, krok)
                    val ton = detektor.zpracuj(buffer)
                    _stav.value = detektor.posledniStav
                    if (ton != null && SystemClock.elapsedRealtime() >= ignorovatDo) {
                        _udery.tryEmit(ton)
                    }
                }
            } finally {
                zaznam.stop()
                zaznam.release()
                _bezi.value = false
                _stav.value = DetektorUderu.Stav(0f, null, 0.0)
            }
        }
    }

    fun zastav() {
        uloha?.cancel()
        uloha = null
    }
}
