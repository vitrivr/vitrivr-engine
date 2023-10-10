package org.vitrivr.engine.core.operators.ingest.templates

import org.vitrivr.engine.core.operators.resolver.Resolver
import org.vitrivr.engine.core.operators.resolver.ResolverFactory

class DummyResolverFactory : ResolverFactory {
    override fun newResolver(parameters: Map<String, Any>): Resolver {
        return DummyResolver()
    }
}