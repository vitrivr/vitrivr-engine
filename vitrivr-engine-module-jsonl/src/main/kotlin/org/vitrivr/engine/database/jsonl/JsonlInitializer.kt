package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import java.io.IOException
import kotlin.io.path.*

class JsonlInitializer<D : Descriptor>(
    override val field: Schema.Field<*, D>,
    connection: JsonlConnection
) : DescriptorInitializer<D> {

    private val path = connection.getPath(field)

    override fun initialize() {
        try {
            path.parent.createDirectories()
            path.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot initialize '${path.absolutePathString()}'" }
        } catch (se: SecurityException) {
            LOGGER.error(se) { "Cannot initialize '${path.absolutePathString()}'" }
        }

    }

    override fun deinitialize() {
        try {
            path.deleteExisting()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot truncate '${path.absolutePathString()}'" }
        }
    }

    override fun isInitialized(): Boolean = path.exists()

    override fun truncate() {
        try {
            path.deleteExisting()
            path.createFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot truncate '${path.absolutePathString()}'" }
        }
    }
}