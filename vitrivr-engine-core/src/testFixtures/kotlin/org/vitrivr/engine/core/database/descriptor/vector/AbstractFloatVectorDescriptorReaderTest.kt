package org.vitrivr.engine.core.database.descriptor.vector

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.vitrivr.engine.core.database.AbstractDatabaseTest
import org.vitrivr.engine.core.database.descriptor.DescriptorWriter
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.metamodel.Schema
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * A series of test cases the test the functionality of the [VectorDescriptorReader].
 *
 * @author Ralph Gasser
 * @version 1.0.1
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractFloatVectorDescriptorReaderTest(schemaPath: String) : AbstractDatabaseTest(schemaPath) {

    /** The [Schema.Field] used for this [AbstractFloatVectorDescriptorReaderTest]. */
    protected val field: Schema.Field<*, FloatVectorDescriptor>
        = this.testSchema["averagecolor"]!! as Schema.Field<*, FloatVectorDescriptor>

    /**
     * Tests [VectorDescriptorReader.getAll] method.
     */
    @Test
    fun testReadAll() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)
        reader.getAll().forEach { descriptor ->
            Assertions.assertTrue(descriptors.find { it.id == descriptor.id } != null)
            Assertions.assertTrue(descriptors.find { it.retrievableId == descriptor.retrievableId } != null)
            Assertions.assertTrue(descriptors.find { it.vector.value.contentEquals(descriptor.vector.value) } != null)
        }
    }

    /**
     * Tests [VectorDescriptorReader.getAll] (with parameters) method.
     */
    @Test
    fun testGetAll() {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)
        val selection = descriptors.shuffled().take(100).map { it.id }
        reader.getAll(selection).forEach { descriptor ->
            Assertions.assertTrue(selection.contains(descriptor.id))
        }
    }

    /**
     * Tests [VectorDescriptorReader.queryAndJoin] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    open fun testQueryAndJoinWithVector(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.ASC,
            100,
            fetchVector = true
        )
        val result = reader.queryAndJoin(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedBy { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach { r ->
            Assertions.assertEquals(r.first.id, r.second.retrievableId)
            Assertions.assertTrue(r.first.hasAttribute(DistanceAttribute::class.java))
            Assertions.assertTrue(r.first.findDescriptor { d -> d.id == r.second.id }.isNotEmpty())
        }
    }

    /**
     * Tests [VectorDescriptorReader.queryAndJoin] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    open fun testQueryAndJoinNoVector(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.ASC,
            100,
            fetchVector = false
        )
        val result = reader.queryAndJoin(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedBy { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(it.first.id, it.second.retrievableId)
            Assertions.assertTrue(it.first.hasAttribute(DistanceAttribute::class.java))
        }
    }

    /**
     * Tests nearest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    open fun testNearestNeighbourSearch(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.ASC,
            100,
            fetchVector = true
        )
        val result = reader.query(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedBy { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(distance(it.first.vector, query.value), distance(it.second.vector, query.value), 0.00005)
        }
    }

    /**
     * Tests farthest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    open fun testFarthestNeighbourSearch(distance: Distance) {
        val writer = this.testConnection.getDescriptorWriter(this.field)
        val reader = this.testConnection.getDescriptorReader(this.field)
        val random = SplittableRandom()

        /* Generate and store test data. */
        val descriptors = this.initialize(writer, random)

        /* Perform nearest neighbour search. */
        val query = ProximityQuery(
            Value.FloatVector(FloatArray(3) { random.nextFloat() }),
            distance,
            SortOrder.DESC,
            100,
            fetchVector = true
        )
        val result = reader.query(query).toList()

        /* Make manual query and compare. */
        val manual = descriptors.sortedByDescending { distance(it.vector, query.value) }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(distance(it.first.vector, query.value), distance(it.second.vector, query.value), 0.00005)
        }
    }

    /**
     * Initializes the test data.
     */
    protected fun initialize(
        writer: DescriptorWriter<FloatVectorDescriptor>,
        random: SplittableRandom
    ): List<FloatVectorDescriptor> {
        val size = random.nextInt(500, 5000)

        /* Generate and store test data. */
        val retrievables = (0 until size).map {
            Ingested(UUID.randomUUID(), "SOURCE:TEST", emptyList(), emptySet(), emptySet(), emptySet(), true)
        }
        Assertions.assertTrue(this.testConnection.getRetrievableWriter().addAll(retrievables))

        val descriptors = retrievables.map {
            FloatVectorDescriptor(UUID.randomUUID(), it.id, Value.FloatVector(FloatArray(3) { random.nextFloat() }))
        }
        Assertions.assertTrue(writer.addAll(descriptors))
        return descriptors
    }

    /**
     * Cleans up the database after each test.
     */
    @BeforeEach
    open fun prepare() {
        this.testConnection.getRetrievableInitializer().initialize()
        this.testConnection.getDescriptorInitializer(this.field).initialize()
    }

    /**
     * Cleans up the database after each test.
     *
     */
    @AfterEach
    open fun cleanup() {
        this.testConnection.getDescriptorInitializer(this.field).deinitialize()
        this.testConnection.getRetrievableInitializer().deinitialize()
    }
}