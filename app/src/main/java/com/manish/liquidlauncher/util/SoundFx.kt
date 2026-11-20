package com.manish.liquidlauncher.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.manish.liquidlauncher.model.SoundTheme
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/** Synthesised sounds (no audio assets needed). */
object SoundFx {
    const val POP = 0
    const val SWISH = 1

    @Volatile
    var enabled: Boolean = true

    /** Current sound theme — read before each play. */
    var theme: SoundTheme = SoundTheme.WaterDrop

    private val tracks = arrayOfNulls<AudioTrack>(2)
    private var ready = false

    /** Returns (startHz, endHz, ms, volume) for the current theme and sound kind. */
    private fun soundParams(kind: Int): FloatArray {
        val base = when (kind) {
            POP -> floatArrayOf(620f, 980f, 110f, 0.28f)
            SWISH -> floatArrayOf(420f, 240f, 150f, 0.18f)
            else -> floatArrayOf(620f, 980f, 110f, 0.28f)
        }
        return when (theme) {
            SoundTheme.WaterDrop -> base
            SoundTheme.GlassChime -> floatArrayOf(base[0] * 1.7f, base[1] * 1.7f, base[2] * 1.8f, base[3] * 0.9f)
            SoundTheme.SoftClick -> floatArrayOf(base[0] * 0.7f, base[1] * 0.7f, base[2] * 0.6f, base[3] * 0.5f)
            SoundTheme.Silent -> floatArrayOf(0f, 0f, 0f, 0f)
        }
    }

    private fun build(startHz: Float, endHz: Float, ms: Int, volume: Float): AudioTrack? {
        return try {
            val rate = 22050
            val n = rate * ms / 1000
            val data = ShortArray(n)
            var phase = 0.0
            for (i in 0 until n) {
                val p = i / n.toFloat()
                val f = startHz + (endHz - startHz) * p
                phase += 2.0 * PI * f / rate
                val env = exp(-5f * p) * min(1f, i / 80f)
                data[i] = (sin(phase) * env * volume * Short.MAX_VALUE).toInt().toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(rate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(n * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(data, 0, n)
            track
        } catch (_: Throwable) {
            null
        }
    }

    private fun ensure() {
        if (ready) return
        ready = true
        val p0 = soundParams(POP)
        val p1 = soundParams(SWISH)
        tracks[POP] = build(p0[0], p0[1], p0[2].toInt(), p0[3])
        tracks[SWISH] = build(p1[0], p1[1], p1[2].toInt(), p1[3])
    }

    fun play(kind: Int) {
        if (!enabled) return
        if (theme == SoundTheme.Silent) return
        // Rebuild tracks if theme changed since last ensure
        ensure()
        val t = tracks.getOrNull(kind) ?: return
        try {
            t.stop()
            t.reloadStaticData()
            t.play()
        } catch (_: Throwable) {
        }
    }
}
