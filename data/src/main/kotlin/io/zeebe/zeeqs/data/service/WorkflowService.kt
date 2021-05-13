package io.zeebe.zeeqs.data.service

import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.BpmnModelInstance
import io.camunda.zeebe.model.bpmn.instance.FlowElement
import io.zeebe.zeeqs.data.repository.WorkflowRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowService(val workflowRepository: WorkflowRepository) {

    @Cacheable(cacheNames = ["bpmnElementInfo"])
    fun getBpmnElementInfo(workflowKey: Long): Map<String, BpmnElementInfo>? {
        return getBpmnModel(workflowKey)
                ?.let { it.getModelElementsByType(FlowElement::class.java) }
                ?.map {
                    Pair(it.id, BpmnElementInfo(
                            elementId = it.id,
                            elementName = it.name
                    ))
                }
                ?.toMap()
    }

    private fun getBpmnModel(workflowKey: Long): BpmnModelInstance? {
        return workflowRepository.findByIdOrNull(workflowKey)
                ?.bpmnXML
                ?.byteInputStream()
                ?.let { Bpmn.readModelFromStream(it) }
    }
}
