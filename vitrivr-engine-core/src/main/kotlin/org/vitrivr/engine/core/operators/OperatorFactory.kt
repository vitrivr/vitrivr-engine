package org.vitrivr.engine.core.operators

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.database.retrievable.Ingested
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.Source

interface OperatorFactory {
    fun createOperator(): Operator<*>

    interface EnumeratorFactory : OperatorFactory {
        override fun createOperator(): Enumerator
    }

    interface DecoderFactory : OperatorFactory {
        override fun createOperator(): Decoder
        fun setSource(source: Operator<Source>): DecoderFactory
    }

    interface TransformerFactory : OperatorFactory {
        override fun createOperator(): Transformer
        fun setSource(source: Operator<ContentElement<*>>): TransformerFactory
    }

    interface SegmenterFactory : OperatorFactory {
        override fun createOperator(): Segmenter
        fun setSource(source: Operator<ContentElement<*>>): SegmenterFactory
    }

    interface ExtractorFactory : OperatorFactory {
        override fun createOperator(): Extractor<*, *>
        fun setSource(source: Operator<Ingested>): ExtractorFactory
    }
}