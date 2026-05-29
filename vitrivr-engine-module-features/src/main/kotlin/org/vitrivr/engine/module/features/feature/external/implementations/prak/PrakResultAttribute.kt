package org.vitrivr.engine.module.features.feature.external.implementations.prak

import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute

data class PrakResultAttribute(
    val uri: String,
    val rank: Int,
    val score: Double,
    val id: List<String>,
    val labels: List<String>,
    val time: List<String>
) : RetrievableAttribute