package org.vitrivr.engine.query.operators.transform.relation

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory


/**
 * [TransformerFactory] for the [RelationExpander].
 *
 * @version 1.0.0
 * @author Luca Rossetto
 */
class RelationExpanderFactory : TransformerFactory {
    override fun newTransformer(name: String, input: Operator<out Retrievable>, schema: Schema, properties: Map<String, String>): Transformer {
        val retrievableReader = schema.connection.getRetrievableReader()
        val incomingRelations = properties["incoming"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        val outgoingRelations = properties["outgoing"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        return RelationExpander(input, incomingRelations, outgoingRelations, retrievableReader, name)
    }
}