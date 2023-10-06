package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Exporter
import org.vitrivr.engine.core.operators.ingest.ExporterFactory
import org.vitrivr.engine.core.operators.ingest.Transformer
import org.vitrivr.engine.core.operators.ingest.TransformerFactory

class DummyExporterFactory : ExporterFactory {
    override fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>,schema: Schema): Exporter {
        return DummyExporter(input, parameters)
    }
}