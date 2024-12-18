package org.vitrivr.engine.query.operators.transform.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator
import java.util.*

/**
 * A [Operator.Nullary] that fetches a series of [Retrieved] by ID and passes them downstream for processing
 *
 * @author Luca Rossetto
 * @version 1.1.0
 */
class MockRetrievedLookup(
    override val name: String,
    val type: RetrivedType
) : Operator.Nullary<Retrieved> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        val list: MutableList<Retrieved> = mutableListOf()
        IntRange(0, 10).forEach {
            val r = Retrieved(UUID.randomUUID(), type, false)
            r.addDescriptor( Vector())
            list.add(r)
        }
    }
}