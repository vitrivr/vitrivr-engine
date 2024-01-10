package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.Persistable
import java.util.*

/**
 * The [Reader] is part of the data persistence layer of vitrivr and can be used to encapsulate DQL operations  for the underlying database.
 *
 * In the simplest version, these operations include a lookup by [UUID], a batched lookup, a list and a count operation.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface Reader<out T : Persistable> {
    /**
     * Returns a [Persistable] of type [T] that corresponds to the provided [UUID].
     *
     * @param id The [UUID] to return.
     * @return [Sequence] of [Persistable] of type [T]
     */
    operator fun get(id: UUID): T?

    /**
     * Returns a [Persistable] of type [T] that corresponds to the provided [UUID] in a specified columnName.
     */
    fun getBy(id: UUID, columnName: String): T?

    /**
     * Checks if a [Persistable] of type [T] with the provided [UUID] exists.
     *
     * @return True if [Persistable] exists, false otherwise.
     */
    fun exists(id: UUID): Boolean

    /**
     * Returns a [Sequence] of all [Persistable] accessible by this [Reader] whose [UUID] is contained in the provided [Iterable].
     *
     * @param ids A [Iterable] of [UUID]s to return.
     * @return [Sequence] of [Persistable] of type [T]
     */
    fun getAll(ids: Iterable<UUID>): Sequence<T>

    /**
     * Returns a [Sequence] of all [Persistable] accessible by this [Reader] whose [UUID] is contained in a specified columnName in the provided [Iterable].
     *
     * @param ids A [Iterable] of [UUID]s to return.
     * @return [Sequence] of [Persistable] of type [T]
     */
    fun getAllBy(ids: Iterable<UUID>, columnName: String): Sequence<T>

    /**
     * Returns a [Sequence] of all [Persistable] accessible by this [Reader].
     *
     * @return [Sequence] of [Persistable] of type [T]
     */
    fun getAll(): Sequence<T>

    /**
     * Counts the number of [Persistable] of this [Reader].
     *
     * @return The number of [Persistable] for this [Reader]
     */
    fun count(): Long
}