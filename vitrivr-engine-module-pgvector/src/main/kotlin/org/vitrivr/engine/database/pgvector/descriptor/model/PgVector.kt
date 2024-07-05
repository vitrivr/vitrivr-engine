package org.vitrivr.engine.database.pgvector.descriptor.model

import org.postgresql.util.ByteConverter
import org.postgresql.util.PGBinaryObject
import org.postgresql.util.PGobject
import org.vitrivr.engine.core.model.types.Value
import java.io.Serializable
import java.sql.SQLException
import java.util.*

/**
 * The [PgVector] class represents a vector in PostgreSQL.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgVector(private var vec: FloatArray? = null) : PGobject(), PGBinaryObject, Serializable {

    init {
        this.type = "vector"
    }

    /**
     * Constructor
     *
     * @param <T> number
     * @param v list of numbers
     */
    constructor(v: List<Number>) : this(FloatArray(v.size) {
        v[it].toFloat()
    })

    /**
     * Constructor
     *
     * @param <T> number
     * @param v list of numbers
     */
    constructor(vector: List<Value<*>>) : this(FloatArray(vector.size) {
        when (val v = vector[it]) {
            is Value.Float-> v.value
            is Value.Double-> v.value.toFloat()
            is Value.Int-> v.value.toFloat()
            is Value.Long-> v.value.toFloat()
            else -> throw IllegalArgumentException("Could not convert $v to float.")
        }
    })

    /**
     * Constructor
     *
     * @param s text representation of a vector
     * @throws SQLException exception
     */
    constructor(s: String?) : this() {
        setValue(s)
    }

    /**
     * Sets the value from a text representation of a vector
     */
    @Throws(SQLException::class)
    override fun setValue(s: String?) {
        if (s == null) {
            this.vec = null
        } else {
            val sp = s.substring(1, s.length - 1).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            this.vec = FloatArray(sp.size)
            for (i in sp.indices) {
                vec!![i] = sp[i].toFloat()
            }
        }
    }

    /**
     * Returns the text representation of a vector
     */
    override fun getValue(): String? = this.vec?.contentToString()?.replace(" ", "")

    /**
     * Returns the number of bytes for the binary representation
     */
    override fun lengthInBytes(): Int = if (vec == null) 0 else 4 + vec!!.size * 4

    /**
     * Sets the value from a binary representation of a vector
     */
    @Throws(SQLException::class)
    override fun setByteValue(value: ByteArray, offset: Int) {
        val dim: Short = ByteConverter.int2(value, offset)
        val unused: Short = ByteConverter.int2(value, offset + 2)
        if (unused != 0.toShort()) {
            throw SQLException("expected unused to be 0")
        }

        this.vec = FloatArray(dim.toInt()) {
            ByteConverter.float4(value, offset + 4 + it * 4)
        }
    }

    /**
     * Writes the binary representation of a vector
     */
    override fun toBytes(bytes: ByteArray, offset: Int) {
        if (vec == null) {
            return
        }

        // server will error on overflow due to unconsumed buffer
        // could set to Short.MAX_VALUE for friendlier error message
        ByteConverter.int2(bytes, offset, vec!!.size)
        ByteConverter.int2(bytes, offset + 2, 0)
        for (i in this.vec!!.indices) {
            ByteConverter.float4(bytes, offset + 4 + i * 4, vec!![i])
        }
    }

    /**
     * Returns an array
     *
     * @return an array
     */
    fun toArray(): FloatArray? = this.vec
}