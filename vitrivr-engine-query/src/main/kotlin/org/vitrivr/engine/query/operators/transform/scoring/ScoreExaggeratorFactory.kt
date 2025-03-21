package org.vitrivr.engine.query.operators.transform.scoring

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.general.Transformer
import org.vitrivr.engine.core.operators.general.TransformerFactory

class ScoreExaggeratorFactory : TransformerFactory{
    override fun newTransformer(name: String, input: Operator<out Retrievable>, context: Context): Transformer {
        val factor = context[name, "factor"]?.toDoubleOrNull() ?: throw IllegalArgumentException("Property 'factor' must be specified")
        return ScoreExaggerator(input, factor, name)
    }
}