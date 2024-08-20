package org.vitrivr.engine.core.database.blackhole

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.github.oshai.kotlinlogging.Marker
import org.vitrivr.engine.core.database.AbstractConnection
import org.vitrivr.engine.core.database.ConnectionProvider
import org.vitrivr.engine.core.database.blackhole.descriptors.BlackholeDescriptorInitializer
import org.vitrivr.engine.core.database.blackhole.descriptors.BlackholeDescriptorReader
import org.vitrivr.engine.core.database.blackhole.descriptors.BlackholeDescriptorWriter
import org.vitrivr.engine.core.database.blackhole.retrievable.BlackholeRetrievableInitializer
import org.vitrivr.engine.core.database.blackhole.retrievable.BlackholeRetrievableReader
import org.vitrivr.engine.core.database.blackhole.retrievable.BlackholeRetrievableWriter
import org.vitrivr.engine.core.database.descriptor.DescriptorReader
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema


/** Defines [KLogger] of the class. */
internal val LOGGER: KLogger = logger {}

/**
 * An [AbstractConnection] that swallows all data and does not store anything. However, it can be used to log operations.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeConnection(schemaName: String, provider: ConnectionProvider, private val log: Boolean = false) : AbstractConnection(schemaName, provider) {



    init {
        LOGGER.warn { "You are using the blackhole connection with schema $schemaName. No data will be stored!" }
    }

    /** [Marker] used for logging. */
    private val marker: Marker = object : Marker {
        override fun getName(): String = this@BlackholeConnection.description()
    }

    override fun initialize() = this.logIf("Initializing schema '$schemaName'.")
    override fun truncate() = this.logIf("Truncating schema '$schemaName'.")
    override fun <T> withTransaction(action: (Unit) -> T): T {
        LOGGER.warn { "Transactions are not supported by blackhole connection. Ignoring transaction." }
        return action(Unit)
    }

    override fun getRetrievableInitializer(): RetrievableInitializer = BlackholeRetrievableInitializer(this)
    override fun getRetrievableWriter(): RetrievableWriter = BlackholeRetrievableWriter(this)
    override fun getRetrievableReader(): RetrievableReader = BlackholeRetrievableReader(this)
    override fun <D : Descriptor<*>> getDescriptorInitializer(field: Schema.Field<*, D>) = BlackholeDescriptorInitializer(this, field)
    override fun <D : Descriptor<*>> getDescriptorWriter(field: Schema.Field<*, D>) = BlackholeDescriptorWriter(this, field)
    override fun <D : Descriptor<*>> getDescriptorReader(field: Schema.Field<*, D>): DescriptorReader<D> = BlackholeDescriptorReader(this, field)
    override fun description(): String = "'$schemaName'@blackhole"
    override fun close() = this.logIf("Closing connection to blackhole database.")

    /**
     * Conditionally logs a message if logging is enabled.
     *
     * @param message The message to log.
     */
    internal fun logIf(message: String) {
        if (this.log) LOGGER.info(throwable = null, marker = this.marker) { message }
    }
}
