package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.operators.OperatorFactory

class DummyEnumeratorFactory : OperatorFactory.EnumeratorFactory {

    override fun createOperator(): DummyEnumerator {
        return DummyEnumerator()
    }
}