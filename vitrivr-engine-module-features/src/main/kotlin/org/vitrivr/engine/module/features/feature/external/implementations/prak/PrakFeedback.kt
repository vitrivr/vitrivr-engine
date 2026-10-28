package org.vitrivr.engine.module.features.feature.external.implementations.prak

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/** Stateless feedback: retain the entire frame list, including frames collapsed onto one local segment. */
@Serializable
data class PrakFeedback(
    val candidates: List<PrakApiResult>,
    @SerialName("selected_images") val selectedImages: List<List<String>> = emptyList(),
    val selectedRetrievableIds: List<String> = emptyList(),
    @SerialName("max_rank") val maxRank: Int? = null
)

@Serializable
internal data class PrakBayesRequest(
    val ids: List<List<String>>,
    val scores: List<String>,
    @SerialName("selected_images") val selectedImages: List<List<String>>,
    @SerialName("max_rank") val maxRank: Int,
    val dataset: String,
    val model: String
)

internal object PrakProtocol {
    val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    fun results(body: String): List<PrakApiResult> = json.parseToJsonElement(body).jsonArray
        .filterNot { it.jsonObject.containsKey("T4_dataServiceProcessedQuery") || it.jsonObject.containsKey("T3_dataServiceReceived") }
        .map { json.decodeFromJsonElement<PrakApiResult>(it) }
        .onEach {
            require(it.id.size == 2 && it.time.size >= 2 && it.time[1].toDoubleOrNull()?.let { t -> t.isFinite() && t >= 0 } == true &&
                it.score.toDoubleOrNull()?.isFinite() == true) { "Malformed PraK result: ${it.id}" }
        }.sortedBy { it.rankInt }

    fun bayes(feedback: PrakFeedback, selected: List<List<String>>, dataset: String, model: String): PrakBayesRequest {
        require(feedback.candidates.isNotEmpty()) { "Bayes requires the previous candidate list." }
        val ids = feedback.candidates.map { it.id }
        require(ids.all { it.size == 2 } && ids.distinct().size == ids.size) { "Invalid or duplicate PraK candidate IDs." }
        require(feedback.candidates.all { it.score.toDoubleOrNull()?.isFinite() == true }) { "Invalid candidate scores." }
        val positives = selected.distinct()
        require(positives.isNotEmpty() && positives.all { it in ids }) { "Select at least one frame from the candidate list." }
        val maxRank = feedback.maxRank ?: (ids.indexOfLast { it in positives } + 5).coerceAtMost(ids.size)
        require(maxRank in 1..ids.size && ids.take(maxRank).any { it !in positives }) { "Bayes requires an unselected candidate before max_rank." }
        return PrakBayesRequest(ids, feedback.candidates.map { it.score }, positives, maxRank, dataset, model)
    }
}
