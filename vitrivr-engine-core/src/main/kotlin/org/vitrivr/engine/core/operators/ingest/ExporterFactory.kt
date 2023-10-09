package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator

interface ExporterFactory{
    val name: String

    fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>, schema: Schema, resolver: Resolver): Exporter

}