package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.resolver.Resolver

/**
 *
 */
interface ExporterFactory {
    fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>, schema: Schema, resolver: Resolver): Exporter

}