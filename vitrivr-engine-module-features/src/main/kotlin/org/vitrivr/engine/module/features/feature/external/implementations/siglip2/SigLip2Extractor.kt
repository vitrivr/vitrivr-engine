package org.vitrivr.engine.module.features.feature.external.implementations.siglip2

import org.vitrivr.engine.core.features.AbstractExtractor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor

/**
 * [SigLip2Extractor] implementation of an [AbstractExtractor] for [SigLip2].
 *
 * @author Rahel Arnold
 * @version 1.3.0
 */
class SigLip2Extractor : AbstractExtractor<ContentElement<*>, FloatVectorDescriptor> {

    private val host: String

    /**
     * Creates an extractor for a schema field.
     *
     * @param input Operator representing the input data source.
     * @param analyser SigLip2 analyser used by this extractor.
     * @param field Schema field for which the extractor generates descriptors.
     * @param host Hostname of the external feature descriptor service.
     */
    constructor(input: Operator<out Retrievable>, analyser: SigLip2, field: Schema.Field<ContentElement<*>, FloatVectorDescriptor>, host: String) : super(input, analyser, field) {
        this.host = host
    }

    /**
     * Creates an extractor for a named output field.
     *
     * @param input Operator representing the input data source.
     * @param analyser SigLip2 analyser used by this extractor.
     * @param name Name of the output field for generated descriptors.
     * @param host Hostname of the external feature descriptor service.
     */
    constructor(input: Operator<out Retrievable>, analyser: SigLip2, name: String, host: String) : super(input, analyser, name) {
        this.host = host
    }


    /**
     * Internal method to check, if [Retrievable] matches this [Extractor] and should thus be processed.
     *
     * @param retrievable The [Retrievable] to check.
     * @return True on match, false otherwise,
     */
    override fun matches(retrievable: Retrievable): Boolean {
        val match = retrievable.content.any { it is ImageContent }
        logger.debug {
            "SigLip2 matches ${retrievable.id}: $match; content=" +
                    retrievable.content.joinToString { it::class.simpleName ?: "unknown" }
        }
        return match
    }
    /**
     * Internal method to perform extraction on [Retrievable].
     **
     * @param retrievable The [Retrievable] to process.
     * @return List of resulting [Descriptor]s.
     */
    override fun extract(retrievable: Retrievable) = retrievable.content.filterIsInstance<ImageContent>().map { c ->
        SigLip2.analyse(c, this.host).copy(retrievableId = retrievable.id, field = this@SigLip2Extractor.field)
    }
}
