package org.vitrivr.engine.core.model.retrievable.decorators

import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [Retrievable] with arbitrary Properties
 *
 * @version 1.0.0
 */
interface RetrievableWithProperties : Retrievable {

    val properties: Map<String, String>

}