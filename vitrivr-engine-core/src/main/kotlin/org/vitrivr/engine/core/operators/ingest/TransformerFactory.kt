package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

interface TransformerFactory : OperatorFactory {
    override fun createOperator(): Transformer
    fun setSource(source: Operator<ContentElement<*>>): TransformerFactory
}