package org.vitrivr.engine.core.model.database.retrievable

import org.vitrivr.engine.core.model.database.Persistable
import java.util.*

/** A typealias to identify the [UUID] identifying a [Retrievable]. */
typealias RetrievableId = UUID

/**
 * A [Persistable] and [Retrievable] unit of information stored by vitrivr.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Retrievable : Persistable {

    /** The [RetrievableId] held by this [Retrievable]. */
    override val id: RetrievableId

    /** The type of this [Retrievable]. This is basically a string that can help to keep apart different types of [Retrievable]. */
    val type: String?


}