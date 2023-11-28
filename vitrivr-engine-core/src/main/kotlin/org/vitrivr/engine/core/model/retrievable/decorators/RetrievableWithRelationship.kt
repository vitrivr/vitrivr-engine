package org.vitrivr.engine.core.model.retrievable.decorators

import org.vitrivr.engine.core.model.retrievable.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [Retrievable] with relationships to other [Retrievable]s
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableWithRelationship : Retrievable {
    /** [Map] of all relationships of this [Retrievable]. */
    val relationships: Set<Relationship>
}