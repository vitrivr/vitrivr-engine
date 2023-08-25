package org.vitrivr.engine.index.database.string.writer

import org.vitrivr.engine.core.database.retrievable.RetrievableWriter
import org.vitrivr.engine.core.model.database.Persistable
import org.vitrivr.engine.core.model.database.retrievable.Retrievable
import java.io.OutputStream

class StringRetrievableWriter(outputStream: OutputStream, stringify: (Persistable) -> String) : StringWriter<Retrievable>(outputStream, stringify), RetrievableWriter {
}