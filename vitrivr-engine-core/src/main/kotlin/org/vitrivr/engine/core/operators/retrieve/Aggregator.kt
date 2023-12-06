package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

interface Aggregator<I : Retrieved, O : Retrieved> : Operator.NAry<I, O>