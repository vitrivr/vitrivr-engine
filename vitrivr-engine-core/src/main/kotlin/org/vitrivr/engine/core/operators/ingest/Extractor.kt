package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.retrieve.Retriever

/**
 * An [Operator.Unary] that extracts [Descriptor]s from a [IngestedRetrievable] based on a [Analyser] and appends them to the [IngestedRetrievable].
 *
 * Typically, an [Extractor] simply enriches the [Retrievable] with additional information, without modifying the [Flow]. Some [Extractor]s
 * may be persisting information using the database layer.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Extractor<T: Descriptor>: Operator.Unary<IngestedRetrievable, IngestedRetrievable> {
    /** The [Analyser] this [Retriever] implements. */
    val analyser: Analyser<T>

    /** The [Schema.Field] populated by this [Extractor]. */
    val field: Schema.Field<T>

    /** Flag indicating, that this [Extractor] is persisting information. */
    val persisting: Boolean
}