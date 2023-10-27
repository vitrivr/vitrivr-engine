package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Initializer
import org.vitrivr.engine.core.model.retrievable.Retrievable

/**
 * A [RetrievableInitializer] is an extension of a [Int] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableInitializer: Initializer<Retrievable>