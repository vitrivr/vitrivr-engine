package org.vitrivr.engine.core.database.descriptor

/** A video-relative point with both flat and optional hierarchical recording identity. */
data class TemporalLookupPoint(
    val video: String,
    val timeMs: Double,
    val day: String? = null,
    val source: String? = null,
    val filename: String? = null
) {
    init {
        require(video.isNotBlank())
        require(timeMs.isFinite() && timeMs >= 0 && (timeMs * 1_000_000).isFinite())
        require((day == null && source == null && filename == null) || (day != null && source != null && filename != null))
    }
}
