package org.vitrivr.engine.query.model.api

import kotlinx.serialization.Serializable


@Serializable
data class IndexInformationNeedDescription (
        val pipeline: String,
)