package org.vitrivr.engine.core.model.content.impl

import org.vitrivr.engine.core.model.content.AudioContent
import org.vitrivr.engine.core.model.content.DerivedContent
import org.vitrivr.engine.core.operators.derive.DerivateName
import java.nio.ShortBuffer

data class DerivedAudioContent(val audioContent: AudioContent, override val deriverName: DerivateName): AudioContent by audioContent, DerivedContent<ShortBuffer>
