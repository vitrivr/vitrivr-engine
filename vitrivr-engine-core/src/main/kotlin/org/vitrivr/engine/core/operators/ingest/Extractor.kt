package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that extracts [Descriptor]s from a [Retrievable] based on a [Analyser] and appends them to the [Ingested].
 *
 * Typically, an [Extractor] simply enriches the [Retrievable] with additional information, without modifying the [Flow]. Some [Extractor]s
 * may be persisting information using the database layer.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.2.0
 */
interface Extractor<C : ContentElement<*>, D : Descriptor<*>> : Operator.Unary<Retrievable, Retrievable> {
    /** The [Schema.Field] populated by this [Extractor]. */
    val field: Schema.Field<C, D>?

    /** The [Analyser] instance this [Extractor] belongs to. */
    val analyser: Analyser<C, D>

    /** The name of this [Extractor]. In case a field is set, is equivalent to field.fieldName.*/
    val name: String

    /** Flag indicating, that this [Extractor] is persisting information. */
    val persisting: Boolean
        get() = this.field != null
}