package org.vitrivr.engine.core.database

import org.vitrivr.engine.core.model.Persistable

import java.util.*

/**
 * The [Writer] is part of the data persistence layer of vitrivr and can be used to encapsulate DML operations for the underlying database.
 *
 * In the simplest version, these operations include a lookup add, update and delete operation.
 *
 * @author Ralph Gasser
 * @author Luca Rossetto
 * @version 1.0.0
 */
interface Writer<in T : Persistable> {

    /**
     * Adds (and typically persists) a single [Persistable] of type [T] through this [Writer].
     *
     * @param item [Persistable] of type [T] to persist.
     * @return True on success, false otherwise.
     */
    fun add(item: T): Boolean

    /**
     * Adds (and typically persists) a batch of [Persistable] of type [T] through this [Writer].
     *
     * The following semantic is expected:
     * - Calls to this method are expected to be atomic. If it fails, now item should be persisted. Otherwise, all items should be persisted.
     * - Ideally, and if supported by the database, batched adding should be faster than item-by-item adding.
     *
     * @param items [Iterable] of [Persistable] of type [T] to persist.
     * @return True on success, false otherwise.
     */
    fun addAll(items: Iterable<T>): Boolean

    /**
     * Updates (and typically persists) an already existing [Persistable] of type [T] through this [Writer].
     *
     * The following semantic is expected:
     * - If [Persistable] with given [UUID] exists, the update is executed and true is returned.
     * - If [Persistable] with given [UUID] does not exist, the update fails.
     * - Any violated constraints lead to a failure of the update operation.
     *
     * @param item [Persistable] of type [T] to update.
     * @return True on success, false otherwise.
     */
    fun update(item: T): Boolean

    /**
     * Deletes (and typically persists) an already existing [Persistable] of type [T] through this [Writer].
     *
     * The following semantic is expected:
     * - If [Persistable] with given [UUID] exists, delete is executed and true is returned.
     * - If [Persistable] with given [UUID] does not exist, delete fails and false is returned
     * - Any violated constraints lead to a failure of the update operation.
     *
     * @param item [Persistable] of type [T] to delete.
     * @return True on success, false otherwise.
     */
    fun delete(item: T): Boolean

    /**
     * Deletes (and typically persists) a list of pre-existing [Persistable] of type [T] through this [Writer].
     *
     * The following semantic is expected:
     * - If [Persistable] with given [UUID] exists, delete is executed and true is returned.
     * - If [Persistable] with given [UUID] does not exist, delete fails and false is returned
     * - Any violated constraints lead to a failure of the update operation.
     *
     * @param items  [Iterable] of [Persistable] of type [T] to delete.
     * @return True on success, false otherwise.
     */
    fun deleteAll(items: Iterable<T>): Boolean
}