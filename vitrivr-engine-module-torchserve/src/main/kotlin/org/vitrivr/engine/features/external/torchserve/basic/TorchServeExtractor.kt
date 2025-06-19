package org.vitrivr.engine.features.external.torchserve.basic

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [Extractor] implementation for the [TorchServe] analyser.
 *
 * @see [TorchServe]
 *
 * @author Ralph Gasser
 * @version 1.1.0
 */
open class TorchServeExtractor<C : ContentElement<*>, D : Descriptor<*>>(
    val host: String,
    val port: Int,
    val token: String?,
    val model: String,
    input: Operator<Retrievable>,
    analyser: TorchServe<C, D>,
    field: Schema.Field<C, D>? = null,
    name: String
) : AbstractExtractor<C, D>(input, analyser, field, name) {

    /**
     * Extracts [Descriptor]s from the provided [Retrievable].
     *
     * @param retrievable [Retrievable] to extract [Descriptor]s from.
     */
    @Suppress("UNCHECKED_CAST")
    override fun extract(retrievable: Retrievable): List<D> {
        val content = retrievable.content.filter { o -> this.analyser.contentClasses.any { c -> c.isInstance(o) } } as List<C>
        return (this.analyser as TorchServe<C, D>).analyse(content, this.model, this.host, this.port, token).map { it.copy(retrievableId = retrievable.id) as D }
    }
}