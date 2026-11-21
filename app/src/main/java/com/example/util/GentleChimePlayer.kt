package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object GentleChimePlayer {

    private val lock = Any()
    private var activeMediaPlayer: MediaPlayer? = null
    private var activeAudioTrack: AudioTrack? = null

    suspend fun startLongAlarm(context: Context, playSound: Boolean) {
        stopAlarm(context)
        startRepeatingVibration(context)
        if (!playSound) return

        withContext(Dispatchers.IO) {
            val playedSystemRing = tryPlaySystemAlarmLoop(context)
            if (!playedSystemRing) {
                tryPlaySynthesizedBellLoop()
            }
        }
    }

    suspend fun playPreviewChime() {
        withContext(Dispatchers.IO) {
            try {
                val sampleRate = 22050
                val durationSec = 1.6
                val numSamples = (sampleRate * durationSec).toInt()
                val buffer = ShortArray(numSamples)
                val notes = listOf(523.25 to 0.0, 659.25 to 0.16, 783.99 to 0.32)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    var sample = 0.0
                    for ((freq, startSec) in notes) {
                        if (t >= startSec) {
                            val dt = t - startSec
                            val env = if (dt < 0.03) dt / 0.03 else exp(-(dt - 0.03) * 3.0)
                            sample += 0.35 * sin(2.0 * PI * freq * dt) * env
                        }
                    }
                    val scaled = (sample * 0.42 * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    buffer[i] = scaled.toShort()
                }

                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                val format = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
                val track = AudioTrack.Builder()
                    .setAudioAttributes(attrs)
                    .setAudioFormat(format)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(buffer.size * 2)
                    .build()
                track.write(buffer, 0, buffer.size)
                track.play()
                kotlinx.coroutines.delay(1700L)
                try {
                    track.stop()
                    track.release()
                } catch (_: Throwable) {
                }
            } catch (_: Throwable) {
            }
        }
    }

    @Suppress("DEPRECATION")
    fun stopAlarm(context: Context) {
        synchronized(lock) {
            activeMediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (_: Throwable) {
                }
                try {
                    player.release()
                } catch (_: Throwable) {
                }
            }
            activeMediaPlayer = null

            activeAudioTrack?.let { track ->
                try {
                    track.pause()
                    track.flush()
                    track.stop()
                } catch (_: Throwable) {
                }
                try {
                    track.release()
                } catch (_: Throwable) {
                }
            }
            activeAudioTrack = null
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator?.cancel()
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.cancel()
            }
        } catch (_: Throwable) {
        }
    }

    private fun tryPlaySystemAlarmLoop(context: Context): Boolean {
        val candidateUris: List<Uri> = listOfNotNull(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        )

        val alarmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        for (uri in candidateUris) {
            val player = MediaPlayer()
            try {
                player.setAudioAttributes(alarmAttributes)
                player.setDataSource(context, uri)
                player.isLooping = true
                player.prepare()
                synchronized(lock) {
                    activeMediaPlayer = player
                }
                player.start()
                return true
            } catch (_: Throwable) {
                try {
                    player.release()
                } catch (_: Throwable) {
                }
            }
        }
        return false
    }

    private fun tryPlaySynthesizedBellLoop() {
        try {
            val sampleRate = 22050
            val durationSec = 2.0
            val numSamples = (sampleRate * durationSec).toInt()
            val buffer = ShortArray(numSamples)

            val note1Freq = 587.33 // D5
            val note2Freq = 880.00 // A5
            val note2StartSample = (sampleRate * 0.4).toInt()

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var sample = 0.0

                val env1 = if (t < 0.02) t / 0.02 else exp(-(t - 0.02) * 2.5)
                sample += (0.65 * sin(2.0 * PI * note1Freq * t) +
                    0.22 * sin(2.0 * PI * (note1Freq * 2.0) * t)) * env1

                if (i >= note2StartSample) {
                    val t2 = (i - note2StartSample).toDouble() / sampleRate
                    val env2 = if (t2 < 0.02) t2 / 0.02 else exp(-(t2 - 0.02) * 2.2)
                    sample += (0.68 * sin(2.0 * PI * note2Freq * t2) +
                        0.20 * sin(2.0 * PI * (note2Freq * 2.0) * t2)) * env2
                }

                val scaled = (sample * 0.45 * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = scaled.toShort()
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(buffer.size * 2)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.setLoopPoints(0, numSamples, -1)
            synchronized(lock) {
                activeAudioTrack = audioTrack
            }
            audioTrack.play()
        } catch (_: Throwable) {
        }
    }

    @Suppress("DEPRECATION")
    private fun startRepeatingVibration(context: Context) {
        val pattern = longArrayOf(0, 600, 350, 600, 350)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0)
                )
            } else {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(pattern, 0)
                    )
                } else {
                    vibrator?.vibrate(pattern, 0)
                }
            }
        } catch (_: Throwable) {
        }
    }
}
