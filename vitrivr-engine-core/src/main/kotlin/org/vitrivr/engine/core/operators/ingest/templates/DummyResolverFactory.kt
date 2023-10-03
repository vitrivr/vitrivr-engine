package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.operators.ingest.ResolverFactory

class DummyResolverFactory : ResolverFactory {
    override val name: String = "DummyResolver"

    override fun newOperator(parameters: Map<String, Any>): Resolver {
        return DummyResolver()
    }
}