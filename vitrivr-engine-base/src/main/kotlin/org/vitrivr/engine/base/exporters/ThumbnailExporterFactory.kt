package org.vitrivr.engine.base.exporters

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.operators.ingest.Resolver

class ThumbnailExporterFactory : ExporterFactory {

    override val name: String = "ThumbnailExporter"

    override fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>, resolver: Resolver): Exporter {
        val maxSideResolution = parameters["maxSideResolution"] as? Int ?: 100
        return ThumbnailExporter(input = input, maxSideResolution = maxSideResolution, resolver = resolver)
    }
}