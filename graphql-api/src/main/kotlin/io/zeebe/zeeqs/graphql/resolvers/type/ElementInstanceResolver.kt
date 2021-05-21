package io.zeebe.zeeqs.data.resolvers

import graphql.kickstart.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.*
import io.zeebe.zeeqs.data.service.ProcessService
import io.zeebe.zeeqs.graphql.resolvers.type.ResolverExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ElementInstanceResolver(
    val elementInstanceRepository: ElementInstanceRepository,
    val processInstanceRepository: ProcessInstanceRepository,
    val incidentRepository: IncidentRepository,
    val elementInstanceStateTransitionRepository: ElementInstanceStateTransitionRepository,
    val timerRepository: TimerRepository,
    val processService: ProcessService,
    val messageSubscriptionRepository: MessageSubscriptionRepository
) : GraphQLResolver<ElementInstance> {

    fun startTime(elementInstance: ElementInstance, zoneId: String): String? {
        return elementInstance.startTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun endTime(elementInstance: ElementInstance, zoneId: String): String? {
        return elementInstance.endTime?.let { ResolverExtension.timestampToString(it, zoneId) }
    }

    fun processInstance(elementInstance: ElementInstance): ProcessInstance? {
        return processInstanceRepository.findByIdOrNull(elementInstance.processInstanceKey)
    }

    fun incidents(elementInstance: ElementInstance): List<Incident> {
        return incidentRepository.findByJobKey(elementInstance.key)
    }

    fun scope(elementInstance: ElementInstance): ElementInstance? {
        return elementInstance.scopeKey?.let { elementInstanceRepository.findByIdOrNull(it) }
    }

    fun stateTransitions(elementInstance: ElementInstance): List<ElementInstanceStateTransition> {
        return elementInstanceStateTransitionRepository.findByElementInstanceKey(elementInstance.key)
    }

    fun elementName(elementInstance: ElementInstance): String? {
        return processService
                .getBpmnElementInfo(elementInstance.processDefinitionKey)
                ?.get(elementInstance.elementId)
                ?.elementName
    }

    fun timers(elementInstance: ElementInstance): List<Timer> {
        return timerRepository.findByElementInstanceKey(elementInstance.key)
    }

    fun messageSubscriptions(elementInstance: ElementInstance): List<MessageSubscription> {
        return messageSubscriptionRepository.findByElementInstanceKey(elementInstance.key)
    }

}