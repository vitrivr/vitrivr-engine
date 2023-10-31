package org.vitrivr.engine.core.context

import org.vitrivr.engine.core.content.ContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IndexContext] holds all information related to an indexing / extraction job.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
data class IndexContext(val schema: Schema, val contentFactory: ContentFactory, val resolver: Resolver)