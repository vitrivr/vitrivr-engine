package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.operators.ingest.ResolverFactory

class DiskResolverFactory : ResolverFactory {
    override val name: String = "ImageOnDiskResolver"
    override fun newResolver(parameters: Map<String, Any>): Resolver{
        val location = parameters["location"] as? String ?: "./thumbnails"
        return DiskResolver(location = location)
    }
}