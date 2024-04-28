package org.vitrivr.engine.core.config.ingest.operator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.vitrivr.engine.core.operators.ingest.*
import org.vitrivr.engine.core.source.MediaType

/**
 * Configuration for ingestion operators, as defined in the [org.vitrivr.engine.core.operators.ingest] package.
 * This is the definition of the operators, whereas the [OperationsConfig] defines the actual pipeline
 */
@Serializable
sealed interface OperatorConfig {
    sealed interface WithFactory : OperatorConfig {
        /**
         * The class name of the factory for the corresponding operator.
         * See [org.vitrivr.engine.core.operators.ingest] for the available factories.
         */
        val factory: String
    }

    /**
     * Configuration for a [Decoder].
     */
    @Serializable
    @SerialName("DECODER")
    data class Decoder(override val factory: String) : WithFactory

    /**
     * Configuration for a [Enumerator]
     */
    @Serializable
    @SerialName("ENUMERATOR")
    data class Enumerator(override val factory: String) : WithFactory {
        val mediaTypes: List<MediaType> = emptyList()
    }


    /**
     * Configuration for a [Transformer].
     */
    @Serializable
    @SerialName("TRANSFORMER")
    data class Transformer(override val factory: String) : WithFactory

    /**
     * Configuration for an [Extractor].
     */
    @Serializable
    @SerialName("EXTRACTOR")
    data class Extractor(val fieldName: String? = null, val factory: String? = null) : OperatorConfig {
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
    data class Exporter(val exporterName: String? = null, val factory: String? = null) : OperatorConfig {
        init {
            require(!this.exporterName.isNullOrBlank() || !this.exporterName.isNullOrBlank()) {
                "An ExporterConfig must have either an exporter name (defined in the schema) or a factory name"
            }
        }
    }
}