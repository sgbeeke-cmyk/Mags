package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.model.EqualizerBand
import com.example.data.model.EqualizerPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioFxManager {
    companion object {
        private const val TAG = "AudioFxManager"
        val DEFAULT_FREQUENCIES = listOf(60, 230, 910, 3600, 14000)
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private val _bands = MutableStateFlow(
        DEFAULT_FREQUENCIES.mapIndexed { index, freq ->
            EqualizerBand(index = index, centerFreqHz = freq, gainDb = 0f)
        }
    )
    val bands: StateFlow<List<EqualizerBand>> = _bands.asStateFlow()

    private val _bassBoostLevel = MutableStateFlow(0) // 0 to 1000
    val bassBoostLevel: StateFlow<Int> = _bassBoostLevel.asStateFlow()

    private val _virtualizerLevel = MutableStateFlow(0) // 0 to 1000
    val virtualizerLevel: StateFlow<Int> = _virtualizerLevel.asStateFlow()

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _currentPresetName = MutableStateFlow("Hi-Res Master")
    val currentPresetName: StateFlow<String> = _currentPresetName.asStateFlow()

    init {
        // Apply default Hi-Res Master preset values initially
        applyPreset(EqualizerPreset.DEFAULT_PRESETS.first())
    }

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        currentSessionId = audioSessionId

        releaseFx()

        try {
            val eq = Equalizer(0, audioSessionId)
            val numBands = eq.numberOfBands.toInt()
            if (numBands > 0) {
                val bandList = mutableListOf<EqualizerBand>()
                for (i in 0 until numBands) {
                    val centerFreqHz = eq.getCenterFreq(i.toShort()) / 1000
                    val currentMilliBels = eq.getBandLevel(i.toShort())
                    val gainDb = currentMilliBels / 100f
                    bandList.add(EqualizerBand(index = i, centerFreqHz = centerFreqHz, gainDb = gainDb))
                }
                _bands.value = bandList
            }
            eq.enabled = _isEnabled.value
            equalizer = eq
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Equalizer initialization failed (using software mode): ${e.message}")
        }

        try {
            val bb = BassBoost(0, audioSessionId)
            if (bb.strengthSupported) {
                bb.setStrength(_bassBoostLevel.value.toShort())
            }
            bb.enabled = _isEnabled.value
            bassBoost = bb
        } catch (e: Exception) {
            Log.w(TAG, "Hardware BassBoost initialization failed: ${e.message}")
        }

        try {
            val virt = Virtualizer(0, audioSessionId)
            if (virt.strengthSupported) {
                virt.setStrength(_virtualizerLevel.value.toShort())
            }
            virt.enabled = _isEnabled.value
            virtualizer = virt
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Virtualizer initialization failed: ${e.message}")
        }

        // Synchronize current band levels to the newly attached session
        reapplyCurrentState()
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        val updated = _bands.value.toMutableList()
        if (bandIndex in updated.indices) {
            updated[bandIndex] = updated[bandIndex].copy(gainDb = gainDb)
            _bands.value = updated
            _currentPresetName.value = "Custom"

            equalizer?.let { eq ->
                try {
                    val milliBels = (gainDb * 100f).toInt().coerceIn(-1200, 1200).toShort()
                    eq.setBandLevel(bandIndex.toShort(), milliBels)
                } catch (e: Exception) {
                    Log.w(TAG, "Error setting band level: ${e.message}")
                }
            }
        }
    }

    fun setBassBoost(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _bassBoostLevel.value = clamped
        _currentPresetName.value = "Custom"
        try {
            bassBoost?.let {
                if (it.strengthSupported) it.setStrength(clamped.toShort())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting bass boost: ${e.message}")
        }
    }

    fun setVirtualizer(strength: Int) {
        val clamped = strength.coerceIn(0, 1000)
        _virtualizerLevel.value = clamped
        _currentPresetName.value = "Custom"
        try {
            virtualizer?.let {
                if (it.strengthSupported) it.setStrength(clamped.toShort())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting virtualizer: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w(TAG, "Error toggling effects enabled: ${e.message}")
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPresetName.value = preset.name
        val currentBands = _bands.value
        val newBands = currentBands.mapIndexed { index, band ->
            val gain = if (index < preset.bandLevels.size) preset.bandLevels[index] else 0f
            band.copy(gainDb = gain)
        }
        _bands.value = newBands
        _bassBoostLevel.value = preset.bassBoost
        _virtualizerLevel.value = preset.virtualizer

        reapplyCurrentState()
    }

    private fun reapplyCurrentState() {
        val eq = equalizer
        if (eq != null) {
            try {
                _bands.value.forEachIndexed { index, band ->
                    if (index < eq.numberOfBands) {
                        val mb = (band.gainDb * 100f).toInt().coerceIn(-1200, 1200).toShort()
                        eq.setBandLevel(index.toShort(), mb)
                    }
                }
                eq.enabled = _isEnabled.value
            } catch (e: Exception) {
                Log.w(TAG, "Error reapplying EQ state: ${e.message}")
            }
        }

        try {
            bassBoost?.let {
                if (it.strengthSupported) it.setStrength(_bassBoostLevel.value.toShort())
                it.enabled = _isEnabled.value
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reapplying bass boost: ${e.message}")
        }

        try {
            virtualizer?.let {
                if (it.strengthSupported) it.setStrength(_virtualizerLevel.value.toShort())
                it.enabled = _isEnabled.value
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reapplying virtualizer: ${e.message}")
        }
    }

    fun releaseFx() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            virtualizer?.release()
            virtualizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing audio effects: ${e.message}")
        }
    }
}
