package org.vitrivr.engine.core.context

import org.vitrivr.engine.core.config.pipeline.ContextConfig
import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.content.impl.InMemoryContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.SegmenterFactory
import org.vitrivr.engine.core.source.Source
import org.vitrivr.engine.index.decode.ImageDecoder
import java.util.*

class ContextFactory {
    fun newContext(contextConfig: ContextConfig): Context {

        val contentFactory = (ServiceLoader.load(ContentFactory::class.java).find {
            it.javaClass.name == "${it.javaClass.packageName}.${contextConfig.contentFactory}Factory"
        }
            ?: throw IllegalArgumentException("Failed to find Segmenter implementation for '${contextConfig.contentFactory}'."))

        return Context(contentFactory)
    }
}