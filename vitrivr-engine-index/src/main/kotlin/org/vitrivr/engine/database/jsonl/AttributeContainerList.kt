package org.vitrivr.engine.database.jsonl

import kotlinx.serialization.Serializable

@Serializable
data class AttributeContainerList(val list: List<AttributeContainer>)
