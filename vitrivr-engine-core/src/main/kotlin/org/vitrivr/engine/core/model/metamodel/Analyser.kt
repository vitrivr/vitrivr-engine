package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.model.database.descriptor.Descriptor
import org.vitrivr.engine.core.model.database.retrievable.IngestedRetrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Extractor
import org.vitrivr.engine.core.operators.retrieve.Retriever
import kotlin.reflect.KClass

/**
 * An [Analyser] is a formal specification of a type an analysis performed for a [Field] to derive a [Descriptor].
 *
 * - During indexing, the analysis step involves analysing the media content to derive a [Descriptor]
 * - During retrieval, the analysis step involves the execution of a query using the derived [Descriptor]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Analyser<T: Descriptor> {
    /**
     * The type name of this [Analyser] instance. Is connected to the type of description derived by this [Analyser] (implementation).
     *
     * By default, the implementing class's fully-qualified name is used as [analyserName].
     */
    val analyserName: String

    /** The [KClass] of the [Descriptor] used by this [Analyser].  */
    val descriptorClass: KClass<T>

    /**
     * Generates a specimen of the [Descriptor] produced / consumed by this [Analyser].
     *
     * This is a required operation.
     *
     * @param field The [Schema.Field] to create a [Descriptor] for.
     * @return A [Descriptor] specimen of type [T].
     */
    fun newDescriptor(field: Schema.Field<T>): T

    /**
     * Generates and returns a new [Extractor] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Extractor], in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @param input The [Operator] that acts as input to the new [Extractor]
     * @param persisting True, if the results of the [Extractor] should be persisted.
     *
     * @return A new [Extractor] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Extractor] instance.
     */
    fun newExtractor(field: Schema.Field<T>, input: Operator<IngestedRetrievable>, persisting: Boolean = true): Extractor<T>

    /**
     * Generates and returns a new [Retriever] instance for this [Analyser].
     *
     * Some [Analyser]s may not come with their own [Retriever], in which case the implementation of this method should throw an [UnsupportedOperationException]
     *
     * @param field The [Schema.Field] to create an [Extractor] for.
     * @return A new [Retriever] instance for this [Analyser]
     * @throws [UnsupportedOperationException], if this [Analyser] does not support the creation of an [Retriever] instance.
     */
    fun newRetriever(field: Schema.Field<T>): Retriever<T>
}