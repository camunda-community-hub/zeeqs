package io.zeebe.zeeqs.data.service

import io.zeebe.zeeqs.data.entity.BpmnElementType

data class BpmnElementInfo(
        val elementId: String,
        val elementName: String?,
        val elementType: BpmnElementType,
        val metadata: BpmnElementMetadata,
        val extensionElements: BpmnExtensionElements?,
        val documentation: String?
)