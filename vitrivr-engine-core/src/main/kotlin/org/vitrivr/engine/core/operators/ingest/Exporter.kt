package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.retrievable.Ingested
import org.vitrivr.engine.core.operators.Operator

/**
 * An [Operator.Unary] that extracts and export information an [Ingested].
 *
 * Typically an [Exporter] derives some content and exports it somewhere outside the main database.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface Exporter : Operator.Unary<Ingested, Ingested>