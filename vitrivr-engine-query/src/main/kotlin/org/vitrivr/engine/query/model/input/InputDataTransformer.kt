package org.vitrivr.engine.query.model.input

import org.vitrivr.engine.query.model.api.input.InputData

interface InputDataTransformer {

        fun transform(): InputData

}