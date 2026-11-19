package com.manish.liquidlauncher.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/** Synthesised water-drop sounds (no audio assets needed). */
object SoundFx {
    const val POP = 0
    const val SWISH = 1

    @Volatile
    var enabled: Boolean = true

    private val tracks = arrayOfNulls<AudioTrack>(2)
    private var ready = false

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
        tracks[POP] = build(620f, 980f, 110, 0.28f)
        tracks[SWISH] = build(420f, 240f, 150, 0.18f)
    }

    fun play(kind: Int) {
        if (!enabled) return
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
