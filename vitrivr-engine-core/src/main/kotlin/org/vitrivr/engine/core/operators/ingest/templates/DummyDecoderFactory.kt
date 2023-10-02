package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.source.Source

class DummyDecoderFactory : DecoderFactory {

    var input: Operator<Source>? = null

    override fun setSource(source: Operator<Source>) : DummyDecoderFactory {
        this.input = source
        return this
    }
    override fun createOperator(): DummyDecoder {
        return DummyDecoder(this.input!!)
    }
}