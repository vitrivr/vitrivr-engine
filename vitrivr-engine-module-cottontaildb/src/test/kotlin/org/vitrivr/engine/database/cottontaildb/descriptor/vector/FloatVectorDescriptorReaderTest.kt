package org.vitrivr.engine.database.cottontaildb.descriptor.vector

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.vitrivr.engine.core.database.descriptor.vector.AbstractFloatVectorDescriptorReaderTest
import org.vitrivr.engine.core.model.query.basics.Distance
import org.vitrivr.engine.core.model.query.basics.SortOrder
import org.vitrivr.engine.core.model.query.proximity.ProximityQuery
import org.vitrivr.engine.core.model.retrievable.attributes.DistanceAttribute
import org.vitrivr.engine.core.model.types.Value
import java.util.*

/**
 * An [AbstractFloatVectorDescriptorReaderTest] for Cottontail DB.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class FloatVectorDescriptorReaderTest : AbstractFloatVectorDescriptorReaderTest("test-schema-cottontaildb.json") {
    /**
     * Tests [VectorDescriptorReader.queryAndJoin] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING"])
    override fun testQueryAndJoinWithVector(distance: Distance) {
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
        val manual = descriptors.sortedBy {
            if (distance == Distance.IP) {
                -1.0 * distance(
                    it.vector,
                    query.value
                ) /* Definition problem with IP in Cottontail DB, which is not a distance. */
            } else {
                distance(it.vector, query.value)
            }
        }.take(100)
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
    override fun testQueryAndJoinNoVector(distance: Distance) {
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
        val manual = descriptors.sortedBy {
            if (distance == Distance.IP) {
                -1.0 * distance(
                    it.vector,
                    query.value
                ) /* Definition problem with IP in Cottontail DB, which is not a distance. */
            } else {
                distance(it.vector, query.value)
            }
        }.take(100)
        result.zip(manual).forEach {
            Assertions.assertEquals(it.first.id, it.second.retrievableId)
            Assertions.assertTrue(it.first.hasAttribute(DistanceAttribute::class.java))
        }
    }
}
