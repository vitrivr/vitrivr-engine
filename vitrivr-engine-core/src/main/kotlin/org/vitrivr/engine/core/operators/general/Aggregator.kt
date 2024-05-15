package org.vitrivr.engine.core.operators.general

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

interface Aggregator : Operator.NAry<Retrievable, Retrievable>