package org.vitrivr.engine.query.operators.retrieval

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Nullary] that fetches a series of [Retrieved] by ID and passes them downstream for processing
 *
 * @author Luca Rossetto
 * @version 1.1.1
 */
class RetrievedLookup(private val reader: RetrievableReader, private val ids: Collection<RetrievableId>, override val name: String) : Operator.Nullary<Retrieved> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        this@RetrievedLookup.reader.getAll(this@RetrievedLookup.ids).forEach { emit(it) }
    }
}