package org.vitrivr.engine.core.database.blackhole.descriptors

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema

/**
 * A [DescriptorWriter] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeDescriptorWriter<T: Descriptor<*>>(override val connection: BlackholeConnection, override val field: Schema.Field<*, T>): DescriptorWriter<T> {
    override fun add(item: T): Boolean {
        this.connection.logIf("Adding descriptor '${item.id}' to entity '${this.field.fieldName}'.")
        return false
    }

    override fun addAll(items: Iterable<T>): Boolean {
        items.forEach { item -> this.connection.logIf("Adding descriptor '${item.id}' to entity '${this.field.fieldName}'.") }
        return false
    }

    override fun update(item: T): Boolean {
        this.connection.logIf("Updating descriptor '${item.id}' in entity '${this.field.fieldName}'.")
        return false
    }

    override fun delete(item: T): Boolean {
        this.connection.logIf("Deleting descriptor '${item.id}' in entity '${this.field.fieldName}'.")
        return false
    }

    override fun deleteAll(items: Iterable<T>): Boolean {
        items.forEach { item -> this.connection.logIf("Deleting descriptor '${item.id}' from entity '${this.field.fieldName}'.") }
        return false
    }
}