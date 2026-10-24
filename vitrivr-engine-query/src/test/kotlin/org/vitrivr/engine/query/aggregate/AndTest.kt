package org.vitrivr.engine.query.aggregate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.ScoreAttribute
import org.vitrivr.engine.core.operators.Operator
import java.util.UUID

class AndTest {
    private fun source(vararg values: Retrievable) = object : Operator.Nullary<Retrievable> {
        override val name = "source"
        override fun toFlow(scope: CoroutineScope) = values.asFlow()
    }

    private fun item(id: UUID = UUID.randomUUID(), score: Double = 1.0) =
        Retrieved(id, "IMAGE", attributes = setOf(ScoreAttribute.Unbound(score)))

    @Test
    fun `intersects all inputs and uses minimum score`() = runBlocking {
        val shared = item(score = 0.9)
        val onlyTwo = item()
        val result = And(listOf(
            source(shared, onlyTwo, item()),
            source(item(shared.id, 0.7), onlyTwo),
            source(item(shared.id, 0.8))
        ), "and").toFlow(this).toList()
        assertEquals(listOf(shared.id), result.map { it.id })
        assertEquals(0.7, result.single().filteredAttribute(ScoreAttribute::class.java)?.score)
    }

    @Test
    fun `duplicates in one input do not satisfy another input`() = runBlocking {
        val a = item()
        assertTrue(And(listOf(source(a, a), source(item())), "and").toFlow(this).toList().isEmpty())
        assertEquals(1, And(listOf(source(a, a), source(a, a)), "and").toFlow(this).toList().size)
    }

    @Test
    fun `empty input produces no results`() = runBlocking {
        assertTrue(And(listOf(source(item()), source()), "and").toFlow(this).toList().isEmpty())
    }

    @Test
    fun `single input deduplicates and preserves order`() = runBlocking {
        val a = item()
        val b = item()
        assertEquals(listOf(b.id, a.id), And(listOf(source(b, a, b)), "and").toFlow(this).toList().map { it.id })
    }

    @Test
    fun `requires an input`() {
        assertThrows(IllegalArgumentException::class.java) { And(emptyList(), "and") }
    }

    @Test
    fun `missing score contributes zero`() = runBlocking {
        val a = item()
        val result = And(listOf(source(a), source(Retrieved(a.id, "IMAGE"))), "and").toFlow(this).toList()
        assertEquals(0.0, result.single().filteredAttribute(ScoreAttribute::class.java)?.score)
    }
}
