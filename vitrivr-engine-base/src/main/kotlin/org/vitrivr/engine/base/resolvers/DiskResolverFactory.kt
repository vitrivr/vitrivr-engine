package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.operators.resolver.Resolver
import org.vitrivr.engine.core.operators.resolver.ResolverFactory
import org.vitrivr.engine.core.source.file.MimeType
import java.nio.file.Paths

/**
 * A [ResolverFactory] for the [DiskResolver]
 *
 * @author Fynn Faber
 * @version 1.0.0
 */
class DiskResolverFactory : ResolverFactory {
    override fun newResolver(parameters: Map<String, Any>): Resolver {
        val location = Paths.get(parameters["location"] as? String ?: "./thumbnails")
        val mimeType = MimeType.valueOf(parameters["mimeType"] as? String ?: "JPG")
        return DiskResolver(location, mimeType)
    }
}