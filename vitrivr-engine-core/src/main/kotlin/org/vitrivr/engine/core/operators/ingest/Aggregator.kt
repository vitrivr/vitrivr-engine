package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Aggregator] is used to aggregate the content contained in an [Ingested] according to some defined logic.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 */
interface Aggregator : Operator.Unary<Retrievable, Retrievable>