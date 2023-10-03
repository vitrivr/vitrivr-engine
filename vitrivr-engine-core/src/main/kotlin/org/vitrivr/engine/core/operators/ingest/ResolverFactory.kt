package org.vitrivr.engine.core.operators.ingest

interface ResolverFactory {

    val name: String
    fun newOperator(parameters: Map<String, Any>): Resolver
}