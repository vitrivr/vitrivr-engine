package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.Connection


/**
 * A [Connection], which is part of a [Schema].
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
@JvmRecord
data class Connection(val databaseName: String, val parameters: Map<String,String> = emptyMap())