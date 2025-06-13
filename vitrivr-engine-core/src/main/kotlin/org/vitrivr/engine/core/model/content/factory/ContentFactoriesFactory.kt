package org.vitrivr.engine.core.model.content.factory

import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A factory class for [ContentFactory]s.
 *
 * Required for vitrivr-engine's [java.util.ServiceLoader] architecture.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface ContentFactoriesFactory {
    /**
     * Generates a new [ContentFactory] instance using the provided parameters.
     *
     * @param schema The [Schema] on which the [ContentFactory] operates
     * @param parameters The configuration parameters.
     * @return [ContentFactory]
     */
    fun newContentFactory(schema: Schema, parameters: Map<String, String>): ContentFactory
}
