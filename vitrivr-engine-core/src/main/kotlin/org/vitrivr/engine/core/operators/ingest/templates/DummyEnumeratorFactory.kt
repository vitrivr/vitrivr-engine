package org.vitrivr.engine.core.operators.ingest.templates
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory

class DummyEnumeratorFactory : EnumeratorFactory {

    override fun createOperator(): DummyEnumerator {
        return DummyEnumerator()
    }
}