package com.example.visualizer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

class RealtimeAudioVisualizerEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "RealtimeVisualizer"
        const val FFT_BANDS = 32
        const val WAVEFORM_POINTS = 64
        private const val SMOOTHING_FACTOR = 0.65f
    }

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0
    private var isPlaying: Boolean = false

    private val _visualizerFrame = MutableStateFlow(VisualizerFrame())
    val visualizerFrame: StateFlow<VisualizerFrame> = _visualizerFrame.asStateFlow()

    private val _hasPermission = MutableStateFlow(checkPermission())
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val smoothedMagnitudes = FloatArray(FFT_BANDS) { 0f }
    private val smoothedWaveform = FloatArray(WAVEFORM_POINTS) { 0f }

    private var fallbackSimulationJob: Job? = null
    private var simulationPhase: Float = 0f

    fun refreshPermissionState() {
        val granted = checkPermission()
        _hasPermission.value = granted
        if (granted && currentSessionId != 0 && isPlaying) {
            setupHardwareVisualizer(currentSessionId)
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onAudioSessionIdChanged(sessionId: Int) {
        Log.d(TAG, "AudioSessionId changed: $sessionId")
        if (sessionId == 0 || sessionId == currentSessionId) return
        currentSessionId = sessionId
        if (isPlaying) {
            attachToSession(sessionId)
        }
    }

    fun onPlaybackStateChanged(playing: Boolean) {
        this.isPlaying = playing
        if (playing) {
            if (currentSessionId != 0) {
                attachToSession(currentSessionId)
            } else {
                startSimulation()
            }
        } else {
            pauseCapture()
        }
    }

    private fun attachToSession(sessionId: Int) {
        if (checkPermission()) {
            setupHardwareVisualizer(sessionId)
        } else {
            Log.i(TAG, "RECORD_AUDIO not granted. Using dynamic reactive synthesizer.")
            releaseHardwareVisualizer()
            startSimulation()
        }
    }

    private fun setupHardwareVisualizer(sessionId: Int) {
        try {
            releaseHardwareVisualizer()
            stopSimulation()

            val captureRate = Visualizer.getMaxCaptureRate()
            val vis = Visualizer(sessionId).apply {
                val range = Visualizer.getCaptureSizeRange()
                val targetCaptureSize = if (range != null && range.size >= 2) {
                    256.coerceIn(range[0], range[1])
                } else {
                    256
                }
                captureSize = targetCaptureSize

                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (waveform != null && isPlaying) {
                                processWaveform(waveform)
                            }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (fft != null && isPlaying) {
                                processFft(fft)
                            }
                        }
                    },
                    captureRate / 2,
                    /* waveform = */ true,
                    /* fft = */ true
                )
                enabled = true
            }
            this.visualizer = vis
            Log.i(TAG, "Hardware Visualizer attached to audioSessionId=$sessionId with captureRate=$captureRate")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach hardware Visualizer (session $sessionId): ${e.message}. Falling back to simulation.")
            releaseHardwareVisualizer()
            startSimulation()
        }
    }

    private fun processWaveform(rawWaveform: ByteArray) {
        val outWave = FloatArray(WAVEFORM_POINTS)
        val step = (rawWaveform.size.toFloat() / WAVEFORM_POINTS).coerceAtLeast(1f)
        var sumSquares = 0.0

        for (i in 0 until WAVEFORM_POINTS) {
            val srcIndex = (i * step).toInt().coerceIn(rawWaveform.indices)
            // rawWaveform is unsigned byte 0..255, centered at 128
            val rawUnsigned = rawWaveform[srcIndex].toInt() and 0xFF
            val normalized = (rawUnsigned - 128) / 128f
            smoothedWaveform[i] = smoothedWaveform[i] * SMOOTHING_FACTOR + normalized * (1f - SMOOTHING_FACTOR)
            outWave[i] = smoothedWaveform[i]
            sumSquares += (normalized * normalized)
        }

        val rms = sqrt(sumSquares / WAVEFORM_POINTS).toFloat()
        val currentMag = _visualizerFrame.value.magnitudes

        _visualizerFrame.value = VisualizerFrame(
            magnitudes = currentMag,
            waveform = outWave,
            rmsLevel = rms.coerceIn(0f, 1f),
            peakLevel = (rms * 1.5f).coerceIn(0f, 1f),
            isHardwareSynced = true
        )
    }

    private fun processFft(fft: ByteArray) {
        val n = fft.size
        if (n < 2) return

        // n bytes contain DC, Nyquist, and (n/2 - 1) complex pairs
        val rawMags = FloatArray(n / 2)
        // DC
        rawMags[0] = (fft[0].toFloat() / 128f).coerceAtLeast(0f)

        for (k in 1 until n / 2) {
            val r = fft[2 * k].toFloat()
            val im = fft[2 * k + 1].toFloat()
            val magnitude = hypot(r, im) / 128f
            rawMags[k] = magnitude
        }

        // Downsample/aggregate into FFT_BANDS with logarithmic grouping (musical frequency distribution)
        val aggregated = FloatArray(FFT_BANDS)
        for (i in 0 until FFT_BANDS) {
            // Logarithmic mapping from bass to treble
            val lowIndex = (Math.pow(i.toDouble() / FFT_BANDS, 2.0) * (n / 2 - 1)).toInt()
            val highIndex = (Math.pow((i + 1.0) / FFT_BANDS, 2.0) * (n / 2 - 1)).toInt().coerceAtLeast(lowIndex + 1)

            var bandSum = 0f
            var count = 0
            for (j in lowIndex until highIndex.coerceAtMost(rawMags.size)) {
                bandSum += rawMags[j]
                count++
            }
            val avg = if (count > 0) bandSum / count else 0f
            // Apply slight progressive frequency weighting for natural display
            val freqWeight = 1.0f + (i.toFloat() / FFT_BANDS) * 0.8f
            val targetVal = (avg * freqWeight).coerceIn(0f, 1f)

            // Attack & Decay smoothing
            if (targetVal > smoothedMagnitudes[i]) {
                smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.4f + targetVal * 0.6f
            } else {
                smoothedMagnitudes[i] = smoothedMagnitudes[i] * 0.78f + targetVal * 0.22f
            }
            aggregated[i] = smoothedMagnitudes[i]
        }

        val currentWave = _visualizerFrame.value.waveform
        val rms = _visualizerFrame.value.rmsLevel

        _visualizerFrame.value = VisualizerFrame(
            magnitudes = aggregated,
            waveform = currentWave,
            rmsLevel = rms,
            peakLevel = aggregated.maxOrNull() ?: 0f,
            isHardwareSynced = true
        )
    }

    private fun startSimulation() {
        if (fallbackSimulationJob?.isActive == true) return
        fallbackSimulationJob = scope.launch(Dispatchers.Default) {
            while (isActive && isPlaying) {
                simulationPhase += 0.18f
                val wave = FloatArray(WAVEFORM_POINTS)
                val mags = FloatArray(FFT_BANDS)

                for (i in 0 until WAVEFORM_POINTS) {
                    val theta = i * 0.25f + simulationPhase
                    val value = (sin(theta) * 0.45f + cos(theta * 2.3f) * 0.25f + sin(theta * 0.6f) * 0.2f).toFloat()
                    wave[i] = value.coerceIn(-1f, 1f)
                }

                for (b in 0 until FFT_BANDS) {
                    val freqFactor = 1.0f - (b.toFloat() / FFT_BANDS) * 0.3f
                    val pulse = (sin(simulationPhase * 2.0f + b * 0.4f) * 0.5f + 0.5f) * freqFactor
                    val secondHarmonic = (cos(simulationPhase * 3.4f + b * 0.7f) * 0.3f + 0.3f)
                    val target = ((pulse + secondHarmonic) * 0.7f).coerceIn(0.08f, 0.95f)

                    smoothedMagnitudes[b] = smoothedMagnitudes[b] * 0.6f + target * 0.4f
                    mags[b] = smoothedMagnitudes[b]
                }

                _visualizerFrame.value = VisualizerFrame(
                    magnitudes = mags,
                    waveform = wave,
                    rmsLevel = 0.5f,
                    peakLevel = mags.maxOrNull() ?: 0.5f,
                    isHardwareSynced = false
                )

                delay(33) // ~30 fps update rate
            }
        }
    }

    private fun stopSimulation() {
        fallbackSimulationJob?.cancel()
        fallbackSimulationJob = null
    }

    private fun pauseCapture() {
        try {
            visualizer?.enabled = false
        } catch (ignored: Exception) {}
        stopSimulation()

        // Decay bars to zero smoothly
        scope.launch(Dispatchers.Default) {
            for (step in 0 until 5) {
                for (i in 0 until FFT_BANDS) {
                    smoothedMagnitudes[i] *= 0.5f
                }
                for (i in 0 until WAVEFORM_POINTS) {
                    smoothedWaveform[i] *= 0.5f
                }
                _visualizerFrame.value = VisualizerFrame(
                    magnitudes = smoothedMagnitudes.clone(),
                    waveform = smoothedWaveform.clone(),
                    rmsLevel = 0f,
                    peakLevel = 0f,
                    isHardwareSynced = false
                )
                delay(25)
            }
        }
    }

    fun release() {
        releaseHardwareVisualizer()
        stopSimulation()
    }

    private fun releaseHardwareVisualizer() {
        try {
            visualizer?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing Visualizer: ${e.message}")
        } finally {
            visualizer = null
        }
    }
}
