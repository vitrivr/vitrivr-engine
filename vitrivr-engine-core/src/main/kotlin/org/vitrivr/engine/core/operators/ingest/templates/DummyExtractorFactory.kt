package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

class DummyExtractorFactory : OperatorFactory.ExtractorFactory {

    var input: Operator<Ingested>? = null

    override fun setSource(source: Operator<Ingested>) : DummyExtractorFactory {
        this.input = source
        return this
    }
    override fun createOperator(): DummyExtractor {
        return DummyExtractor(this.input!!)
    }
}