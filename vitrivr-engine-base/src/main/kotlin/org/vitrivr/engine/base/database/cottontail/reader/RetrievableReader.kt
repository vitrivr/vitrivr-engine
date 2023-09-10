package org.vitrivr.engine.base.database.cottontail.reader

import org.vitrivr.cottontail.client.language.basics.expression.Column
import org.vitrivr.cottontail.client.language.basics.expression.List
import org.vitrivr.cottontail.client.language.basics.predicate.Compare
import org.vitrivr.cottontail.client.language.dql.Query
import org.vitrivr.cottontail.core.database.Name
import org.vitrivr.cottontail.core.values.StringValue
import org.vitrivr.engine.base.database.cottontail.CottontailConnection
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.base.database.cottontail.CottontailConnection.Companion.RETRIEVABLE_TYPE_COLUMN_NAME
import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import org.vitrivr.engine.core.model.database.retrievable.RetrievableId
import org.vitrivr.engine.core.model.database.retrievable.Retrieved
import java.util.*

/**
 * A [RetrievableReader] implementation for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
internal class RetrievableReader(private val connection: CottontailConnection): RetrievableReader {
    /** The [Name.EntityName] for this [RetrievableInitializer]. */
    private val entityName: Name.EntityName = Name.EntityName(this.connection.schemaName, CottontailConnection.RETRIEVABLE_ENTITY_NAME)

    /**
     * Returns the [Retrievable]s that matches the provided [RetrievableId]
     *
     * @param id [RetrievableId]s to return.
     * @return A [Sequence] of all [Retrievable].
     */
    override fun get(id: UUID): Retrievable? = this.getAll(listOf(id)).firstOrNull()

    /**
     * Returns all [Retrievable]s  that match any of the provided [RetrievableId]
     *
     * @param ids A [Iterable] of [RetrievableId]s to return.
     * @return A [Sequence] of all [Retrievable].
     */
    override fun getAll(ids: Iterable<RetrievableId>): Sequence<Retrievable> {
        val query = Query(this.entityName).select("*").where(Compare(Column(Name.ColumnName(RETRIEVABLE_ID_COLUMN_NAME)), Compare.Operator.IN, List(ids.map { StringValue(it.toString()) }.toTypedArray())))
        return this.connection.client.query(query).asSequence().map { tuple ->
            val retrievableId = UUID.fromString(tuple.asString(RETRIEVABLE_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."))
            val type = tuple.asString(RETRIEVABLE_TYPE_COLUMN_NAME)
            Retrieved.Default(retrievableId, type, false) /* TODO: Use UUID type once supported. */
        }
    }

    /**
     * Returns all [Retrievable]s stored by the database.
     *
     * @return A [Sequence] of all [Retrievable]s in the database.
     */
    override fun getAll(): Sequence<Retrievable> {
        val query = Query(this.entityName).select("*")
        return this.connection.client.query(query).asSequence().map { tuple ->
            val retrievableId = UUID.fromString(tuple.asString(RETRIEVABLE_ID_COLUMN_NAME) ?: throw IllegalArgumentException("The provided tuple is missing the required field '${RETRIEVABLE_ID_COLUMN_NAME}'."))
            val type = tuple.asString(RETRIEVABLE_TYPE_COLUMN_NAME)
            Retrieved.Default(retrievableId, type, false) /* TODO: Use UUID type once supported. */
        }
    }

    /**
     * Counts the number of retrievables stored by the database.
     *
     * @return The number of [Retrievable]s.
     */
    override fun count(): Long {
        var count = 0L
        this.connection.client.query(Query(this.entityName).count()).forEach {
            count = it.asLong(0)!!
        }
        return count
    }
}