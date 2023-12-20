package org.vitrivr.engine.query.execution

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.vitrivr.engine.core.database.retrievable.RetrievableReader
import org.vitrivr.engine.core.model.retrievable.RetrievableId
import org.vitrivr.engine.core.model.retrievable.Retrieved
import org.vitrivr.engine.core.operators.Operator

class RetrievedLookup(
    private val retrievableReader: RetrievableReader,
    private val ids: Collection<RetrievableId>
) : Operator.Nullary<Retrieved> {
    override fun toFlow(scope: CoroutineScope): Flow<Retrieved> {

        return this.retrievableReader.getAll(ids).map {
            Retrieved.Default(it.id, it.type, false)
        }.asFlow()

    }
}