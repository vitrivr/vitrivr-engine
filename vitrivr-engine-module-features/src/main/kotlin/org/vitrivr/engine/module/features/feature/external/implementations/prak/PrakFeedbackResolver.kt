package org.vitrivr.engine.module.features.feature.external.implementations.prak

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.model.descriptor.struct.metadata.TemporalMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.FileSourceMetadataDescriptor
import org.vitrivr.engine.core.model.descriptor.struct.metadata.source.RecordingMetadataDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import java.util.UUID

@Serializable
internal data class PrakNearestRequest(
    val frames: List<PrakNearestPoint>,
    @SerialName("max_distance_ms") val maxDistanceMs: Double,
    val dataset: String,
    val model: String
)

@Serializable
internal data class PrakNearestPoint(val video: String, @SerialName("time_ms") val timeMs: Double)

@Serializable
internal data class PrakNearestResponse(val frames: List<PrakNearestMatch>)

@Serializable
internal data class PrakNearestMatch(
    val video: String,
    @SerialName("time_ms") val timeMs: Double,
    val id: List<String>? = null,
    @SerialName("frame_time_ms") val frameTimeMs: Double? = null,
    @SerialName("distance_ms") val distanceMs: Double? = null
)

internal data class PrakSelectedPoint(val segmentId: UUID, val video: String, val startMs: Double, val endMs: Double) {
    val timeMs get() = startMs + (endMs - startMs) / 2
}

internal data class PrakResolvedPool(val points: List<PrakSelectedPoint>, val failures: Map<UUID, String>)

/** Resolve local-only selections using stored video identity and video-relative temporal metadata. */
internal class PrakFeedbackResolver(private val schema: Schema, fileField: String, timeField: String, recordingField: String) {
    private val files = requireNotNull(schema[fileField])
    private val times = requireNotNull(schema[timeField])
    private val recording = schema[recordingField]

    init {
        require(files.analyser.descriptorClass == FileSourceMetadataDescriptor::class)
        require(times.analyser.descriptorClass == TemporalMetadataDescriptor::class)
        require(recording == null || recording.analyser.descriptorClass == RecordingMetadataDescriptor::class)
    }

    fun resolve(ids: Set<UUID>): List<PrakSelectedPoint> {
        val pool = resolvePool(ids)
        require(pool.failures.isEmpty()) { pool.failures.values.joinToString(" ") }
        return pool.points
    }

    fun resolvePool(ids: Set<UUID>): PrakResolvedPool {
        if (ids.isEmpty()) return PrakResolvedPool(emptyList(), emptyMap())
        val failures = mutableMapOf<UUID, String>()
        val reader = schema.connection.getRetrievableReader()
        val existing = readPrakIdBatches(ids) { reader.getAll(it).toList() }.map { it.id }.toSet()

        val parents = readPrakIdBatches(ids) { reader.getConnections(it, listOf("partOf"), emptyList()).toList() }
            .groupBy({ it.subjectId }, { it.objectId })
        val related = ids + parents.values.flatten()
        val paths = readPrakIdBatches(related) { files.getReader().getAllForRetrievable(it).filterIsInstance<FileSourceMetadataDescriptor>().toList() }
            .groupBy { it.retrievableId }
        val recordings = if (recording == null) emptyMap() else readPrakIdBatches(related) {
            recording.getReader().getAllForRetrievable(it).filterIsInstance<RecordingMetadataDescriptor>().toList()
        }.groupBy { it.retrievableId }
        val temporal = readPrakIdBatches(ids) { times.getReader().getAllForRetrievable(it).filterIsInstance<TemporalMetadataDescriptor>().toList() }
            .groupBy { it.retrievableId }
        val points = ids.sortedBy(UUID::toString).flatMap { id ->
            if (id !in existing) {
                failures[id] = "Feedback segment $id no longer exists."
                return@flatMap emptyList()
            }
            val objects = setOf(id) + parents[id].orEmpty()
            val videos = objects.flatMap { objectId -> paths[objectId].orEmpty().flatMap { path ->
                val provenance = recordings[objectId].orEmpty().ifEmpty { objects.flatMap { recordings[it].orEmpty() } }
                if (provenance.isEmpty()) listOfNotNull(video(path.path.value, null, null)) else
                    provenance.mapNotNull { video(path.path.value, it.day.value, it.source.value) }
            } }.distinct()
            if (videos.size != 1) {
                failures[id] = "Cannot resolve one unambiguous PraK video for segment $id."
                return@flatMap emptyList()
            }
            val ranges = temporal[id].orEmpty().map { it.start.value to it.end.value }.distinct().sortedBy { it.first }
            if (ranges.isEmpty() || ranges.any { (start, end) -> start < 0 || end < start }) {
                failures[id] = "Segment $id has no valid video-relative timestamps."
                return@flatMap emptyList()
            }
            ranges.map { (start, end) -> PrakSelectedPoint(id, videos.single(), start / 1_000_000.0, end / 1_000_000.0) }
        }
        return PrakResolvedPool(points, failures)
    }

    companion object {
        internal fun video(path: String, day: String?, source: String?): String? {
            val basename = path.substringAfterLast('/')
            if (!basename.endsWith(".mp4")) return null
            val stem = basename.removeSuffix(".mp4")
            if (PrakSegmentMapper.recording(stem) != null) return stem
            if (!stem.matches(Regex("[0-9]{1,2}"))) return null
            if (day != null && source != null) return "${day}_${source}_$stem".takeIf { PrakSegmentMapper.recording(it) != null }
            val parts = path.split('/')
            val starts = parts.indices.filter { it + 2 < parts.size && parts[it].matches(Regex("day[1-4]")) }
            return starts.singleOrNull()?.let { "${parts[it]}_${parts[it + 1]}_$stem" }
        }

        /** Appended frames use the weakest positive visual prior; local OCR/ASR scores are not converted to SigLIP scores. */
        internal fun expand(feedback: PrakFeedback, points: List<PrakSelectedPoint>, matches: List<PrakNearestMatch>, maxDistance: Double): Pair<PrakFeedback, List<List<String>>> {
            require(matches.size == points.size) { "PraK nearestFrames returned an incomplete mapping." }
            val candidates = feedback.candidates.toMutableList()
            val known = candidates.map { it.id }.toMutableSet()
            val prior = candidates.mapNotNull { it.score.toDoubleOrNull()?.takeIf { score -> score.isFinite() && score > 0 } }.minOrNull() ?: 1e-6
            val selected = points.zip(matches).map { (point, match) ->
                val id = match.id
                val time = match.frameTimeMs
                require(match.video == point.video && match.timeMs == point.timeMs && id?.size == 2 && id[0] == point.video &&
                    time != null && time.isFinite() && match.distanceMs?.let { it.isFinite() && it >= 0 && it <= maxDistance } == true &&
                    kotlin.math.abs(time - point.timeMs) <= maxDistance &&
                    (if (point.startMs == point.endMs) time == point.startMs else point.startMs <= time && time < point.endMs)) {
                    "No PraK keyframe within the selected segment ${point.segmentId} and the allowed distance."
                }
                val frameId = requireNotNull(id)
                if (known.add(frameId)) candidates.add(PrakApiResult(id = frameId, score = prior.toString(), rank = candidates.size.toString(),
                    time = listOf(frameId.joinToString("_"), time.toString(), time.toString(), time.toString())))
                frameId
            }.distinct()
            return feedback.copy(candidates = candidates) to selected
        }
    }
}
