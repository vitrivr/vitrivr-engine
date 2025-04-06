package org.vitrivr.engine.query.model.api

import kotlinx.serialization.Serializable
import org.vitrivr.engine.query.model.api.input.InputData
import org.vitrivr.engine.query.model.api.operator.OperatorDescription


@Serializable
data class InformationNeedDescription(
    val inputs: Map<String, InputData>,
    val operations: Map<String, OperatorDescription>,
    val output: String
)
