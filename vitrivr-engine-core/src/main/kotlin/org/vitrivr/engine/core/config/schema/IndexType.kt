package org.vitrivr.engine.core.config.schema

/**
 * The type of index that can be created on a [Schema.Field].
 */
enum class IndexType {
    /** A B-tree based index. Well-suited for point-lookups and Boolean search on scalar values. */
    SCALAR,

    /** A fulltext index. Well-suited for fulltext-queries. */
    FULLTEXT,

    /** Index suited for NNS. */
    NNS
}