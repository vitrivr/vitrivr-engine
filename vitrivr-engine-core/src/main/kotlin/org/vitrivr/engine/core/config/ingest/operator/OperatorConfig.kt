package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig.Decoder
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig.Enumerator
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig.Exporter
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig.Extractor
import org.vitrivr.engine.core.config.ingest.operator.OperatorConfig.Transformer
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Analyser
import org.vitrivr.engine.core.operators.general.ExporterFactory
import org.vitrivr.engine.core.operators.general.TransformerFactory
import org.vitrivr.engine.core.operators.ingest.DecoderFactory
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.source.MediaType
import org.vitrivr.engine.core.util.extension.loadServiceCandidates
import java.util.ServiceLoader
import kotlin.collections.find

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable(with = ModuleSerializer::class)
sealed class OperatorConfig {
    /**
     * The class name of the factory for the corresponding operator.
     * See [org.vitrivr.engine.core.operators.ingest] for the available factories.
     */
    abstract val factory: String
    abstract val parameters: Map<String, String>


    /**
     * Configuration for a [Decoder].
     */
    @Serializable
    data class Decoder(override val factory: String, override val parameters: Map<String, String> = emptyMap()) :
        OperatorConfig()

    /**
     * Configuration for a [Enumerator]
     */
    @Serializable
    data class Enumerator(override val factory: String, override val parameters: Map<String, String> = emptyMap()) :
        OperatorConfig() {
        val mediaTypes: List<MediaType> = emptyList()
    }


    /**
     * Configuration for a [Transformer].
     */
    @Serializable
    data class Transformer(override val factory: String, override val parameters: Map<String, String> = emptyMap()) :
        OperatorConfig()

    /**
     * Configuration for an [Extractor].
     */
    @Serializable
    data class Extractor(
        val fieldName: String? = null,
        override val factory: String = "",
        override val parameters: Map<String, String> = emptyMap()
    ) : OperatorConfig() {
        init {
            require(!this.fieldName.isNullOrBlank() || !this.factory.isNullOrBlank()) {
                "An ExporterConfig must have either an exporter name (defined in the schema) or a factory name"
            }
        }
    }

    /**
     * Configuration for an [Exporter].
     */
    @Serializable
    data class Exporter(
        val exporterName: String? = null,
        override val factory: String = "",
        override val parameters: Map<String, String> = emptyMap()
    ) : OperatorConfig() {
        init {
            require(!this.exporterName.isNullOrBlank() || !this.exporterName.isNullOrBlank()) {
                "An ExporterConfig must have either an exporter name (defined in the schema) or a factory name"
            }
        }
    }
}

object ModuleSerializer : JsonContentPolymorphicSerializer<OperatorConfig>(OperatorConfig::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out OperatorConfig> {

        val analyzerCandidates = loadServiceCandidates<Analyser<ContentElement<*>, Descriptor<*>>>()
        val exporterCandidates = loadServiceCandidates<ExporterFactory>()
        val transformerCandidates = loadServiceCandidates<TransformerFactory>()
        val decoderCandidates = loadServiceCandidates<DecoderFactory>()
        val enumeratorCandidates = loadServiceCandidates<EnumeratorFactory>()

        val factory = element.jsonObject["factory"]?.jsonPrimitive?.content
        val field = element.jsonObject["fieldName"]?.jsonPrimitive?.content
        val exporter = element.jsonObject["exporterName"]?.jsonPrimitive?.content


        return when {
            enumeratorCandidates.find { it::class.java.simpleName == factory } != null -> Enumerator.serializer()
            decoderCandidates.find { it::class.java.simpleName == factory } != null -> Decoder.serializer()
            transformerCandidates.find { it::class.java.simpleName == factory } != null -> Transformer.serializer()
            field == null && factory == null && exporter != null -> Exporter.serializer()
            field != null && factory == null && exporter == null -> Extractor.serializer()
            else -> throw Exception("Unknown Module: key 'type' not found or does not matches any module type")
        }
    }
}
