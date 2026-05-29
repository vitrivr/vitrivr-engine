package org.vitrivr.engine.module.features.feature.external.implementations.prak



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrakQueryRequest(
    val k: Int = 100,
    val dataset: String = "V3C",
    val model: String = "clip-vit-so400m",

    @SerialName("max_labels")
    val maxLabels: Int = 10,

    @SerialName("add_features")
    val addFeatures: Boolean = false,

    @SerialName("speed_up")
    val speedUp: Boolean = true,

    val filters: Map<String, String> = emptyMap(),
    val queries: String
)

@Serializable
data class PrakSearchResult(
    val id: String,
    val score: Double
)