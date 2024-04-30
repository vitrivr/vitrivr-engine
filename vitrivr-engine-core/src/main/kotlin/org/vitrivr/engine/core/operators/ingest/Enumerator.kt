package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.model.retrievable.Retrievable
import org.vitrivr.engine.core.operators.Operator
import org.vitrivr.engine.core.source.Source

/**
 * A [Enumerator] to process a data source that can be used for index.
 *
 * A [Enumerator] basically produces a stream of [Source] objects from some data source. The data source could
 * be a folder in the file system, in which case the [Source] would be individual files.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @vesion 1.0.0
 */
interface Enumerator : Operator.Nullary<Retrievable>