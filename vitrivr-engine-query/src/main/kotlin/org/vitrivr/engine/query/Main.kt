package org.vitrivr.engine.query

import org.vitrivr.engine.core.config.VitrivrConfig
import org.vitrivr.engine.core.model.metamodel.SchemaManager
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Entry point for vitrivr engine query.
 */
fun main(args: Array<String>) {
    /* Load system configuration. */
    val config = VitrivrConfig.read(Paths.get(args.getOrElse(0) { VitrivrConfig.DEFAULT_SCHEMA_PATH })) ?: exitProcess(1)

    /* Open all schemas. */
    for (schema in config.schemas) {
        SchemaManager.open(schema)
    }
}
