package io.zeebe.zeeqs.data.service

import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.BpmnModelInstance
import io.camunda.zeebe.model.bpmn.impl.BpmnModelConstants
import io.camunda.zeebe.model.bpmn.instance.*
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeAssignmentDefinition
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeSubscription
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeTaskDefinition
import io.zeebe.zeeqs.data.entity.BpmnElementType
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
                ?.map { flowElement ->
                    Pair(flowElement.id, BpmnElementInfo(
                            elementId = flowElement.id,
                            elementName = flowElement.name,
                            elementType = getBpmnElementType(flowElement),
                            metadata = getMetadata(flowElement)
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

    private fun getBpmnElementType(element: FlowElement): BpmnElementType {
        return when (element.elementType.typeName) {
            BpmnModelConstants.BPMN_ELEMENT_PROCESS -> BpmnElementType.PROCESS
            BpmnModelConstants.BPMN_ELEMENT_SUB_PROCESS -> getBpmnSubprocessType(element)
            BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY -> BpmnElementType.CALL_ACTIVITY
            BpmnModelConstants.BPMN_ELEMENT_SEQUENCE_FLOW -> BpmnElementType.SEQUENCE_FLOW
            BpmnModelConstants.BPMN_ELEMENT_EXCLUSIVE_GATEWAY -> BpmnElementType.EXCLUSIVE_GATEWAY
            BpmnModelConstants.BPMN_ELEMENT_PARALLEL_GATEWAY -> BpmnElementType.PARALLEL_GATEWAY
            BpmnModelConstants.BPMN_ELEMENT_EVENT_BASED_GATEWAY -> BpmnElementType.EVENT_BASED_GATEWAY
            BpmnModelConstants.BPMN_ELEMENT_START_EVENT -> BpmnElementType.START_EVENT
            BpmnModelConstants.BPMN_ELEMENT_END_EVENT -> BpmnElementType.END_EVENT
            BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT -> BpmnElementType.INTERMEDIATE_CATCH_EVENT
            BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT -> BpmnElementType.INTERMEDIATE_THROW_EVENT
            BpmnModelConstants.BPMN_ELEMENT_BOUNDARY_EVENT -> BpmnElementType.BOUNDARY_EVENT
            BpmnModelConstants.BPMN_ELEMENT_SERVICE_TASK -> BpmnElementType.SERVICE_TASK
            BpmnModelConstants.BPMN_ELEMENT_USER_TASK -> BpmnElementType.USER_TASK
            BpmnModelConstants.BPMN_ELEMENT_MANUAL_TASK -> BpmnElementType.MANUAL_TASK
            BpmnModelConstants.BPMN_ELEMENT_SEND_TASK -> BpmnElementType.SEND_TASK
            BpmnModelConstants.BPMN_ELEMENT_RECEIVE_TASK -> BpmnElementType.RECEIVE_TASK
            BpmnModelConstants.BPMN_ELEMENT_BUSINESS_RULE_TASK -> BpmnElementType.BUSINESS_RULE_TASK
            BpmnModelConstants.BPMN_ELEMENT_SCRIPT_TASK -> BpmnElementType.SCRIPT_TASK
            BpmnModelConstants.BPMN_ELEMENT_INCLUSIVE_GATEWAY -> BpmnElementType.INCLUSIVE_GATEWAY
            else -> BpmnElementType.UNKNOWN
        }
    }

    private fun getBpmnSubprocessType(element: FlowElement) =
            if (element is SubProcess) {
                if (element.triggeredByEvent()) {
                    BpmnElementType.EVENT_SUB_PROCESS
                } else {
                    BpmnElementType.SUB_PROCESS
                }
            } else {
                BpmnElementType.UNKNOWN
            }

    private fun getMetadata(element: FlowElement): BpmnElementMetadata {
        return BpmnElementMetadata(
                jobType = element
                        .getSingleExtensionElement(ZeebeTaskDefinition::class.java)
                        ?.type,
                conditionExpression = when (element) {
                    is SequenceFlow -> element.conditionExpression?.textContent
                    else -> null
                },
                timerDefinition = when (element) {
                    is CatchEvent -> element.eventDefinitions
                            ?.filterIsInstance(TimerEventDefinition::class.java)
                            ?.firstOrNull()
                            ?.let { it.timeCycle ?: it.timeDate ?: it.timeDuration }
                            ?.textContent
                    else -> null
                },
                errorCode = when (element) {
                    is CatchEvent -> element.eventDefinitions
                            ?.filterIsInstance(ErrorEventDefinition::class.java)
                            ?.firstOrNull()
                            ?.error
                            ?.errorCode
                    else -> null
                },
                calledProcessId = element
                        .getSingleExtensionElement(ZeebeCalledElement::class.java)
                        ?.processId,
                messageSubscriptionDefinition = when (element) {
                    is CatchEvent -> element.eventDefinitions
                            ?.filterIsInstance(MessageEventDefinition::class.java)
                            ?.firstOrNull()
                            ?.message
                            ?.let {
                                MessageSubscriptionDefinition(
                                        messageName = it.name,
                                        messageCorrelationKey = it
                                                .getSingleExtensionElement(ZeebeSubscription::class.java)
                                                ?.correlationKey
                                )
                            }
                    else -> null
                },
                assignee = element
                        .getSingleExtensionElement(ZeebeAssignmentDefinition::class.java)
                        ?.assignee,
                candidateGroups = element
                        .getSingleExtensionElement(ZeebeAssignmentDefinition::class.java)
                        ?.candidateGroups
        )
    }
}
