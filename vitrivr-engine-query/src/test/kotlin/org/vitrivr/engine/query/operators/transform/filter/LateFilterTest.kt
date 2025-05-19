package org.vitrivr.engine.query.operators.transform.filter

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.vitrivr.engine.core.context.QueryContext

class LateFilterTest {

    var filter: LateFilter? = null

    @BeforeEach
    fun setUp() {
        val context = QueryContext()
        val retriever = MockRetrievedLookup(
            name = "retriever",
            type = "test",
            size = 10,
            descriptors = listOf("vector" to { null }),
            contents = listOf("text" to { null }),
            attributes = listOf("property" to { null })
        )
        this.filter = LateFilterFactory().newTransformer("filter", retriever, emptyMap(), context)

    }


    @AfterEach
    fun tearDown() {
    }

}

