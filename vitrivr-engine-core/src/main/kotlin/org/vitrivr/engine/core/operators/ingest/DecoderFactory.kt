package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.source.Source

interface DecoderFactory : OperatorFactory {
    override fun createOperator(): Decoder
    fun setSource(source: Operator<Source>): DecoderFactory
}