package com.example.data.p2p

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class CallStatus {
    IDLE,
    DIALING,
    INCOMING,
    CONNECTED,
    ENDED
}

data class CallSession(
    val peerId: String = "",
    val peerName: String = "",
    val isIncoming: Boolean = false,
    val status: CallStatus = CallStatus.IDLE,
    val durationSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isPushToTalkActive: Boolean = false,
    val latencyMs: Int = 18, // Ultra-low latency P2P BLE/Wi-Fi Direct
    val audioLevel: Float = 0f // 0.0 to 1.0 for visualizer
)

class WalkieTalkieManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onSendAudioPacket: (peerId: String, data: ByteArray) -> Unit
) {
    private val sampleRate = 16000
    private val channelConfigIn = AudioFormat.CHANNEL_IN_MONO
    private val channelConfigOut = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfigIn, audioFormat).coerceAtLeast(1024)

    private val _callSession = MutableStateFlow(CallSession())
    val callSession = _callSession.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null

    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startOutgoingCall(peerId: String, peerName: String) {
        _callSession.value = CallSession(
            peerId = peerId,
            peerName = peerName,
            isIncoming = false,
            status = CallStatus.DIALING
        )

        // Simulate instant local handshake response after 1.5s
        scope.launch {
            kotlinx.coroutines.delay(1200)
            if (_callSession.value.status == CallStatus.DIALING) {
                connectCall()
            }
        }
    }

    fun answerIncomingCall() {
        connectCall()
    }

    fun simulateIncomingCall(peerId: String, peerName: String) {
        _callSession.value = CallSession(
            peerId = peerId,
            peerName = peerName,
            isIncoming = true,
            status = CallStatus.INCOMING
        )
    }

    private fun connectCall() {
        _callSession.value = _callSession.value.copy(
            status = CallStatus.CONNECTED,
            durationSeconds = 0
        )
        initAudioTrack()
        startAudioCapture()
        startDurationTimer()
    }

    fun endCall() {
        stopAudioCapture()
        stopAudioTrack()
        timerJob?.cancel()
        _callSession.value = _callSession.value.copy(status = CallStatus.ENDED)
        scope.launch {
            kotlinx.coroutines.delay(800)
            _callSession.value = CallSession()
        }
    }

    fun toggleMicMute() {
        _callSession.value = _callSession.value.copy(isMicMuted = !_callSession.value.isMicMuted)
    }

    fun toggleSpeaker() {
        _callSession.value = _callSession.value.copy(isSpeakerOn = !_callSession.value.isSpeakerOn)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.isSpeakerphoneOn = _callSession.value.isSpeakerOn
    }

    fun setPushToTalkActive(active: Boolean) {
        _callSession.value = _callSession.value.copy(isPushToTalkActive = active)
    }

    private fun initAudioTrack() {
        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(audioFormat)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelConfigOut)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    private fun startAudioCapture() {
        if (!hasRecordPermission()) return
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfigIn,
                audioFormat,
                bufferSize
            )
            audioRecord?.startRecording()

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(bufferSize)
                while (isActive && _callSession.value.status == CallStatus.CONNECTED) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0 && !_callSession.value.isMicMuted) {
                        // Calculate amplitude for visualizer
                        var maxAmp = 0
                        for (i in 0 until read step 2) {
                            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
                            maxAmp = maxOf(maxAmp, abs(sample.toShort().toInt()))
                        }
                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        _callSession.value = _callSession.value.copy(audioLevel = normalized)

                        // Send over P2P link
                        val peer = _callSession.value.peerId
                        if (peer.isNotBlank()) {
                            onSendAudioPacket(peer, buffer.copyOf(read))
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun playReceivedAudio(data: ByteArray) {
        try {
            audioTrack?.write(data, 0, data.size)
        } catch (_: Exception) {}
    }

    private fun startDurationTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _callSession.value.status == CallStatus.CONNECTED) {
                kotlinx.coroutines.delay(1000)
                _callSession.value = _callSession.value.copy(
                    durationSeconds = _callSession.value.durationSeconds + 1
                )
            }
        }
    }

    private fun stopAudioCapture() {
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    private fun stopAudioTrack() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
