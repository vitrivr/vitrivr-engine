package org.vitrivr.engine.query.operators.transform.benchmark

import kotlinx.serialization.Serializable

@Serializable
data class BenchmarkMessage (
    val id: Int,
    val name: String,
    val timestamp: String,
)