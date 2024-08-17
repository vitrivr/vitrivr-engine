package org.vitrivr.engine.query.model.input

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.query.model.api.input.InputData

interface InputDataTransformerFactory {

    fun newTransformer(name: String, inputs: List<InputData>, schema: Schema, context: Context): InputDataTransformer

}