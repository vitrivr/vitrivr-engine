package org.vitrivr.engine.index.pipeline

import org.vitrivr.engine.core.model.metamodel.Schema
import kotlin.reflect.KClass

/**
 *
 * @author Ralph Gasser
 * @version 1.0
 */
class PipelineBuilder(private val schema: Schema) {

    companion object {
        /**
         * Creates a new [PipelineBuilder] using the provided [Schema].
         *
         * @param schema The [Schema] to create a [PipelineBuilder] for.
         */
        fun forSchema(schema: Schema) = PipelineBuilder(schema)
    }

    /** The [Enumerator] instance this [PipelineBuilder] is pointing to. */
    private var child: Enumerator? = null

    /**
     * Appends an [Enumerator] to the [PipelineBuilder].
     *
     * @param enumerator The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
     * @param parameters A [Map] of parameters.
     * @return [PipelineBuilder.Enumerator]
     */
    fun withEnumerator(enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Enumerator>, parameters: Map<String, Any>): PipelineBuilder.Enumerator {
        this.child = Enumerator(enumerator, parameters)
        return this.child!!
    }

    /**
     * An [Enumerator] stage in the pipeline described by the surrounding [PipelineBuilder].
     */
    inner class Enumerator internal constructor(private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Enumerator>, private val parameters: Map<String, Any>) {

        /** The (child) [Decoder] instance this [PipelineBuilder.Enumerator] is pointing to. */
        private var child: Decoder? = null

        /**
         * Appends an [Decoder] to the [Enumerator].
         *
         * @param decoder The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
         * @param parameters A [Map] of parameters.
         * @return [PipelineBuilder.Enumerator]
         */
        fun withDecoder(decoder: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>, parameters: Map<String, Any>): PipelineBuilder.Decoder {
            this.child = Decoder(decoder, parameters)
            return this.child!!
        }

    }

    /**
     * An [Decoder] stage in the pipeline described by the surrounding [PipelineBuilder].
     */
    inner class Decoder internal constructor(private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>, private val parameters: Map<String, Any>) {

        /** The (child) [Decoder] instance this [PipelineBuilder.Enumerator] is pointing to. */
        private var child: Transformer? = null

        /**
         * Appends an [Decoder] to the [Enumerator].
         *
         * @param decoder The [KClass] specifying the [org.vitrivr.engine.core.operators.ingest.Enumerator].
         * @param parameters A [Map] of parameters.
         * @return [PipelineBuilder.Enumerator]
         */
        fun withTransformer(decoder: KClass<org.vitrivr.engine.core.operators.ingest.Decoder>, parameters: Map<String, Any>) = Decoder(decoder, parameters)
    }

    /**
     *
     */
    inner class Transformer internal constructor(private val enumerator: KClass<org.vitrivr.engine.core.operators.ingest.Transformer>, private val parameters: Map<String, Any>) {

    }
}