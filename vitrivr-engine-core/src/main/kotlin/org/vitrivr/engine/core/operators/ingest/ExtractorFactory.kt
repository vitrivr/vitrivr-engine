package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

interface ExtractorFactory : OperatorFactory {
    override fun createOperator(): Extractor<*, *>
    fun setSource(source: Operator<Ingested>): ExtractorFactory
}