package org.vitrivr.engine.query.operators.transform.relation

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

/**
 * [TransformerFactory] for the [RelationResolver]
 */
class RelationResolverFactory: TransformerFactory {
    /**
     * Creates a new [Transformer] instance from this [TransformerFactory].
     *
     * @param name The name of the [Transformer]
     * @param input The input [Operator].
     * @param context The [Context] to use.
     */
    override fun newTransformer(
        name: String,
        input: Operator<out Retrievable>,
        context: Context
    ): Transformer {
        val retrievableReader = context.schema.connection.getRetrievableReader()
        val predicate = context[name, "predicate"]
        require(predicate?.isNotBlank() == true){"Requires a non-blank predicate!"}
        return RelationResolver(input, predicate!! , retrievableReader)
    }
}
