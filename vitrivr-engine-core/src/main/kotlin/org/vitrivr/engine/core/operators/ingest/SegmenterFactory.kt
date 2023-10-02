package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory

interface SegmenterFactory : OperatorFactory {
    override fun createOperator(): Segmenter
    fun setSource(source: Operator<ContentElement<*>>): SegmenterFactory
}