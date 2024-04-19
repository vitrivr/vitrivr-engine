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
 * @version 1.1.0
 */
class RetrievedLookup(private val reader: RetrievableReader, private val ids: Collection<RetrievableId>) : Operator.Nullary<Retrieved> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> = flow {
        this@RetrievedLookup.reader.getAll(ids).forEach {
            emit(Retrieved(it.id, it.type, false))
        }
    }
}