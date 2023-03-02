package io.zeebe.zeeqs.graphql.resolvers.type

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.data.service.VariableService
import io.zeebe.zeeqs.graphql.resolvers.connection.DecisionEvaluationConnection
import org.springframework.data.repository.findByIdOrNull
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class ElementInstanceResolver(
    val elementInstanceRepository: ElementInstanceRepository,
    val processInstanceRepository: ProcessInstanceRepository,
    val incidentRepository: IncidentRepository,
    val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
    val timerRepository: TimerRepository,
    val processService: ProcessService,
    val messageSubscriptionRepository: MessageSubscriptionRepository,
    val variableService: VariableService,
    private val decisionEvaluationRepository: DecisionEvaluationRepository
) {

    @SchemaMapping(typeName = "ElementInstance", field = "startTime")
    fun startTime(
        elementInstance: ElementInstance,
        @Argument zoneId: String
    ): String? {
        return elementInstance.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "ElementInstance", field = "endTime")
    fun endTime(
        elementInstance: ElementInstance,
        @Argument zoneId: String
    ): String? {
        return elementInstance.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    @SchemaMapping(typeName = "ElementInstance", field = "processInstance")
    fun processInstance(elementInstance: ElementInstance): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(elementInstance.processInstanceKey)
    }

    @SchemaMapping(typeName = "ElementInstance", field = "incidents")
    fun incidents(elementInstance: ElementInstance): List<Incident> {
        return incidentRepository.findByJobKey(elementInstance.key)
    }

    @SchemaMapping(typeName = "ElementInstance", field = "scope")
    fun scope(elementInstance: ElementInstance): ElementInstance? {
        return elementInstance.scopeKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    @SchemaMapping(typeName = "ElementInstance", field = "stateTransitions")
    fun stateTransitions(elementInstance: ElementInstance): List<ElementInstanceStateTransition> {
        return elementInstanceStateTransitionRepository.findByElementInstanceKey(elementInstance.key)
    }

    @SchemaMapping(typeName = "ElementInstance", field = "elementName")
    fun elementName(elementInstance: ElementInstance): String? {
        return processService
            .getBpmnElementInfo(elementInstance.processDefinitionKey)
            ?.get(elementInstance.elementId)
            ?.elementName
    }

    @SchemaMapping(typeName = "ElementInstance", field = "timers")
    fun timers(elementInstance: ElementInstance): List<Timer> {
        return timerRepository.findByElementInstanceKey(elementInstance.key)
    }

    @SchemaMapping(typeName = "ElementInstance", field = "messageSubscriptions")
    fun messageSubscriptions(elementInstance: ElementInstance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByElementInstanceKey(elementInstance.key)
    }

    @SchemaMapping(typeName = "ElementInstance", field = "element")
    fun element(elementInstance: ElementInstance): BpmnElement {
        return BpmnElement(
            processDefinitionKey = elementInstance.processDefinitionKey,
            elementId = elementInstance.elementId,
            elementType = elementInstance.bpmnElementType
        )
    }

    @SchemaMapping(typeName = "ElementInstance", field = "variables")
    fun variables(
        elementInstance: ElementInstance,
        @Argument localOnly: Boolean,
        @Argument shadowing: Boolean
    ): List<Variable> {
        return variableService.getVariables(
            elementInstanceKey = elementInstance.key,
            localOnly = localOnly,
            shadowing = shadowing
        )
    }

    @SchemaMapping(typeName = "ElementInstance", field = "decisionEvaluations")
    fun decisionEvaluations(elementInstance: ElementInstance): DecisionEvaluationConnection {
        return DecisionEvaluationConnection(
            getItems = { decisionEvaluationRepository.findAllByElementInstanceKey(elementInstanceKey = elementInstance.key) },
            getCount = { decisionEvaluationRepository.countByElementInstanceKey(elementInstanceKey = elementInstance.key) }
        )
    }

}