package org.vitrivr.engine.core.operators.ingest

import org.vitrivr.engine.core.source.file.MimeType

data class Resolvable(val mimeType: MimeType, val inputStream: java.io.InputStream)