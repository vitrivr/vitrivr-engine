package org.vitrivr.engine.core.database.retrievable

import org.vitrivr.engine.core.database.Reader
import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.database.retrievable.Retrievable

/**
 * A [RetrievableWriter] is an extension of a [Retrievable] for [Retrievable]s.
 *
 * @author Luca Rossetto
 * @author Ralph Gasser
 * @version 1.0.0
 */
interface RetrievableReader: Reader<Retrievable>