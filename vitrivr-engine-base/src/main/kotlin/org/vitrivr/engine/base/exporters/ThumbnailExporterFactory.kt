package org.vitrivr.engine.base.exporters

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.operators.resolver.Resolver
import org.vitrivr.engine.core.source.file.MimeType

/**
 * Factory for [ThumbnailExporter] instances.
 *
 * @author Finn Faber
 * @version 1.0.0
 */
class ThumbnailExporterFactory : ExporterFactory {
    override fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>, schema: Schema, resolver: Resolver): Exporter {
        val maxSideResolution = parameters["maxSideResolution"] as? Int ?: 100
        val mimeType = parameters["mimeType"] as? MimeType ?: MimeType.JPG
        return ThumbnailExporter(input = input, maxSideResolution = maxSideResolution, resolver = resolver, mimeType = mimeType)
    }
}