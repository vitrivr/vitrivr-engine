package org.vitrivr.engine.query.operators.transform.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.model.content.element.ContentElement
import org.vitrivr.engine.core.model.content.impl.memory.InMemoryTextContent
import org.vitrivr.engine.core.model.descriptor.Descriptor
import org.vitrivr.engine.core.model.descriptor.vector.FloatVectorDescriptor
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.model.retrievable.attributes.PropertyAttribute
import org.vitrivr.engine.core.model.retrievable.attributes.RetrievableAttribute
import org.vitrivr.engine.core.model.types.Value
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * A [Operator.Nullary] that fetches a series of [Retrieved] by ID and passes them downstream for processing
 *
 * @author Luca Rossetto
 * @version 1.1.1
 */
class MockRetrievedLookup(
    override val name: String,
    val type: String,
    val size: Int = 10,
    val descriptors: List<Pair<String, () -> Descriptor<*>?>> = emptyList(),
    val contents: List<Pair<String, () -> ContentElement<*>?>> = emptyList(),
    val attributes: List<Pair<String, () -> RetrievableAttribute?>> = emptyList()

) : Operator.Nullary<Retrieved> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        for (i in 0 until size) {
            val descriptors = this@MockRetrievedLookup.descriptors.map { it.second.invoke()?:randomDescriptor() }.toSet()
            val content = this@MockRetrievedLookup.contents.map { it.second.invoke()?:randomContent() }
            val attributes = this@MockRetrievedLookup.attributes.map { it.second.invoke()?:randomAttribute() }.toSet()
            emit(Retrieved(UUID.randomUUID(), type, content, descriptors, attributes, emptySet(), false))
        }
    }

    /**
     * Generates and returns random [FloatVectorDescriptor].
     *
     * @return [FloatVectorDescriptor]
     */
    private fun randomDescriptor() = FloatVectorDescriptor(
        UUID.randomUUID(), UUID.randomUUID(), Value.FloatVector(FloatArray(10) { Random().nextFloat() })
    )

    /**
     * Generates and returns random [InMemoryTextContent].
     *
     * @return [InMemoryTextContent]
     */
    private fun randomContent() = InMemoryTextContent(randomText())

    /**
     * Generates and returns random [RetrievableAttribute].
     *
     * @return [RetrievableAttribute]
     */
    private fun randomAttribute(): RetrievableAttribute {
        val r = Random().nextLong(0, 100000000000)
        val d = Random().nextLong(100000000, 1000000000)
        val map = mapOf(
            "start" to r.toString(),
            "end" to (r+d).toString(),
            "value" to randomText(),
        )
        return PropertyAttribute(map)
    }

    /**
     * Generates and returns random text.
     *
     * @return [String]
     */
    private fun randomText(): String {
        val wordSet = setOf(
            "Lorem", "ipsum", "dolor", "sit", "amet", "consetetur", "sadipscing", "elitr", "sed", "diam", "nonumy", "eirmod",
            "tempor", "invidunt", "ut", "labore", "et", "dolore", "magna", "aliquyam", "erat", "voluptua", "At", "vero", "eos",
            "accusam", "justo", "duo", "dolores", "ea", "rebum", "Stet", "clita", "kasd", "gubergren", "no", "sea", "takimata",
            "sanctus", "est"
        )
        val numWordsInSentence = Random().nextInt(5, 10)  // Random number of words in a sentence between 5 and 10
        val sb = StringBuilder()
        for (i in 1..numWordsInSentence) {
            val randomWord = wordSet.random()  // Pick a random word from the set
            sb.append(randomWord)
        }
        return sb.toString()

    }
}


