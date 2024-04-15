package org.vitrivr.engine.core.model.metamodel

import org.vitrivr.engine.core.database.MockConnection

class MockSchema(name: String = "test-schema"): Schema(name, MockConnection("test-connection")) {
}
