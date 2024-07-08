package org.vitrivr.engine.database.pgvector.descriptor.model

import org.postgresql.util.ByteConverter
import org.postgresql.util.PGBinaryObject
import org.postgresql.util.PGobject
import java.io.Serializable
import java.sql.SQLException


/**
 * The [PgBitVector] class represents a bit vector in PostgreSQL.
 *
 * @see https://github.com/pgvector/pgvector-java/blob/master/src/main/java/com/pgvector/PGbit.java
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class PgBitVector () : PGobject(), PGBinaryObject, Serializable {

    /** The [ByteArray] backing this [PgBitVector]. */
    private var vec: ByteArray? = null

    /** The length of this [PgBitVector]. */
    private var length = 0

    /**
     * Constructor
     */
    init {
        this.type = "bit"
    }

    /**
     * Constructor
     *
     * @param v boolean array
     */
    constructor(v: BooleanArray) : this() {
        val vec = ByteArray((v.size + 7) / 8)
        for (i in v.indices) {
            vec[i / 8] = (vec[i / 8].toInt() or ((if (v[i]) 1 else 0) shl (7 - (i % 8)))).toByte()
        }

        /* Update local state. */
        this.vec = vec
        this.length = v.size
    }

    /**
     * Constructor
     *
     * @param s text representation of a bit string
     * @throws SQLException exception
     */
    constructor(s: String?) : this() {
        setValue(s)
    }

    /**
     * Sets the value from a text representation of a bit string
     */
    @Throws(SQLException::class)
    override fun setValue(s: String?) {
        if (s == null) {
            this.vec = null
            this.length = 0
        } else {
            val data = ByteArray((s.length + 7) / 8)
            for (i in s.indices) {
                data[i / 8] = (data[i / 8].toInt() or ((if (s[i] != '0') 1 else 0) shl (7 - (i % 8)))).toByte()
            }
            /* Update local state. */
            this.vec = data
            this.length = s.length
        }
    }

    /**
     * Returns the text representation of a bit string
     */
    override fun getValue(): String? {
        val data = this.vec ?: return null
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(if (((data[i / 8].toInt() shr (7 - (i % 8))) and 1) == 1) '1' else '0')
        }
        return sb.toString()
    }

    /**
     * Returns the number of bytes for the binary representation
     */
    override fun lengthInBytes(): Int = this.vec?.size?.plus(4) ?: 0

    /**
     * Sets the value from a binary representation of a bit string
     */
    @Throws(SQLException::class)
    override fun setByteValue(value: ByteArray, offset: Int) {
        val length = ByteConverter.int4(value, offset)
        val data = ByteArray((length + 7) / 8)
        for (i in data.indices) {
            data[i] = value[offset + 4 + i]
        }

        /* Update local state. */
        this.vec = data
        this.length = length
    }

    /**
     * Writes the binary representation of a bit string
     */
    override fun toBytes(bytes: ByteArray, offset: Int) {
        val data = this.vec ?: return
        ByteConverter.int4(bytes, offset, length)
        for (i in data.indices) {
            bytes[offset + 4 + i] = data[i]
        }
    }

    /**
     * Returns the length
     *
     * @return an array
     */
    fun length(): Int = this.length

    /**
     * Returns a byte array
     *
     * @return an array
     */
    fun toByteArray(): ByteArray? = this.vec

    /**
     * Returns an array
     *
     * @return an array
     */
    fun toArray(): BooleanArray {
        val bits = BooleanArray(this.length)
        for (i in 0 until this.length) {
            bits[i] = ((this.vec!![i / 8].toInt() shr (7 - (i % 8))) and 1) == 1
        }
        return bits
    }
}