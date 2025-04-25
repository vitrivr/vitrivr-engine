package org.vitrivr.engine.core.context

import org.vitrivr.engine.core.model.content.factory.ContentFactory
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.resolver.Resolver

/**
 * The [IndexContext] holds all information related to an indexing / extraction job.
 *
 * @author Raphael Waltenspuel
 * @version 1.1.0
 */
data class IndexContext(
    /** The [Schema] used by this [IndexContext] */
    override val schema: Schema,

    /** The [ContentFactory] used by this [IndexContext] */
    val contentFactory: ContentFactory,

    /** A [Map] of named [Resolver]s. */
    val resolver: Map<String,Resolver> = emptyMap(),

    /** Local (per-operator) configuration parameters. */
    override val local: Map<String, Map<String, String>> = emptyMap(),

    /** Global configuration parameters. */
    override val global: Map<String, String> = emptyMap(),
) : Context()
