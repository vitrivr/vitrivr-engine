package org.vitrivr.engine.module.features.feature.external.implementations.prak

import kotlinx.serialization.Serializable

@Serializable
data class PrakApiResult(
    val uri: String = "",
    val rank: String = "",
    val score: String = "",
    val id: List<String> = emptyList(),
    val label: List<String> = emptyList(),
    val time: List<String> = emptyList()
) {
    val rankInt: Int
        get() = rank.toIntOrNull() ?: Int.MAX_VALUE

    val scoreDouble: Double
        get() = score.toDoubleOrNull() ?: 0.0
}