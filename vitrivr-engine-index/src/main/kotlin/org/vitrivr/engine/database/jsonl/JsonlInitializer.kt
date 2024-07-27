package org.vitrivr.engine.database.jsonl

import org.vitrivr.engine.core.database.descriptor.DescriptorInitializer
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import java.io.IOException

class JsonlInitializer<D : Descriptor>(override val field: Schema.Field<*, D>, private val connection: JsonlConnection) : DescriptorInitializer<D> {

    private val file = connection.getFile(field)

    override fun initialize() {
        try{
            file.parentFile.mkdirs()
            file.createNewFile()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot initialize '${file.absolutePath}'" }
        } catch (se: SecurityException) {
            LOGGER.error(se) { "Cannot initialize '${file.absolutePath}'" }
        }

    }

    override fun deinitialize() {
        /* nop */
    }

    override fun isInitialized(): Boolean = file.exists()

    override fun truncate() {
        try{
            file.delete()
        } catch (ioe: IOException) {
            LOGGER.error(ioe) { "Cannot delete '${file.absolutePath}'" }
        } catch (se: SecurityException) {
            LOGGER.error(se) { "Cannot delete '${file.absolutePath}'" }
        }
    }
}