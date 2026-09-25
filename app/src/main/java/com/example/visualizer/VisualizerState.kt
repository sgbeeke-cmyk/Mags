package com.example.visualizer

enum class VisualizerStyle(val displayName: String) {
    BARS("Bars"),
    WAVEFORM("Wave"),
    CIRCULAR("Radial"),
    SPECTRUM_PEAKS("Peaks")
}

data class VisualizerFrame(
    val magnitudes: FloatArray = FloatArray(32),
    val waveform: FloatArray = FloatArray(64),
    val rmsLevel: Float = 0f,
    val peakLevel: Float = 0f,
    val isHardwareSynced: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisualizerFrame) return false
        if (!magnitudes.contentEquals(other.magnitudes)) return false
        if (!waveform.contentEquals(other.waveform)) return false
        if (rmsLevel != other.rmsLevel) return false
        if (peakLevel != other.peakLevel) return false
        if (isHardwareSynced != other.isHardwareSynced) return false
        return true
    }

    override fun hashCode(): Int {
        var result = magnitudes.contentHashCode()
        result = 31 * result + waveform.contentHashCode()
        result = 31 * result + rmsLevel.hashCode()
        result = 31 * result + peakLevel.hashCode()
        result = 31 * result + isHardwareSynced.hashCode()
        return result
    }
}
