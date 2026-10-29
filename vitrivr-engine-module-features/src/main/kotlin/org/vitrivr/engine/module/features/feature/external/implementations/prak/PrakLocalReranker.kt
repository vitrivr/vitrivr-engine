package org.vitrivr.engine.module.features.feature.external.implementations.prak

import java.util.UUID

/** PraK is only a feedback service: all candidates originate in the local result pool. */
internal class PrakLocalReranker(private val resolve: (Set<UUID>) -> PrakResolvedPool) {
    suspend fun rerank(
        feedback: PrakFeedback, dataset: String, model: String, maxDistance: Double,
        post: suspend (String, String) -> String
    ): PrakFeedback {
        val ids = feedback.localCandidates
        require(ids.isNotEmpty() && ids.distinct().size == ids.size) { "Supply a unique ordered local candidate pool." }
        ids.forEach(UUID::fromString)
        require(feedback.selectedRetrievableIds.isNotEmpty() && feedback.selectedRetrievableIds.all { it in ids }) {
            "Select relevant segments from the saved local candidate pool."
        }
        require(feedback.selectedImages.isEmpty()) { "Local feedback selects local segment IDs." }
        require(maxDistance.isFinite() && maxDistance >= 0)
        val prepared = if (feedback.localPoolMapped) feedback else {
            require(feedback.candidates.isEmpty() && feedback.localMappings.isEmpty() && feedback.unmappedLocalIds.isEmpty()) {
                "An unmapped local pool cannot contain remote candidate state."
            }
            val pool = resolve(ids.map(UUID::fromString).toSet())
            // One representative per local result, including results with multiple stored intervals.
            val pointsById = pool.points.groupBy { it.segmentId }
            val points = ids.mapNotNull { id -> pointsById[UUID.fromString(id)]?.minByOrNull { it.startMs } }
            val nearest = points.chunked(1000).flatMap { chunk ->
                PrakProtocol.json.decodeFromString<PrakNearestResponse>(post("nearestFrames", PrakProtocol.json.encodeToString(
                    PrakNearestRequest(chunk.map { PrakNearestPoint(it.video, it.timeMs) }, maxDistance, dataset, model)))).frames
            }
            require(nearest.size == points.size) { "PraK nearestFrames returned an incomplete local-pool mapping." }
            val failures = pool.failures.mapKeys { it.key.toString() }.toMutableMap()
            val priors = ids.withIndex().associate { it.value to (1.0 / (it.index + 1)).toString() }
            val mappings = linkedMapOf<String, List<String>>()
            val frames = linkedMapOf<List<String>, PrakApiResult>()
            points.zip(nearest).forEach { (point, match) ->
                // Reuse strict identity, distance, and in-segment validation; no external failures are swallowed.
                val frame = try {
                    PrakFeedbackResolver.expand(PrakFeedback(), listOf(point), listOf(match), maxDistance).first.candidates.single()
                } catch (error: IllegalArgumentException) {
                    failures[point.segmentId.toString()] = error.message ?: "No in-segment PraK keyframe."
                    null
                }
                if (frame != null) {
                    val localId = point.segmentId.toString()
                    mappings[localId] = frame.id
                    frames.putIfAbsent(frame.id, frame.copy(score = priors.getValue(localId), rank = frames.size.toString()))
                }
            }
            ids.filterNot { it in mappings || it in failures }.forEach { failures[it] = "No usable video-relative point for segment $it." }
            feedback.copy(candidates = frames.values.toList(), localMappings = mappings, unmappedLocalIds = failures, localPoolMapped = true)
        }
        require(prepared.localMappings.keys.intersect(prepared.unmappedLocalIds.keys).isEmpty() &&
            prepared.localMappings.keys + prepared.unmappedLocalIds.keys == ids.toSet()) { "Incomplete or conflicting local-pool state." }
        require(prepared.localMappings.values.toSet() == prepared.candidates.map { it.id }.toSet()) { "Local/frame mapping does not match the remote candidates." }
        val unmappedSelections = feedback.selectedRetrievableIds.filter { it !in prepared.localMappings }
        require(unmappedSelections.isEmpty()) {
            unmappedSelections.joinToString(" ") { prepared.unmappedLocalIds[it] ?: "Selected segment $it cannot be mapped to PraK." }
        }
        val selected = feedback.selectedRetrievableIds.map { prepared.localMappings.getValue(it) }.distinct()
        val request = PrakProtocol.bayes(prepared, selected, dataset, model)
        val results = PrakProtocol.results(post("bayes", PrakProtocol.json.encodeToString(request)))
        require(results.size == prepared.candidates.size && results.map { it.id }.toSet() == prepared.candidates.map { it.id }.toSet()) {
            "PraK Bayes returned an empty or incomplete local candidate list; retain the previous ranking."
        }
        return prepared.copy(candidates = results, selectedRetrievableIds = emptyList(), selectedImages = emptyList())
    }

    companion object {
        fun order(state: PrakFeedback): List<String> = state.candidates.flatMap { frame ->
            state.localCandidates.filter { state.localMappings[it] == frame.id }
        } + state.localCandidates.filter { it in state.unmappedLocalIds }
    }
}
