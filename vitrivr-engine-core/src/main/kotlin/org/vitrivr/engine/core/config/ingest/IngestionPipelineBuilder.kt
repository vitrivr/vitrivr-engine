package org.vitrivr.engine.core.config.ingest

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.vitrivr.engine.core.config.ingest.operator.DecoderConfig
import org.vitrivr.engine.core.config.operators.EnumeratorConfig
import org.vitrivr.engine.core.context.IndexContext
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.operators.ingest.Decoder
import org.vitrivr.engine.core.operators.ingest.Enumerator
import org.vitrivr.engine.core.operators.ingest.EnumeratorFactory
import org.vitrivr.engine.core.util.extension.loadServiceForName


/**
 * Parses the [IngestionConfig] to build an ingestion pipeline.
 */
class IngestionPipelineBuilder(val schema: Schema, val config: IngestionConfig) {

    private val logger: KLogger = KotlinLogging.logger { }

    private lateinit var enumerator: Enumerator
    private lateinit var decoder: Decoder
    private val operators = mutableListOf<Operator<*>>()

    /**
     * Parses the given [EnumeratorConfig] and builds the corresponding instance.
     */
    private fun parseEnumerator(config: EnumeratorConfig, context: IndexContext){
        val factory = loadServiceForName<EnumeratorFactory>(config.name)
            ?: throw IllegalArgumentException("Failed to find 'EnumeratorFactory' implementation for '${config.name}'.")
        enumerator = factory.newOperator(context, config.parameters)
        logger.info{"Instantiated new Enumerator: ${enumerator.javaClass.name}, parameters: ${config.parameters}"}
    }

    
}
