package org.vitrivr.engine.core.context

import kotlinx.serialization.Transient
import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IndexContext] holds all information related to an indexing / extraction job.
 *
 * @author Raphael Waltenspuel
 * @version 1.0.0
 */
data class IndexContext(
    @Transient
    override val schema: Schema,

    val contentFactory: ContentFactory,

    val resolver: Resolver,
    override val local: Map<String, Map<String, String>>,
    override val global: Map<String, String>
) : Context()
