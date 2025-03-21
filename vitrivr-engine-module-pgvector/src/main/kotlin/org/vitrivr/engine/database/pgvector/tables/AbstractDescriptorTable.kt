package org.vitrivr.engine.database.pgvector.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.vitrivr.engine.core.config.schema.IndexType
import org.vitrivr.engine.core.database.Initializer.Companion.DISTANCE_PARAMETER_NAME
import org.vitrivr.engine.core.database.Initializer.Companion.INDEX_TYPE_PARAMETER_NAME
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ENTITY_PREFIX
import org.vitrivr.engine.database.pgvector.DESCRIPTOR_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.RETRIEVABLE_ID_COLUMN_NAME
import org.vitrivr.engine.database.pgvector.exposed.index.VectorIndex
import org.vitrivr.engine.database.pgvector.exposed.types.FloatVectorColumnType
import org.vitrivr.engine.database.pgvector.exposed.types.TsVectorColumnType

/**
 * An abstract [UUIDTable] for [Descriptor]s. This class is used as a base class for all descriptor tables.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
abstract class AbstractDescriptorTable<D : Descriptor<*>>(protected val field: Schema.Field<*, D>): UUIDTable("${DESCRIPTOR_ENTITY_PREFIX}_${field.fieldName.lowercase()}", DESCRIPTOR_ID_COLUMN_NAME) {
    /** Reference to the [RetrievableTable]. */
    val retrievableId = reference(RETRIEVABLE_ID_COLUMN_NAME, RetrievableTable, onDelete = ReferenceOption.CASCADE).index()

    /** List of [VectorIndex] structures. */
    private val vectorIndexes: MutableList<VectorIndex> = mutableListOf()

    /** The prototype value handled by this [StructDescriptorTable]. */
    protected val prototype by lazy { this.field.getPrototype() }

    /**
     * Registers a new [FloatVectorColumnType].
     *
     * @param name The name of the [FloatVectorColumnType]
     * @param dimension The dimensionality of the [FloatVectorColumnType]
     */
    protected fun floatVector(name: String, dimension: Int) = registerColumn(name, FloatVectorColumnType(dimension))

    /**
     * Registers a new [VectorIndex].
     *
     * @param column The column to create the [VectorIndex] for.
     * @param type The type of the [VectorIndex] to create.
     * @param distance The [Distance] function to create [VectorIndex] for.
     * @param customName Name of the index.
     * @param parameters Named paramters used for index creation.
     */
    protected fun vectorIndex(column: Column<*>, type: String, distance: Distance, customName: String? = null, parameters: Map<String, String> = emptyMap()) {
        require(this.columns.contains(column)) { "Column $column does not exist in table '${this.nameInDatabaseCase()}'." }
        this.vectorIndexes.add(VectorIndex(column, type, distance, customName, parameters))
    }

    /**
     * Initializes the indexes for this [AbstractDescriptorTable].
     *
     * This method is used to create the indexes for the table. It must be called by the implementing subclass
     * for reasons of inheritance.
     */
    protected fun initializeIndexes() {
        for (index in this.field.indexes) {
            val columns = index.attributes.map { a -> this.columns.find { c -> a == c.name } ?: throw IllegalStateException("Cannot create index; no column named '$a'.") }
            when (index.type) {
                IndexType.SCALAR -> {
                    val type = index.parameters[INDEX_TYPE_PARAMETER_NAME]?.lowercase() ?: "btree"
                    if (type == "btree") {
                        index(columns = columns.toTypedArray())
                    } else {
                        index(indexType = type, columns = columns.toTypedArray())
                    }
                }
                IndexType.FULLTEXT -> {
                    val column = columns.firstOrNull() ?: error("Cannot create fulltext index without column.")
                    val language =  index.parameters["language"]?.lowercase() ?: "english"
                    this.registerColumn("${column.nameInDatabaseCase()}_ft_index", TsVectorColumnType())
                        .databaseGenerated()
                        .withDefinition("GENERATED ALWAYS AS (to_tsvector('${language}', ${column.nameInDatabaseCase()})) STORED")
                }
                IndexType.NNS -> {
                    val type = index.parameters[INDEX_TYPE_PARAMETER_NAME]?.lowercase() ?: "hnsw"
                    val distance = index.parameters[DISTANCE_PARAMETER_NAME]?.let { Distance.valueOf(it.uppercase()) } ?: Distance.EUCLIDEAN
                    this.vectorIndex(columns.first(), type, distance, parameters = index.parameters.filter { it.key == INDEX_TYPE_PARAMETER_NAME || it.key == DISTANCE_PARAMETER_NAME })
                }
            }
        }
        this.vectorIndexes.forEach { it.createStatement() }
    }

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun insert(descriptor: D) = this.insert {
        it[this.id] = descriptor.id
        it[this.retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        it.setValue(descriptor)
    }

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun update(descriptor: D) = this.update({ this@AbstractDescriptorTable.id eq descriptor.id }) {
        it[this.retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        it.setValue(descriptor)
    }

    /**
     * Inserts a descriptor into the table. This method is used to insert a descriptor into the table.
     */
    fun batchInsert(entities: Iterable<D>) = this.batchInsert(entities) { descriptor ->
        this[id] = descriptor.id
        this[retrievableId] = descriptor.retrievableId ?: error("Cannot insert entity without retrievableId.")
        this.setValue(descriptor)
    }

    /**
     * Converts a [org.vitrivr.engine.core.model.query.Query] into a [Query] that can be executed against the database.
     *
     * @param query The [org.vitrivr.engine.core.model.query.Query] to convert.
     * @return The [Query] that can be executed against the database.
     * @throws UnsupportedOperationException If the query is not supported.
     */
    abstract fun parse(query: org.vitrivr.engine.core.model.query.Query): Query

    /**
     * Converts a [ResultRow] to a [Descriptor].
     *
     * @param row The [ResultRow] to convert.
     * @return The [Descriptor] represented by the [ResultRow].
     */
    abstract fun rowToDescriptor(row: ResultRow): D

    /**
     * Sets the value of the descriptor in the [InsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun InsertStatement<*>.setValue(d: D)

    /**
     * Sets the value of the descriptor in the [UpdateStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun UpdateStatement.setValue(d: D)

    /**
     * Sets the value of the descriptor in the [BatchInsertStatement]t.
     *
     * @param d The [Descriptor] to set value for
     */
    protected abstract fun BatchInsertStatement.setValue(d: D)

    /**
     * Creates the SQL statement to create the table.
     */
    override fun createStatement(): List<String> =
        super.createStatement() + this.vectorIndexes.flatMap { it.createStatement() }

    /**
     * Creates the SQL statement to create the table.
     */
    override fun dropStatement(): List<String> =
        super.dropStatement() + this.vectorIndexes.flatMap { it.dropStatement() }
}