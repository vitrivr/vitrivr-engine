package org.vitrivr.engine.database.jsonl.model

import kotlinx.serialization.Serializable

@Serializable
data class AttributeContainerList(val list: List<AttributeContainer>)
