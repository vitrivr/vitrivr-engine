package org.vitrivr.engine.query.operators.transform.benchmark

import kotlinx.serialization.Serializable

@Serializable
data class BenchmarkMessage (
    val name: String,
    val source: String,
    val timestamp: String
)