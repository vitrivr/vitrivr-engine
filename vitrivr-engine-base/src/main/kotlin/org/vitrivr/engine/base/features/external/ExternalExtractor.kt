package org.vitrivr.engine.base.features.external

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithContent
import org.vitrivr.engine.core.model.retrievable.decorators.RetrievableWithDescriptor
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * Abstract class for implementing an external feature extractor.
 *
 * @param C Type of [ContentElement] that this external extractor operates on.
 * @param D Type of [Descriptor] produced by this external extractor.
 *
 * @see [Extractor]
 *
 * @author Rahel Arnold
 * @version 1.0.0
 */
abstract class ExternalExtractor<C : ContentElement<*>, D : Descriptor>(
    /** */
    override val input: Operator<Retrievable>,

    /** */
    override val field: Schema.Field<C, D>,

    /** */
    val context: IndexContext,

    /** */
    override val persisting: Boolean = true
) : Extractor<C, D> {
    /** The [DescriptorWriter] used by this [ExternalExtractor]. */
    private val writer: DescriptorWriter<D> by lazy { this.field.getWriter() }

    /**
     * Creates a [Descriptor] from the given [RetrievableWithContent].
     *
     * @param retrievable The [RetrievableWithContent] from which to create the descriptor.
     * @return The generated [Descriptor].
     */
    abstract fun createDescriptor(retrievable: RetrievableWithContent): D

    /**
     * Queries the external feature extraction API for the given [RetrievableWithContent].
     *
     * @param retrievable The [RetrievableWithContent] for which to query the external feature extraction API.
     * @return A list of external features.
     */
    abstract fun queryExternalFeatureAPI(retrievable: RetrievableWithContent): List<*>

    override fun toFlow(scope: CoroutineScope): Flow<Retrievable> = this.input.toFlow(scope).map { retrievable ->
        if (retrievable is RetrievableWithContent) {
            val descriptor = createDescriptor(retrievable)

            /* Append descriptor to retrievable */
            if (retrievable is RetrievableWithDescriptor.Mutable) {
                retrievable.addDescriptor(descriptor)
            }

            /* Persist descriptor. */
            if (this.persisting) {
                this.writer.add(descriptor)
            }
        }
        retrievable
    }
}
