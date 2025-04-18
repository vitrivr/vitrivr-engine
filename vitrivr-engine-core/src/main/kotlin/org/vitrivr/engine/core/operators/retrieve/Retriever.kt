package org.vitrivr.engine.core.operators.retrieve

import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Nullary] that facilitates retrieval based on a [Analyser].
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.1.0
 */
interface Retriever<C : ContentElement<*>, D : Descriptor<*>> : Operator.Nullary<Retrieved> {
    /** The [Schema.Field] queried by this [Retriever]. */
    val field: Schema.Field<C, D>

    override val name: String
        get() = this.field.fieldName

    /** The [Analyser] instance this [Retriever] belongs to. */
    val analyser: Analyser<C, D>
        get() = this.field.analyser
}