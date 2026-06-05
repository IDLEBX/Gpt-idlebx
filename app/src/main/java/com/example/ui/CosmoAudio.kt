package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.sin

object CosmoAudio {
    private const val TAG = "CosmoAudio"

    /**
     * Dynamically synthesizes a high-fidelity retro sci-fi audio sine pulse.
     * Accomplished natively without requiring any raw sound assets.
     */
    fun playCosmicBeep(frequencyHz: Double = 440.0, durationMs: Int = 180, volume: Float = 0.8f) {
        Thread {
            try {
                val sampleRate = 8000
                val numSamples = ((durationMs / 1000.0) * sampleRate).toInt()
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                // Render beautiful smooth sine wave
                for (i in 0 until numSamples) {
                    sample[i] = sin(2 * Math.PI * i / (sampleRate / frequencyHz))
                }

                // Convert to PCM 16bit byte array representation
                var idx = 0
                for (i in 0 until numSamples) {
                    val valShort = (sample[i] * 32767 * volume).toInt().toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00FF).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xFF00) ushr 8).toByte()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSnd.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(generatedSnd, 0, generatedSnd.size)
                audioTrack.play()
                
                // Allow audio track to finish playback before releasing
                Thread.sleep(durationMs.toLong() + 50)
                audioTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Dynamic sonification synthesis failed", e)
            }
        }.start()
    }

    /**
     * Synthesizes a beautiful ascending cosmic chord effect.
     */
    fun playChimeChord() {
        playCosmicBeep(523.25, 100, 0.4f) // C5
        Thread.sleep(80)
        playCosmicBeep(659.25, 100, 0.4f) // E5
        Thread.sleep(80)
        playCosmicBeep(783.99, 150, 0.5f) // G5
    }

    /**
     * Synthesizes a fast descending laser error/collapse effect.
     */
    fun playCollapseEffect() {
        playCosmicBeep(600.0, 60, 0.5f)
        Thread.sleep(40)
        playCosmicBeep(450.0, 60, 0.5f)
        Thread.sleep(40)
        playCosmicBeep(300.0, 100, 0.5f)
    }
}
