package org.vitrivr.engine.core.context

import org.vitrivr.engine.core.config.pipeline.ContextConfig
import org.vitrivr.engine.core.content.ContentFactory
import java.util.*

class IndexContextFactory {
    fun newContext(contextConfig: ContextConfig): IndexContext {

        val contentFactory = (ServiceLoader.load(ContentFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${contextConfig.contentFactory}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${contextConfig.contentFactory}'."))

        return IndexContext(contentFactory)
    }
}