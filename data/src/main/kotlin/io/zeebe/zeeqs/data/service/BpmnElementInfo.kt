package io.zeebe.zeeqs.data.service

import io.camunda.zeebe.model.bpmn.instance.Documentation
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeProperty
import io.zeebe.zeeqs.data.entity.BpmnElementType

data class BpmnElementInfo(
        val elementId: String,
        val elementName: String?,
        val elementType: BpmnElementType,
        val metadata: BpmnElementMetadata,
        val extensionProperties: Collection<BpmnElementExtensionProperties>?,
        val documentation: String?
)