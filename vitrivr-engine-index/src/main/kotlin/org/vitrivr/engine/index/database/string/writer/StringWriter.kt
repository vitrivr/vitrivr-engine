package org.vitrivr.engine.index.database.string.writer

import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.database.Persistable
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter

open class StringWriter<T : Persistable>(outputStream: OutputStream, private val stringify: (Persistable) -> String) : Writer<T> {

    private val writer = PrintWriter(outputStream)

    private fun write(item: T) {
        writer.println(stringify(item))
    }

    override fun add(item: T): Boolean {
        return try {
            write(item)
            writer.flush()
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun addAll(items: Iterable<T>): Boolean {
        return try {
            items.forEach { write(it) }
            writer.flush()
            true
        } catch (e: IOException) {
            false
        }
    }

    override fun update(item: T): Boolean {
        throw UnsupportedOperationException("StringWriter is append only")
    }

    override fun delete(item: T): Boolean {
        throw UnsupportedOperationException("StringWriter is append only")
    }
}