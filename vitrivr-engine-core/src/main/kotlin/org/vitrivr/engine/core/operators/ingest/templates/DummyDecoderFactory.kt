package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.OperatorFactory
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.source.Source

class DummyDecoderFactory : DecoderFactory {
    override fun newOperator(input: Operator<Source>, parameters: Map<String, Any>, schema: Schema, context: Context): Decoder {
        return DummyDecoder(input, parameters)
    }
}