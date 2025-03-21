package org.vitrivr.engine.database.cottontaildb.descriptor.vector

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.vitrivr.engine.core.database.descriptor.vector.AbstractFloatVectorDescriptorReaderTest
import org.vitrivr.engine.core.model.query.basics.Distance
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
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING", "IP"])
    override fun testQueryAndJoinWithVector(distance: Distance) {
        super.testQueryAndJoinWithVector(distance)
    }

    /**
     * Tests [VectorDescriptorReader.queryAndJoin] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING", "IP"])
    override fun testQueryAndJoinNoVector(distance: Distance) {
        super.testQueryAndJoinNoVector(distance)

    }

    /**
     * Tests nearest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING", "IP"])
    override fun testNearestNeighbourSearch(distance: Distance) {
        super.testNearestNeighbourSearch(distance)
    }

    /**
     * Tests farthest neighbour search through the [VectorDescriptorReader.query] method.
     */
    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["JACCARD", "HAMMING", "IP"])
    override fun testFarthestNeighbourSearch(distance: Distance) {
        super.testFarthestNeighbourSearch(distance)
    }
}
