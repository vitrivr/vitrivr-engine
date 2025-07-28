package org.vitrivr.engine.features.external.torchserve.basic

import com.google.protobuf.ByteString
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.features.averagecolor.AverageColor
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.element.ImageContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.module.torchserve.client.InferenceClient

private val logger: KLogger = KotlinLogging.logger {}

/**
 * An abstract implementation of the [TorchServe] [Analyser], which leverages TorchServe ML models for inference.
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
abstract class TorchServe<C : ContentElement<*>, D : Descriptor<*>> : Analyser<C, D> {

    companion object {
        /** Key for the TorchServe 'host' parameter. */
        const val TORCHSERVE_HOST_KEY = "host"

        /** Default for the TorchServe 'host' parameter. */
        const val TORCHSERVE_HOST_DEFAULT = "127.0.0.1"

        /** Key for the TorchServe 'port' parameter. */
        const val TORCHSERVE_PORT_KEY = "port"

        /** Default value for the TorchServe 'port' parameter. */
        const val TORCHSERVE_PORT_DEFAULT = 7070

        /** Key for the TorchServe 'token' parameter. */
        const val TORCHSERVE_TOKEN_KEY = "token"

        /** Key for the TorchServe 'model' parameter. */
        const val TORCHSERVE_MODEL_KEY = "model"

    }

    /** A cached version of the [InferenceClient]. */
    @Volatile
    private var cachedClient: InferenceClient? = null

    /**
     * Performs the [AverageColor] analysis on the provided [List] of [ImageContent] elements.
     *
     * @param content The [List] of [ImageContent] elements.
     * @return [List] of [FloatVectorDescriptor]s.
     */
    fun analyse(content: Collection<C>, model: String, host: String, port: Int = 8080, token: String? = null): List<D> {
        /* Obtain a client. */
        val localClient = synchronized(this) {
            var localClient = this.cachedClient
            if (localClient == null || localClient.host != host || localClient.port != port) {
                localClient = InferenceClient(host, port, token)
                this.cachedClient?.close()
                this.cachedClient = localClient
            }
            localClient
        }

        val descriptors = mutableListOf<D>()
        for (c in content) {
            /* Perform the prediction. */
            val result = try {
                localClient.predict(model, this.toByteString(c))
            } catch (e: Throwable) {
                logger.warn(e) { "Failed to invoke torchserve model '$model' due to error." }
                continue
            }

            /* Convert output and map to list. */
            descriptors.addAll(this.byteStringToDescriptor(result))
        }

        return descriptors
    }

    /**
     * Generates and returns a new [TorchServeExtractor] instance for this [TorchServe].
     *
     * @param field The [Schema.Field] to create an [TorchServeExtractor] for.
     * @param input The [Operator] that acts as input to the new [TorchServeExtractor].
     * @param context The [IndexContext] to use with the [TorchServeExtractor].
     *
     * @return A new [TorchServeExtractor] instance for this [Analyser]
     */
    override fun newExtractor(field: Schema.Field<C, D>, input: Operator<Retrievable>, context: IndexContext): TorchServeExtractor<C, D> {
        val host = context.local[field.fieldName]?.get(TORCHSERVE_HOST_KEY) ?: field.parameters[TORCHSERVE_HOST_KEY] ?: TORCHSERVE_HOST_DEFAULT
        val port = ((context.local[field.fieldName]?.get(TORCHSERVE_PORT_KEY) ?: field.parameters[TORCHSERVE_PORT_KEY]))?.toIntOrNull() ?: TORCHSERVE_PORT_DEFAULT
        val token = context.local[field.fieldName]?.get(TORCHSERVE_TOKEN_KEY) ?: field.parameters[TORCHSERVE_TOKEN_KEY]
        val model = context.local[field.fieldName]?.get(TORCHSERVE_MODEL_KEY) ?: field.parameters[TORCHSERVE_MODEL_KEY] ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        return TorchServeExtractor(host, port, token, model, input, this, field, field.fieldName)
    }

    /**
     * Generates and returns a new [TorchServeExtractor] instance for this [TorchServe].
     *
     * @param name The name of the [TorchServeExtractor].
     * @param input The [Operator] that acts as input to the new [TorchServeExtractor].
     * @param context The [IndexContext] to use with the [TorchServeExtractor].
     *
     * @return A new [TorchServeExtractor] instance for this [TorchServe]
     */
    override fun newExtractor(name: String, input: Operator<Retrievable>, context: IndexContext): TorchServeExtractor<C, D> {
        val host = context.local[name]?.get(TORCHSERVE_HOST_KEY) ?: TORCHSERVE_HOST_DEFAULT
        val port = context.local[name]?.get(TORCHSERVE_PORT_KEY)?.toIntOrNull() ?: TORCHSERVE_PORT_DEFAULT
        val token = context.local[name]?.get(TORCHSERVE_TOKEN_KEY)
        val model = context.local[name]?.get(TORCHSERVE_MODEL_KEY) ?: throw IllegalArgumentException("Missing model for TorchServe model.")
        return TorchServeExtractor(host, port, token, model, input, this, null, name)
    }

    /**
     * Generates and returns a Map of [String] to [ByteString] intended as an input for a [TorchServe] model.
     *
     * The form of the input data is model dependent.
     *
     * @param content The [ContentElement] [C] to convert to [ByteString].
     * @return  [Map] of [String] to [ByteString].
     */
    protected abstract fun toByteString(content: C): Map<String, ByteString>

    /**
     * Generates and returns a [List] of [Descriptor]s [D] from a [ByteString] returned by a [TorchServe] model.
     *
     * The format of the [ByteString] is model dependent.
     *
     * @param byteString [ByteString] to convert
     * @return [List] of [Descriptor]s [D].
     */
    protected abstract fun byteStringToDescriptor(byteString: ByteString): List<D>
}