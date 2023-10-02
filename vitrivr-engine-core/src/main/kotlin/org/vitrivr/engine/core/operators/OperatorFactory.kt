package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source

interface OperatorFactory {
    fun createOperator(): Operator<*>
}