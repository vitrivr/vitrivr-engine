package org.vitrivr.engine.module.features.feature.external.implementations.prak

import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute


data class PrakResultAttribute(
    val uri: String,
    val rank: Int,
    val score: Double,
    val id: List<String>,
    val labels: List<String>,
    val time: List<String>
) : RetrievableAttribute {

    fun toPropertyAttribute(): PropertyAttribute = PropertyAttribute(
        mapOf(
            "prak.uri" to uri,
            "prak.rank" to rank.toString(),
            "prak.score" to score.toString(),
            "prak.id" to id.joinToString(","),
            "prak.labels" to labels.joinToString(","),
            "prak.time" to time.joinToString(",")
        )
    )
}