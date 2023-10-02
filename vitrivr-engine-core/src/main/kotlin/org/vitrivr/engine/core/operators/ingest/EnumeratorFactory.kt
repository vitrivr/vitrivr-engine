package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.operators.OperatorFactory

interface EnumeratorFactory : OperatorFactory {
    override fun createOperator(): Enumerator
}
