package org.vitrivr.engine.index.decode

import org.vitrivr.engine.core.content.impl.InMemoryContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.source.Source

/**
 * A [DecoderFactory] for the [ImageDecoder].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class ImageDecoderFactory : DecoderFactory {
    override fun newOperator(input: Operator<Source>, parameters: Map<String, Any>, schema: Schema): ImageDecoder {
        val contentFactory = InMemoryContentFactory()
        return ImageDecoder(input, contentFactory)
    }
}