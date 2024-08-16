package org.vitrivr.engine.query.operators.input

import org.vitrivr.engine.core.context.Context
import org.vitrivr.engine.query.model.api.input.InputData

interface InputDataTransformerFactory {

    fun newTransformer(name: String, inputs: List<InputData>, context: Context): InputDataTransformer

}