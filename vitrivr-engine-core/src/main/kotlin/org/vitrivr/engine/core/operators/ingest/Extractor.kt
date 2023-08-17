package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Describer
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that extracts [Descriptor]s from a [IngestedRetrievable] based on a [Describer] and appends them to the [IngestedRetrievable].
 *
 * Typically, an [Extractor] simply enriches the [Retrievable] with additional information, without modifying the [Flow]. Some [Extractor]s
 * may be persisting information using the database layer.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Extractor: Operator.Unary<Retrievable, IngestedRetrievable>, Describer {
    /** Flag indicating, that this [Extractor] is persisting information. */
    val persisting: Boolean
}