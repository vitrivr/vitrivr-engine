package org.vitrivr.engine.core.database.blackhole.retrievable

import org.vitrivr.engine.core.database.blackhole.BlackholeConnection
import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.relationship.Relationship
import org.vitrivr.engine.core.model.retrievable.Retrievable


/**
 * A [RetrievableWriter] for the [BlackholeConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BlackholeRetrievableWriter(override val connection: BlackholeConnection): RetrievableWriter {
    override fun connect(relationship: Relationship): Boolean {
        this.connection.logIf("Adding relationship ${relationship.subjectId} >[${relationship.predicate}]  ${relationship.objectId}.")
        return false
    }

    override fun connectAll(relationships: Iterable<Relationship>): Boolean {
        relationships.forEach { relationship ->  this.connection.logIf("Adding relationship ${relationship.subjectId} >[${relationship.predicate}]  ${relationship.objectId}.")}
        return false
    }

    override fun disconnect(relationship: Relationship): Boolean {
        this.connection.logIf("Removing relationship ${relationship.subjectId} >[${relationship.predicate}]  ${relationship.objectId}.")
        return false
    }

    override fun disconnectAll(relationships: Iterable<Relationship>): Boolean {
        relationships.forEach { relationship ->  this.connection.logIf("Removing relationship ${relationship.subjectId} >[${relationship.predicate}]  ${relationship.objectId}.")}
        return false
    }

    override fun add(item: Retrievable): Boolean {
        this.connection.logIf("Adding retrievable '${item.id}' to entity 'retrievable'.")
        return false
    }

    override fun addAll(items: Iterable<Retrievable>): Boolean {
        items.forEach { item -> this.connection.logIf("Adding retrievable '${item.id}' to entity 'retrievable'.") }
        return false
    }

    override fun update(item: Retrievable): Boolean {
        this.connection.logIf("Updating retrievable '${item.id}' in entity 'retrievable'.")
        return false
    }

    override fun delete(item: Retrievable): Boolean {
        this.connection.logIf("Deleting retrievable '${item.id}' from entity 'retrievable'.")
        return false
    }

    override fun deleteAll(items: Iterable<Retrievable>): Boolean {
        items.forEach { item -> this.connection.logIf("Deleting retrievable '${item.id}' from entity 'retrievable'.") }
        return false
    }
}