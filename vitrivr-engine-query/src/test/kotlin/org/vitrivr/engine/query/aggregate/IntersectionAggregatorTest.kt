package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Test for [IntersectionAggregator] to verify it correctly identifies items in the intersection of multiple inputs.
 *
 * @author henrikluemkemann
 * @version 1.0.0
 */
class IntersectionAggregatorTest {

    @Test
    fun testIntersection() = runBlocking {
        // Create fixed UUIDs for testing
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val id5 = UUID.randomUUID()

        // Create mock retrievers with overlapping IDs
        val retriever1 = MockRetrievedLookupWithFixedIds(
            name = "retriever1",
            type = "test",
            ids = listOf(id1, id2, id3)
        )

        val retriever2 = MockRetrievedLookupWithFixedIds(
            name = "retriever2",
            type = "test",
            ids = listOf(id2, id3, id4)
        )

        val retriever3 = MockRetrievedLookupWithFixedIds(
            name = "retriever3",
            type = "test",
            ids = listOf(id3, id4, id5)
        )

        // Create the IntersectionAggregator with the mock retrievers
        val aggregator = IntersectionAggregator(
            inputs = listOf(retriever1, retriever2, retriever3),
            name = "intersection"
        )

        val results = aggregator.toFlow(this).toList()

        // Verify that only id3 is in the intersection
        assertEquals(1, results.size, "There should be exactly one item in the intersection")
        assertEquals(id3, results.first().id, "The ID in the intersection should be id3")

        // Test with two retrievers to verify different intersection behavior
        val twoInputAggregator = IntersectionAggregator(
            inputs = listOf(retriever1, retriever2),
            name = "intersection2"
        )

        val twoInputResults = twoInputAggregator.toFlow(this).toList()

        // Verify that id2 and id3 are in the intersection
        assertEquals(2, twoInputResults.size, "There should be exactly two items in the intersection")
        val resultIds = twoInputResults.map { it.id }.toSet()
        assertEquals(setOf(id2, id3), resultIds, "The IDs in the intersection should be id2 and id3")
    }

    @Test
    fun testEmptyIntersection() = runBlocking {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        val id4 = UUID.randomUUID()
        val id5 = UUID.randomUUID()

        // Create mock retrievers with no overlapping IDs
        val retriever1 = MockRetrievedLookupWithFixedIds(
            name = "retriever1",
            type = "test",
            ids = listOf(id1, id2)
        )

        val retriever2 = MockRetrievedLookupWithFixedIds(
            name = "retriever2",
            type = "test",
            ids = listOf(id3, id4)
        )

        val retriever3 = MockRetrievedLookupWithFixedIds(
            name = "retriever3",
            type = "test",
            ids = listOf(id5)
        )

        // Create the IntersectionAggregator with the mock retrievers
        val aggregator = IntersectionAggregator(
            inputs = listOf(retriever1, retriever2, retriever3),
            name = "intersection"
        )

        val results = aggregator.toFlow(this).toList()

        // Verify that there are no items in the intersection
        assertEquals(0, results.size, "There should be no items in the intersection")
    }

    @Test
    fun testSingleInput() = runBlocking {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()

        // create a single mock retriever
        val retriever = MockRetrievedLookupWithFixedIds(
            name = "retriever",
            type = "test",
            ids = listOf(id1, id2)
        )

        val aggregator = IntersectionAggregator(
            inputs = listOf(retriever),
            name = "intersection"
        )

        val results = aggregator.toFlow(this).toList()

        // Verify that all items from the single input are returned
        assertEquals(2, results.size, "All items from the single input should be returned")
        val resultIds = results.map { it.id }.toSet()
        assertEquals(setOf(id1, id2), resultIds, "The IDs should be id1 and id2")
    }

    @Test
    fun testEmptyInput() = runBlocking {
        // create the IntersectionAggregator with no inputs
        val aggregator = IntersectionAggregator(
            inputs = emptyList(),
            name = "intersection"
        )

        val results = aggregator.toFlow(this).toList()

        // Verify that there are no results
        assertEquals(0, results.size, "There should be no results for empty input")
    }
}