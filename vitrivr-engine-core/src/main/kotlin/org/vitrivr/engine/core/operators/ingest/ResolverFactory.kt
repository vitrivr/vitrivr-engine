package org.vitrivr.engine.core.operators.ingest

interface ResolverFactory {

    val name: String
    fun newResolver(parameters: Map<String, Any>): Resolver
}