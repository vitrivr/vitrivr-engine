package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.element.AudioContent
import org.vitrivr.engine.core.model.content.decorators.DerivedContent
import org.vitrivr.engine.core.operators.derive.DerivateName

data class DerivedAudioContent(val audioContent: AudioContent, override val deriverName: DerivateName): AudioContent by audioContent, DerivedContent
