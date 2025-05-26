package org.vitrivr.engine.query.operators.transform.relation

import org.vitrivr.engine.core.context.Context
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
    override fun newTransformer(
        name: String, input: Operator<out Retrievable>, parameters: Map<String, String>,
        context: Context
    ): Transformer {
        require(context is Context)
        val retrievableReader = context.schema.connection.getRetrievableReader()
        val incomingRelations = context[name, "incoming"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        val outgoingRelations = context[name, "outgoing"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        return RelationExpander(input, incomingRelations, outgoingRelations, retrievableReader, name)
    }
}