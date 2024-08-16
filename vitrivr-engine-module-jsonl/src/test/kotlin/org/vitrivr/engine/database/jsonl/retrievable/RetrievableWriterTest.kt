package org.vitrivr.engine.database.jsonl.retrievable

import org.vitrivr.engine.core.database.retrievable.AbstractRetrievableWriterTest

class RetrievableWriterTest : AbstractRetrievableWriterTest("test-schema-jsonl.json") {
    override fun testDeleteAll() {
        /* Not supported. */
    }

    override fun testDelete() {
        /* Not supported. */
    }

    override fun testUpdate() {
        /* Not supported. */
    }
}