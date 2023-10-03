package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.*

class DummyExporterFactory : ExporterFactory {

    override val name: String = "DummyExporter"
    override fun newOperator(input: Operator<Ingested>, parameters: Map<String, Any>, resolver: Resolver): Exporter {
        return DummyExporter(input, parameters)
    }
}