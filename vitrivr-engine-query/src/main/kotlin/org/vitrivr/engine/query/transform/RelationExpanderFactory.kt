package org.vitrivr.engine.query.transform

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Transformer
import org.vitrivr.engine.core.operators.retrieve.TransformerFactory

class RelationExpanderFactory : TransformerFactory<Retrieved, Retrieved.RetrievedWithRelationship> {
    override fun newTransformer(
        input: Operator<Retrieved>,
        schema: Schema,
        properties: Map<String, String>
    ): Transformer<Retrieved, Retrieved.RetrievedWithRelationship> {

        val retrievableReader = schema.connection.getRetrievableReader()
        val incomingRelations = properties["incoming"]?.split(",")?.map { s -> s.trim() } ?: emptyList()
        val outgoingRelations = properties["outgoing"]?.split(",")?.map { s -> s.trim() } ?: emptyList()

        return RelationExpander(
            input, incomingRelations, outgoingRelations, retrievableReader
        )
    }
}