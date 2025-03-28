package org.vitrivr.engine.database.pgvector.retrievable

import org.vitrivr.engine.core.database.retrievable.AbstractRetrievableWriterTest
import org.vitrivr.engine.database.pgvector.PgVectorConnection

/**
 * An [AbstractRetrievableWriterTest] for the [PgVectorConnection].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgRetrievableWriterTest : AbstractRetrievableWriterTest("test-schema-postgres.json")