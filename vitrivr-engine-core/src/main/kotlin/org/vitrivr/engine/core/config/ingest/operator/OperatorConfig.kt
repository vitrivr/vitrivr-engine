package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.source.MediaType

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 *
 * @author Loris Sauter
 * @version 1.0.0
 */
@Serializable
sealed interface OperatorConfig {
    sealed interface WithFactory : OperatorConfig {
        /**
         * The class name of the factory for the corresponding operator.
         * See [org.vitrivr.engine.core.operators.ingest] for the available factories.
         */
        val factory: String
        val parameters: Map<String, String>
    }

    /**
     * Configuration for a [Decoder].
     */
    @Serializable
    @SerialName("DECODER")
    data class Decoder(override val factory: String, override val parameters: Map<String, String> = emptyMap()) : WithFactory

    /**
     * Configuration for a [Enumerator]
     */
    @Serializable
    @SerialName("ENUMERATOR")
    data class Enumerator(override val factory: String, override val parameters: Map<String, String> = emptyMap()) : WithFactory {
        val mediaTypes: List<MediaType> = emptyList()
    }


    /**
     * Configuration for a [Transformer].
     */
    @Serializable
    @SerialName("TRANSFORMER")
    data class Transformer(override val factory: String, override val parameters: Map<String, String> = emptyMap()) : WithFactory

    /**
     * Configuration for an [Extractor].
     */
    @Serializable
    @SerialName("EXTRACTOR")
    data class Extractor(val fieldName: String? = null, val factory: String? = null, val parameters: Map<String, String> = emptyMap()) : OperatorConfig {
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
    @SerialName("EXPORTER")
    data class Exporter(val exporterName: String? = null, val factory: String? = null, val parameters: Map<String, String> = emptyMap()) : OperatorConfig {
        init {
            require(!this.exporterName.isNullOrBlank() || !this.exporterName.isNullOrBlank()) {
                "An ExporterConfig must have either an exporter name (defined in the schema) or a factory name"
            }
        }
    }
}