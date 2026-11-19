package org.vitrivr.engine.module.features.feature.external.implementations.vlm

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.model.retrievable.TerminalRetrievable
import org.vitrivr.engine.core.operators.Operator
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class VlmCaptionExtractorTest {
    @Test
    fun runsCaptionsConcurrentlyAndEmitsInSourceOrder() = runBlocking {
        val shots = List(2) { Ingested(UUID.randomUUID(), "SEGMENT", emptyList(), transient = false) }
        val expected = shots + TerminalRetrievable
        val input = object : Operator.Nullary<Retrievable> {
            override val name = "test"
            override fun toFlow(scope: CoroutineScope) = expected.asFlow()
        }
        val secondRequestStarted = CountDownLatch(1)
        val extractor = VlmCaptionExtractor(input, VlmCaption(), null, "caption", { shot ->
            if (shot.id == shots.first().id) {
                check(secondRequestStarted.await(3, TimeUnit.SECONDS))
            } else {
                secondRequestStarted.countDown()
            }
            VlmDescription("caption", "model", "1", JsonObject(emptyMap()))
        }, concurrency = 2)

        val output = extractor.toFlow(this).toList()
        assertEquals(expected.map { it.id }, output.map { it.id })
        assertEquals(listOf(1, 1), output.take(2).map { it.descriptors.size })
    }
}
