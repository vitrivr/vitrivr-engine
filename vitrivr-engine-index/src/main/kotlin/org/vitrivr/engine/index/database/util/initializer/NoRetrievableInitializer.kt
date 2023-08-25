package org.vitrivr.engine.index.database.util.initializer

import org.vitrivr.engine.core.database.retrievable.RetrievableInitializer
import org.vitrivr.engine.core.model.database.retrievable.Retrievable

class NoRetrievableInitializer : NoInitializer<Retrievable>(), RetrievableInitializer