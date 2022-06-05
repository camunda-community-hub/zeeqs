package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.BpmnElementType

data class BpmnElement(
        val processDefinitionKey: Long,
        val elementId: String,
        val elementType: BpmnElementType? = null
)
