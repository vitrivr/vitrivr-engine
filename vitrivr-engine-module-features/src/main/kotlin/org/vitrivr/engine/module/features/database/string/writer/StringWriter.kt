package org.vitrivr.engine.module.features.database.string.writer

import org.vitrivr.engine.core.database.Writer
import org.vitrivr.engine.core.model.Persistable
import org.vitrivr.engine.module.features.database.string.StringConnection
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter

open class StringWriter<T : Persistable>(override val connection: StringConnection, outputStream: OutputStream, private val stringify: (Persistable) -> String) : Writer<T> {

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

    override fun deleteAll(items: Iterable<T>): Boolean {
        throw UnsupportedOperationException("StringWriter is append only")
    }
}