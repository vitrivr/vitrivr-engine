package org.vitrivr.engine.index.segment

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory

/**
 * A [SegmenterFactory] for the [PassThroughSegmenter].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PassThroughSegmenterFactory : SegmenterFactory {
    override fun newOperator(input: Operator<ContentElement<*>>, parameters: Map<String, Any>, schema: Schema) = PassThroughSegmenter(input, schema.connection.getRetrievableWriter())
}