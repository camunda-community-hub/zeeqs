package io.zeebe.zeeqs.data.service

import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.BpmnModelInstance
import io.camunda.zeebe.model.bpmn.instance.FlowElement
import io.zeebe.zeeqs.data.repository.ProcessRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProcessService(val processRepository: ProcessRepository) {

    @Cacheable(cacheNames = ["bpmnElementInfo"])
    fun getBpmnElementInfo(processDefinitionKey: Long): Map<String, BpmnElementInfo>? {
        return getBpmnModel(processDefinitionKey)
                ?.let { it.getModelElementsByType(FlowElement::class.java) }
                ?.map {
                    Pair(it.id, BpmnElementInfo(
                            elementId = it.id,
                            elementName = it.name
                    ))
                }
                ?.toMap()
    }

    private fun getBpmnModel(processDefinitionKey: Long): BpmnModelInstance? {
        return processRepository.findByIdOrNull(processDefinitionKey)
                ?.bpmnXML
                ?.byteInputStream()
                ?.let { Bpmn.readModelFromStream(it) }
    }
}
