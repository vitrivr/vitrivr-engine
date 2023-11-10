package org.vitrivr.engine.query.model.api

import kotlinx.serialization.Serializable
import org.vitrivr.engine.query.model.api.input.InputData


@Serializable
data class PipelineInformationNeedDescription (
        val inputs: Map<String, InputData>,
        val pipeline: String,
        val output: String
)