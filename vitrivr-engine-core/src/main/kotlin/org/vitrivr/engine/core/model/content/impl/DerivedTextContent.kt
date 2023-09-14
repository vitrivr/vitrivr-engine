package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.model.content.element.TextContent
import org.vitrivr.engine.core.operators.derive.DerivateName

data class DerivedTextContent(val textContent: TextContent, override val deriverName: DerivateName): TextContent by textContent, DerivedContent
