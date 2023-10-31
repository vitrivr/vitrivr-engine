package org.vitrivr.engine.core.model.retrievable.decorators

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.source.Source

/**
 * A decorator for [Retrievable]s that allows to specify a [Source] for the [Retrievable].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithSource : Retrievable {
    /** The [Source] this [RetrievableWithSource] is referring to. */
    val source: Source
}