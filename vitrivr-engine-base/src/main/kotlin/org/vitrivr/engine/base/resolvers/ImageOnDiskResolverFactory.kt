package org.vitrivr.engine.base.resolvers

import org.vitrivr.engine.core.operators.ingest.Resolver
import org.vitrivr.engine.core.operators.ingest.ResolverFactory

class ImageOnDiskResolverFactory : ResolverFactory {
    override val name: String = "ImageOnDiskResolver"
    override fun newOperator(parameters: Map<String, Any>): Resolver{
        val location = parameters["location"] as? String ?: "./thumbnails"
        val formatStr = parameters["format"] as? String ?: "jpg"
        val format = try {
            ImageResolverFormat.valueOf(formatStr.uppercase())
        } catch (e: IllegalArgumentException) {
            ImageResolverFormat.JPG
        }
        return ImageOnDiskResolver(format = format, location = location)
    }
}