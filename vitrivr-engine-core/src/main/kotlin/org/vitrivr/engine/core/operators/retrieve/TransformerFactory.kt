package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

interface TransformerFactory {

    fun newTransformer(input: Operator<Retrieved>, schema: Schema, properties: Map<String, String>) : Transformer

}